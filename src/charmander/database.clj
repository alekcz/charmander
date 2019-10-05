(ns charmander.database
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [charmander.admin :as charm-admin]
            [clojure.string :as str]
            [clojure.core.async :as async])
  (:import 	com.google.auth.oauth2.GoogleCredentials
            com.google.firebase.FirebaseApp
            com.google.firebase.FirebaseOptions
            com.google.firebase.FirebaseOptions$Builder
            com.google.firebase.auth.FirebaseAuth
            com.google.firebase.auth.FirebaseAuthException
            ;realtime-database
            com.google.firebase.database.FirebaseDatabase	
            com.google.firebase.database.Query
            com.google.firebase.database.DatabaseReference
            com.google.firebase.database.DataSnapshot
            com.google.firebase.database.Transaction
            com.google.firebase.database.DatabaseError
            com.google.firebase.database.DatabaseException
            com.google.firebase.database.MutableData
            com.google.firebase.database.ValueEventListener
            com.google.firebase.database.ChildEventListener
            com.google.api.core.ApiFuture)
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

(defn- normalize [data]
  (if (nil? data)
    false
    (let [clojurified (json/decode (json/encode data) true)]
       clojurified)))

(defn iteration->seq [iteration]
 (seq
  (reify java.lang.Iterable 
      (iterator [this] 
         (reify java.util.Iterator
           (hasNext [this] (.hasNext iteration))
           (next [this] (.next iteration))
           (remove [this] (.remove iteration)))))))

;For some obscure reason the java-admin-sdk only allows queries for numbers that are doubles. 
(defn- handle-type [typed-data]
  (if (number? typed-data) 
    (double typed-data)
    typed-data))

(defn- snapshotValue [^DataSnapshot dataSnapshot] 
  (. dataSnapshot getValue))

(def listener-map (atom {}))

(defn has-listener [path listener]
    (some? (get-in @listener-map [path listener])))

(defn add-listener [path listener]
    (if (has-listener path listener)
      (swap! listener-map update-in [path listener] inc)
      (swap! listener-map assoc-in [path listener] 1)))  


(defn order [db-reference args]
  (cond 
    (some? (:order-by-child args)) ;==
      (-> db-reference (.orderByChild (:order-by-child args)))
    (some? (:order-by-key args)) ;==
      (-> db-reference (.orderByKey))
    (some? (:order-by-value args)) ;==
      (-> db-reference (.orderByValue (:order-by-value args)))
    :else db-reference))

(defn query [db-reference args]
  ;(println (instance? com.google.firebase.database.DatabaseReference db-reference))
  (let [query-reference (if (instance? com.google.firebase.database.DatabaseReference db-reference)
                            (. db-reference orderByKey)
                            db-reference )]
    (cond 
      (some? (:limit-to-first args)) ;==
        (-> query-reference (.limitToFirst (handle-type (:limit-to-first args))))
      (some? (:limit-to-last args)) ;==
        (-> query-reference (.limitToLast (handle-type (:limit-to-last args))))
      (some? (:start-at args)) ;==
        (-> query-reference (.startAt (handle-type (:start-at args))))
      (some? (:end-at args)) ;==
        (-> query-reference (.endAt (handle-type (:end-at args))))      
      (some? (:equal-to args)) ;==
        (-> query-reference (.equalTo (handle-type (:equal-to args))))      
      (some? (:equals args)) ;==
        (-> query-reference (.equalTo (handle-type (:equals args))))        
      :else query-reference)))

; database API

(defn init []
  (charm-admin/init))


(defn push-object [path data] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [reff (. database-instance getReference path)]
      (let [new-reff (. reff push)]
        (. new-reff setValueAsync (stringify-keys data))
        (. new-reff getKey)))))

(defn set-object [path data] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [reff (. database-instance getReference path)]
      (. reff setValueAsync (stringify-keys data)))))

(defn delete-object [path] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [reff (. database-instance getReference path)]
      (. reff setValueAsync nil))))

(defn update-object [path data] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [reff (. database-instance getReference path)]
      (. reff updateChildrenAsync (stringify-keys data)))))

(defn get-object [path channel & arguments]  
  (let [database-instance (. FirebaseDatabase getInstance) args (apply hash-map arguments)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (.addListenerForSingleValueEvent 
          reff  (reify ValueEventListener
                  (onDataChange [this dataSnapshot]
                    (let [snapshot (normalize (. dataSnapshot getValue))]
                      (async/>!! channel snapshot)))))))))

(defn get-children [path channel & arguments]  
  (let [database-instance (. FirebaseDatabase getInstance) args (apply hash-map arguments)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (.addListenerForSingleValueEvent 
          reff  (reify ValueEventListener
                  (onDataChange [this dataSnapshot]
                    (let [iterator (. (. dataSnapshot getChildren) iterator)]
                      (let [children (normalize (map snapshotValue (iterator-seq iterator)))]
                        (doseq [child children]
                          (async/>!! channel child)))))))))))                          

(defn listen-to-object [path channel & args] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (do 
          (add-listener path "object")
          (.addValueEventListener 
            reff  (reify ValueEventListener
                    (onDataChange [this dataSnapshot]
                      (let [snapshot (normalize (. dataSnapshot getValue))]
                        (async/>!! channel snapshot)))))
          true)))))

(defn listen-to-child-added [path channel & args] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (do 
          (add-listener path "child-added")
          (.addChildEventListener 
            reff  (reify ChildEventListener
                    (onChildAdded [this dataSnapshot prevChildKey]
                      (let [iterator (. (. dataSnapshot getChildren) iterator)]
                        (let [children (normalize (map snapshotValue (iterator-seq iterator)))]
                          (doseq [child children]
                            (async/>!! channel child))))
          true))))))))

(defn listen-to-child-changed [path channel & args] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (do 
          (add-listener path "child-changed")
          (.addChildEventListener 
            reff  (reify ChildEventListener
                    (onChildChanged [this dataSnapshot prevChildKey]
                      (let [iterator (. (. dataSnapshot getChildren) iterator)]
                        (let [children (normalize (map snapshotValue (iterator-seq iterator)))]
                          (doseq [child children]
                            (async/>!! channel child))))
          true))))))))

(defn listen-to-child-removed [path channel & args] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (do 
          (add-listener path "child-removed")
          (.addChildEventListener 
            reff  (reify ChildEventListener
                    (onChildRemoved [this dataSnapshot]
                      (let [iterator (. (. dataSnapshot getChildren) iterator)]
                        (let [children (normalize (map snapshotValue (iterator-seq iterator)))]
                          (doseq [child children]
                            (async/>!! channel child))))
          true))))))))

(defn listen-to-child-moved [path channel & args] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [raw-reff (. database-instance getReference path)]
      (let [reff (-> raw-reff (order args) (query args))]
        (do 
          (add-listener path "child-moved")
          (.addChildEventListener 
          reff  (reify ChildEventListener
                  (onChildMoved [this dataSnapshot prevChildKey]
                    (let [iterator (. (. dataSnapshot getChildren) iterator)]
                          (let [children (normalize (map snapshotValue (iterator-seq iterator)))]
                            (doseq [child children]
                              (async/>!! channel child))))
          true))))))))
