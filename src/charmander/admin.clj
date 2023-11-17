(ns charmander.admin
  (:require [clojure.java.io :as io]
            [clojure.walk :as w]
            [environ.core :refer [env]]
            [googlecredentials.core :as gcred])
  (:import 	com.google.auth.oauth2.GoogleCredentials
            com.google.auth.oauth2.ServiceAccountCredentials
            com.google.firebase.FirebaseApp
            com.google.firebase.FirebaseOptions
            com.google.firebase.FirebaseOptions$Builder
            com.google.firebase.auth.FirebaseAuth
            com.google.firebase.auth.UserMetadata
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

; private methods

(defn- build-firebase-options 
  ([]
    (try 
      (-> (new FirebaseOptions$Builder) ;use thread-first when the final part of the function will return value to be used
          (.setCredentials (gcred/load-service-credentials))
          (.build))
    (catch Exception e (println "\nError: FIREBASE_CONFIG/GOOGLE_APPLICATION_CREDENTIALS AND GOOGLE_CLOUD_PROJECT environment variables must both be set"))))
  ([database-name]
    (try 
      (-> (new FirebaseOptions$Builder) ;use thread-first when the final part of the function will return value to be used
          (.setCredentials ^ServiceAccountCredentials (gcred/load-service-credentials))
          (.setDatabaseUrl (str "https://" database-name ".firebaseio.com"))
          (.build))
    (catch Exception e (println "\nError: FIREBASE_CONFIG/GOOGLE_APPLICATION_CREDENTIALS AND GOOGLE_CLOUD_PROJECT environment variables must both be set")))))
  

(defn- build-create-user-request [email password] 
  (let [create-request (new UserRecord$CreateRequest)]
      (doto create-request ;doto mutates the object. Use it when you're going to return the object
        (.setEmail email)
        (.setPassword password)
        (.setEmailVerified false)
        (.setDisabled false))))

(defn- convert-user-record-to-map [^UserRecord user-record]
  { :email (. user-record getEmail)
    :email-verified (. user-record isEmailVerified)
    :uid (. user-record getUid)
    :provider-id (. user-record getProviderId)
    :photo-url (. user-record getPhotoUrl)
    :phone-number (. user-record getPhoneNumber)
    :display-name (. user-record getDisplayName)
    :disabled (. user-record isDisabled)
    :custom-claims (. user-record getCustomClaims)
    :created-at (doto user-record (.getUserMetadata) (.getCreationTimestamp))})

(defn- format-error [error]
   (. error getMessage))
; public methods

; init admin api
(defn init []
  (let [database-name (or (env :firebase-database) (env :google-cloud-project))]
    (if (nil? database-name)
      (try
          (. FirebaseAuth getInstance) 
        (catch IllegalStateException ise
          (let [options (build-firebase-options)]
            (. FirebaseApp initializeApp options)))))
      (try
          (. FirebaseAuth getInstance) 
        (catch IllegalStateException ise
          (let [options (build-firebase-options database-name)]
            (. FirebaseApp initializeApp options))))))

; user management api

(defn create-user [email password]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance) 
        create-request (build-create-user-request email password)]
      (convert-user-record-to-map (. firebase-auth createUser create-request)))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn delete-user [uuid]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (. firebase-auth deleteUser uuid))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn get-user [uuid]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (convert-user-record-to-map (. firebase-auth getUser uuid)))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn get-user-by-email [email]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (convert-user-record-to-map (. firebase-auth getUserByEmail email)))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn get-user-by-phone-number [phone-number]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (convert-user-record-to-map (. firebase-auth 	getUserByPhoneNumber phone-number)))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn set-user-email [uuid email]
  (try
   (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setEmail email) (.setEmailVerified false)))))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn set-user-password [uuid password]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setPassword password)))))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn set-user-phone-number [uuid phone-number]
  (try  
    (let [firebase-auth (. FirebaseAuth getInstance)
          update-request (new UserRecord$UpdateRequest uuid)]
        (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setPhoneNumber phone-number)))))
        (catch Exception e {:error true :error-data (format-error e)})))

(defn set-user-display-name [uuid display-name]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setDisplayName display-name)))))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn set-user-photo-url [uuid photo-url]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setPhotoUrl photo-url)))))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn generate-password-reset-link [email]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (. firebase-auth generatePasswordResetLink email))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn generate-email-verification-link [email]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (. firebase-auth generateEmailVerificationLink email))
      (catch Exception e {:error true :error-data (format-error e)})))

(defn generate-custom-token [uuid]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (. firebase-auth createCustomToken uuid))
    (catch Exception e {:error true :error-data (format-error e)})))

(defn set-custom-user-claims [uuid ^clojure.lang.PersistentHashMap claims]
  (try
    (let [firebase-auth (. FirebaseAuth getInstance)]
      (. firebase-auth setCustomUserClaims uuid (java.util.HashMap. claims)))
    (catch Exception e {:error true :error-data (format-error e)})))
