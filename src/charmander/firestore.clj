(ns charmander.firestore
  (:require [clojure.java.io :as io]
            [jsonista.core :as json]
            [charmander.admin :as charm-admin]
            [clojure.string :as str])
  (:import 	com.google.auth.oauth2.GoogleCredentials
            com.google.firebase.FirebaseApp
            com.google.firebase.FirebaseOptions
            com.google.firebase.FirebaseOptions$Builder
            com.google.firebase.auth.FirebaseAuth
            com.google.firebase.auth.FirebaseAuthException
            com.google.firebase.auth.UserRecord
            com.google.firebase.auth.UserRecord$CreateRequest
            com.google.firebase.auth.UserRecord$UpdateRequest
            ;firestore
            com.google.firebase.cloud.FirestoreClient
            com.google.cloud.firestore.Firestore
            com.google.cloud.firestore.QueryDocumentSnapshot
            com.google.cloud.firestore.QuerySnapshot
            com.google.cloud.firestore.DocumentReference
            com.google.cloud.firestore.DocumentSnapshot
            com.google.cloud.firestore.WriteResult
            com.google.api.core.ApiFuture
            com.google.cloud.firestore.CollectionReference)
  (:gen-class))

(def mapper (json/object-mapper {:decode-key-fn true}))

(defn- keywordize-keys
  "Recursively transforms all map keys from strings to keywords."
  [m]
  (let [f (fn [[k v]] (if (string? k) [(keyword k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))    

(defn- stringify-keys
  "Recursively transforms all map keys from keywords to strings."
  [m]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))  

; private methods

(defn- snapshot-to-map [snapshot]
  (let [mapped (into {} (. snapshot getData))]
    (-> mapped json/write-value-as-string (json/read-value mapper))))

(defn- process-document [reff]
   (let [futuristic (cast ApiFuture (. reff get))]
        (let [query-snapshot (. futuristic get)]
          (. query-snapshot getDocuments))))

(defn- process-document-subcollection [firestore-collection]
    (let [collection-list (. firestore-collection getCollections)]
      (for [x collection-list] 
        (assoc {} 
          :id (. x getId) 
          :path (. x getPath) 
          :data (for [y (process-document x)]
                  (assoc {} 
                    :id (. y getId) 
                    :data (snapshot-to-map y)))))))
        
(defn- list-document-subcollection [firestore-collection]
    (let [collection-list (. firestore-collection getCollections)]
      (for [x collection-list] 
        (assoc {} 
          :id (. x getId) 
          :path (. x getPath)))))

(defn- resolve-write-future [api-future]
  (try
    (. api-future get)
  (catch Exception e {:error (str (. e getMessage))})))
  
(defn- clean [string]
  string) ;turns out firebase can handle strings.
; public methods

; firestore api
(defn init []
  (charm-admin/init))

(defn get-document [collection document]
  (let [collection (clean collection) document (clean document)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document document)))]
        (let [futuristic (cast ApiFuture (. reff get))]
          (let [document-snapshot (cast DocumentSnapshot (. futuristic get))]
            (if (. document-snapshot exists) 
              (let [object (snapshot-to-map document-snapshot)]
                (assoc {}
                :id (. document-snapshot getId) 
                :data (assoc object :subcollections (list-document-subcollection reff))))
              nil)))))))

(defn get-document-and-subcollections [collection document]
  (let [collection (clean collection) document (clean document)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document document)))]
        (let [futuristic (cast ApiFuture (. reff get))]
          (let [document-snapshot (cast DocumentSnapshot (. futuristic get))]
            (if (. document-snapshot exists) 
              (let [object (snapshot-to-map document-snapshot)]
                (assoc {}
                :id (. document-snapshot getId) 
                :data (assoc object :subcollections (process-document-subcollection reff))))
              nil)))))))

(defn get-collection [collection]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast CollectionReference (-> firestore (.collection collection)))]
        (let [futuristic (cast ApiFuture (. reff get))]
          (let [firestore-collection (. futuristic get)]
              (let [collection-list (. firestore-collection getDocuments)]
                (for [x collection-list] 
                  (assoc {} :id (. x getId) :data (snapshot-to-map x)) ))))))))

(defn- query [collection-reference args]
  (let [property (:where args)]
    (cond 
      (some? (:equals args)) ;==
        (-> collection-reference (.whereEqualTo property (:equals args)))
      (some? (:equal-to args)) ;==
        (-> collection-reference (.whereEqualTo property (:equal-to args)))
      (some? (:less-than args)) ;<
        (-> collection-reference (.whereLessThan property (:less-than args)))
      (some? (:greater-than args)) ;> 
        (-> collection-reference (.whereGreaterThan property (:greater-than args)))
      (some? (:less-than-or-equal-to args)) ;<=
        (-> collection-reference (.whereLessThanOrEqualTo property (:less-than-or-equal-to args)))
      (some? (:greater-than-or-equal-to args)) ;>=
        (-> collection-reference (.whereGreaterThanOrEqualTo property (:greater-than-or-equal-to args)))
      (and (some? (:between args)) (= (count (:between args)) 2))  ;a < x < b
        (let [lower (first (:between args)) upper (second (:between args))]
          (cond  
            (true? (:include-upper args)) (-> collection-reference (.whereGreaterThan property lower) (.whereLessThanOrEqualTo property upper))
            (true? (:include-lower args)) (-> collection-reference (.whereGreaterThanOrEqualTo property lower) (.whereLessThan property upper))
            :else (-> collection-reference (.whereGreaterThan property lower) (.whereLessThan property upper))))
      (and (some? (:from args)) (= (count (:from args)) 2) ) ;a <= x <= b
        (let [lower (first (:from args)) upper (second (:from args))]
          (-> collection-reference (.whereGreaterThanOrEqualTo property lower) (.whereLessThanOrEqualTo property upper)))
      (some? (:contains args)) 
        (-> collection-reference (.whereArrayContains property (:contains args))))))

(defn query-collection [collection & args]
  (let [collection (clean collection)]
    (let [arguments (apply hash-map args)]
      (if (empty? (:where arguments))
        (get-collection collection)    
        (let [firestore (FirestoreClient/getFirestore)] 
          (let [collref (cast CollectionReference (-> firestore (.collection collection)))]
            (let [reff (query collref arguments)]
              (let [futuristic (cast ApiFuture (. reff get))]
                (let [firestore-collection (. futuristic get)]
                    (let [collection-list (. firestore-collection getDocuments)]
                      (for [x collection-list] 
                        (assoc {} :id (. x getId) :data (snapshot-to-map x)) )))))))))))

(defn add-document-to-collection [collection data]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [collreff (cast CollectionReference (-> firestore (.collection collection)))]
        (let [pre-reff (-> collreff (.add (stringify-keys data)))]
          (let [reff (. pre-reff get)]
            (let [futuristic (cast ApiFuture (. reff get))]
              (let [document-snapshot (cast DocumentSnapshot (. futuristic get))]
                (let [object (snapshot-to-map document-snapshot)]
                  (assoc {}
                  :id (. document-snapshot getId) 
                  :data (assoc object :subcollections (list-document-subcollection reff))))))))))))

(defn push-document-to-collection [collection data]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [collreff (cast CollectionReference (-> firestore (.collection collection)))]
        (-> collreff (.add (stringify-keys data)))))))

(defn create-document [collection name data]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
        (resolve-write-future (-> reff (.create (stringify-keys data))))))))
        
(defn set-document [collection name data]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
        (resolve-write-future (-> reff (.set (stringify-keys data))))))))

(defn update-document [collection name data]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
        (resolve-write-future (-> reff (.update (stringify-keys data))))))))

(defn delete-document [collection name]
  (let [collection (clean collection)]
    (let [firestore (FirestoreClient/getFirestore)] 
      (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
        (resolve-write-future (-> reff (.delete)))))))