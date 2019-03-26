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
(def connection (d/create-conn {}))
(def serialized-db (atom ""))
(def ready(atom false))

; private methods

(defn- storage-instance [path] 
  (-> (FirebaseDatabase/getInstance) ;use thread-first when the final part of the function will return value to be used
      (.getReference path)))

(defn- write-data [storage-ref db]
  (. storage-ref setValueAsync (pr-str @db)))

; public methods

(defn conn [] connection)
(defn conn? [] (and connection @ready))

(defn attach-storage [path]
  (let [stor (storage-instance path)]
    
    (let [p (fn [^String data] 
              (do 
                (reset! serialized-db data)
                (reset! ready true)))
          listener (. stor addValueEventListener 
                      (reify ValueEventListener
                        (onDataChange [this data-snapshot] 
                         (p (. data-snapshot getValue)))))]
                         (do 
                            (add-watch connection :database-watcher
                              (fn [key atom old-state new-state]
                                (if @ready (. stor setValueAsync (pr-str new-state)))))
                            (add-watch serialized-db :serialization-watcher
                              (fn [key atom old-state new-state]
                                  (d/reset-conn! connection (read-db new-state))))
                            stor))))

