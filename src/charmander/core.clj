(ns charmander.core
	(:require 
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]
            [base64-clj.core :as base64]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [overtone.at-at :as at])
	(:gen-class))

(def public-keys (atom nil))
(def threadpool (at/mk-pool)) ;make threadpool for public key updates
(def public-key-url  "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com")

; Fetching and updating public keys

(defn- update-public-keys [pubkey-atom data]
	"Update the public key store with the desired data"
	(reset! pubkey-atom (json/decode data true)))

(defn- load-public-keys [threadpool callback]
	"Loads the public keys from https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
	(let [{:keys [status headers body error] :as resp} @(http/get public-key-url)]
	    (if error
	       (throw (Exception. "Could not retrieve public key"))
	       (do
	       		(update-public-keys public-keys body)
	       		(callback threadpool headers)))))

(defn- schedule-public-key-update [thread-pool response-header]
	"Schedules the next update of the public key based on response header cache-control info (see https://firebase.google.com/docs/auth/admin/verify-id-tokens)"
	(let [cache-control-header (:cache-control response-header)]
		(let [seconds-to-next-update (Integer. (str/replace (re-find #"max-age=\d+" (str cache-control-header)) "max-age=" ""))]
			(at/after (* 3600 seconds-to-next-update) #(load-public-keys) thread-pool :desc "Refresh public keys"))))

; Allow specific domains using regex

(defn- verify-domain [projectid-regex data]
	"Test the domain using regex. If valid returns the unsigned token data"
	(let [project-matches (re-matches (re-pattern (str projectid-regex)) (:projectid data))] 
		(if (nil? project-matches) nil data)))	

; Formatting data for return

(defn- format-result [data]
	"Format result for easy use. Removes nesting from map"
	(if (not (nil? data))
		{   :projectid (:aud data)
            :uid (:user_id data)
            :email (:email data)
            :email_verified (:email_verified data)
            :sign_in_provider (-> data :firebase :sign_in_provider)
            :exp (:exp data)
           	:auth_time (:auth_time data)}))	

; Dealing with JWT tokens

(defn- pad-token [token]
	"Pads token to so that length is a multiple of 4 as required by base64"
	(let [len (count token) ]
		(let [remainder (mod len 4)]
		(if (zero? remainder)
			  token
			  ;a base64 string must have a length that is multiple of 4
			  (let [padding (- 4 remainder)] 
			  		;"="" is the padding character for base64 encoded strings
			      	(str token (apply str (repeat padding "=")))))))) 

(defn- get-token-header [token]
	"Retrieves header from token. Header is used to find appropriate public key (see https://firebase.google.com/docs/auth/admin/verify-id-tokens)"
    (let [token-array (str/split token #"\." 3)]
    	(json/decode (base64/decode (pad-token (first token-array))) true)))

(defn- authenticate [projectid-regex token]
	"Core library method. Validates token using public key and returns formatted data"
  	(let [header  (get-token-header token)]
       	(let [cert (keys/str->public-key ((keyword (:kid header)) @public-keys))]
       		(let [unsigned-data (if (keys/public-key? cert) (jwt/unsign token cert {:alg :rs256}) nil)]
       			(verify-domain projectid-regex (format-result unsigned-data))))))	

; public methods

(defn validate-token [projectid-regex token]
	"Public method that validates token and makes sure the issuing domain is also valid"
	(try
		(if (nil? @public-keys) 
			(do	(load-public-keys threadpool schedule-public-key-update) (authenticate projectid-regex token))
			(authenticate projectid-regex token))
		(catch Exception e nil)))
