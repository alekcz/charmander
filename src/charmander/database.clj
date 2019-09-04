(ns charmander.database
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:import 	com.google.auth.oauth2.GoogleCredentials
            com.google.firebase.FirebaseApp
            com.google.firebase.FirebaseOptions
            com.google.firebase.FirebaseOptions$Builder
            com.google.firebase.auth.FirebaseAuth
            com.google.firebase.auth.FirebaseAuthException
            ;firestore
            com.google.firebase.database.FirebaseDatabase	
            com.google.firebase.database.DatabaseReference
            com.google.firebase.database.DataSnapshot
            com.google.firebase.database.Transaction
            com.google.firebase.database.Query
            com.google.firebase.database.DatabaseError
            com.google.firebase.database.DatabaseException
            com.google.firebase.database.MutableData
            com.google.firebase.database.ValueEventListener
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
  (let [clojurified (json/decode (json/encode data) true)]
     clojurified))

; database API

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

(defn get-object [path] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [reff (. database-instance getReference path)]
     (.addListenerForSingleValueEvent
        reff
        (reify ValueEventListener
          (onDataChange [this dataSnapshot]
            (normalize (. dataSnapshot getValue))))))))

(defn attach-listener-to-object [path callback] 
  (let [database-instance (. FirebaseDatabase getInstance)]
    (let [reff (. database-instance getReference path)]
      (.addValueEventListener reff
        (reify ValueEventListener
          (onDataChange [this dataSnapshot]
            (callback (normalize (. dataSnapshot getValue)))))))))
              