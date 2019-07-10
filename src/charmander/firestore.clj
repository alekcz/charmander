(ns charmander.firestore
  (:require [clojure.java.io :as io])
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

;(def firestore (atom nil))

; private methods

(defn- build-firebase-options [key-file-json database-name] 
  (-> (new FirebaseOptions$Builder) ;use thread-first when the final part of the function will return value to be used
      (.setCredentials (GoogleCredentials/fromStream (io/input-stream key-file-json))) 
      (.setDatabaseUrl (str "https://" database-name ".firebaseio.com"))
      (.build)))

(defn- snapshot-to-map [snapshot]
  (keywordize-keys (into {} (. snapshot getData))))

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
  (do 
    (. api-future get)
    true))
          
; public methods

; firestore api

(defn get-document [collection document]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document document)))]
      (let [futuristic (cast ApiFuture (. reff get))]
        (let [document-snapshot (cast DocumentSnapshot (. futuristic get))]
          (if (. document-snapshot exists) 
            (let [object (snapshot-to-map document-snapshot)]
              (assoc {}
              :id (. document-snapshot getId) 
              :data (assoc object :subcollections (list-document-subcollection reff))))
            nil))))))

(defn get-document-and-subcollections [collection document]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document document)))]
      (let [futuristic (cast ApiFuture (. reff get))]
        (let [document-snapshot (cast DocumentSnapshot (. futuristic get))]
          (if (. document-snapshot exists) 
            (let [object (snapshot-to-map document-snapshot)]
              (assoc {}
              :id (. document-snapshot getId) 
              :data (assoc object :subcollections (process-document-subcollection reff))))
            nil))))))

(defn get-collection [collection]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast CollectionReference (-> firestore (.collection collection)))]
      (let [futuristic (cast ApiFuture (. reff get))]
        (let [firestore-collection (. futuristic get)]
            (let [collection-list (. firestore-collection getDocuments)]
              (for [x collection-list] 
                (assoc {} 
                  :id (. x getId) 
                  :data (snapshot-to-map x)) )))))))

(defn add-document-to-collection [collection data]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast CollectionReference (-> firestore (.collection collection)))]
      (resolve-write-future (-> reff (.add (stringify-keys data)))))))

(defn create-document [collection name data]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
      (resolve-write-future (-> reff (.create (stringify-keys data)))))))

(defn add-document-to-subcollection [collection document name data]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast CollectionReference (-> firestore (.collection collection) (.document document) (.collection name)))]
      (resolve-write-future (-> reff (.add (stringify-keys data)))))))

(defn set-document [collection name data]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
      (resolve-write-future (-> reff (.set (stringify-keys data)))))))

(defn update-document [collection name data]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
      (resolve-write-future (-> reff (.update (stringify-keys data)))))))

(defn delete-document [collection name]
  (let [firestore (FirestoreClient/getFirestore)] 
    (let [reff (cast DocumentReference (-> firestore (.collection collection) (.document name) ))]
      (resolve-write-future (-> reff (.delete))))))