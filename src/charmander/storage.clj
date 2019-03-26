(ns charmander.storage
	(:require 
            [datascript.core :as d]
            [overtone.at-at :as at])
  (:import 	com.google.auth.oauth2.GoogleCredentials
            com.google.firebase.FirebaseApp
            com.google.firebase.FirebaseOptions
            com.google.firebase.database.DatabaseReference
            com.google.firebase.database.FirebaseDatabase
            com.google.firebase.database.DataSnapshot
            com.google.firebase.database.ValueEventListener)
	(:gen-class))

(defn read-db [ser] (clojure.edn/read-string {:readers datascript.core/data-readers} ser))
(def conn (d/create-conn {}))
(def serialized-db (atom ""))

; private methods


;;(defn- refresh-db [serialized-data]
;;   (d/reset-conn! conn )

(defn- get-storage-instance [path] 
  (-> (FirebaseDatabase/getInstance) ;use thread-first when the final part of the function will return value to be used
      (.getReference path)))

; Add watch to serialized-db

(add-watch serialized-db :watcher
  (fn [key atom old-state new-state]
    (do 
      (d/reset-conn! conn (read-db new-state))
      (println new-state))))


; public methods

(defn attach-storage [path]
  (let [storage-instance (get-storage-instance path)]
    
    (let [p (fn [^String data] (reset! serialized-db data))
          listener (. storage-instance addValueEventListener 
                      (reify ValueEventListener
                        (onDataChange [this data-snapshot] 
                         (p (. data-snapshot getValue)))))]
                         storage-instance)))

(defn write-data [storage-ref db]
  (. storage-ref setValueAsync (pr-str @db)))