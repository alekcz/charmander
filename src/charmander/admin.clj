(ns charmander.admin
  (:require [clojure.java.io :as io]
            [environ.core :refer [env]])
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

; private methods

(defn- string->stream
  ([s] (string->stream s "UTF-8"))
  ([s encoding]
   (-> s
       (.getBytes encoding)
       (java.io.ByteArrayInputStream.))))

(defn- build-firebase-options []
  (try 
    (-> (new FirebaseOptions$Builder) ;use thread-first when the final part of the function will return value to be used
        (.setCredentials (GoogleCredentials/fromStream (string->stream (env :firebase-config))));(io/input-stream key-file-json))) 
        (.build))
  (catch Exception e (println "\nError: FIREBASE_CONFIG AND GOOGLE_CLOUD_PROJECT environment variables must both be set"))))

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
    :disabled (. user-record isDisabled)})
          
; public methods

; init admin api
(defn init []
  (try
    (. FirebaseAuth getInstance) 
  (catch IllegalStateException ise
    (let [options (build-firebase-options)]
      (. FirebaseApp initializeApp options)))))

; user management api

(defn create-user [email password]
  (let [firebase-auth (. FirebaseAuth getInstance) 
        create-request (build-create-user-request email password)]
    (try
      (convert-user-record-to-map (. firebase-auth createUser create-request))
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn delete-user [uuid]
  (let [firebase-auth (. FirebaseAuth getInstance)]
    (try
      (. firebase-auth deleteUser uuid)
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn get-user [uuid]
  (let [firebase-auth (. FirebaseAuth getInstance)]
    (try
      (convert-user-record-to-map (. firebase-auth getUser uuid))
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn get-user-by-email [email]
  (let [firebase-auth (. FirebaseAuth getInstance)]
    (try
      (convert-user-record-to-map (. firebase-auth getUserByEmail email))
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn get-user-by-phone-number [phone-number]
  (let [firebase-auth (. FirebaseAuth getInstance)]
    (try
      (convert-user-record-to-map (. firebase-auth 	getUserByPhoneNumber phone-number))
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn set-user-email [uuid email]
  (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
    (try
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setEmail email) (.setEmailVerified false))))
      (catch IllegalArgumentException iae {:error true :error-code "invalid-email"})
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn set-user-password [uuid password]
  (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
    (try
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setPassword password))))
      (catch IllegalArgumentException iae {:error true :error-code "invalid-password"})
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn set-user-phone-number [uuid phone-number]
  (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
    (try
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setPhoneNumber phone-number))))
      (catch IllegalArgumentException iae {:error true :error-code "invalid-phone-number"})
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn set-user-display-name [uuid display-name]
  (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
    (try
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setDisplayName display-name))))
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn set-user-photo-url [uuid photo-url]
  (let [firebase-auth (. FirebaseAuth getInstance)
        update-request (new UserRecord$UpdateRequest uuid)]
    (try
      (convert-user-record-to-map (. firebase-auth updateUser (doto update-request (.setPhotoUrl photo-url))))
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn generate-password-reset-link [email]
  (let [firebase-auth (. FirebaseAuth getInstance)]
    (try
      (. firebase-auth generatePasswordResetLink email)
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))

(defn generate-email-verification-link [email]
  (let [firebase-auth (. FirebaseAuth getInstance)]
    (try
      (. firebase-auth generateEmailVerificationLink email)
      (catch FirebaseAuthException fae {:error true :error-code (. fae getErrorCode)}))))
