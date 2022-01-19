(ns charmander.core
	(:require 
            [clj-http.client :as http]
            [jsonista.core :as json]
            [clojure.string :as str]
            [base64-clj.core :as base64]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [overtone.at-at :as at])
	(:gen-class))

(def public-keys (atom nil))
(def public-key-url  "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com")
(def mapper (json/object-mapper {:decode-key-fn true}))

; Fetching and updating public keys

(defn- update-public-keys 
	"Update the public key store with the desired data"
	[pubkey-atom data]
	(reset! pubkey-atom (json/read-value data mapper)))

(defn- load-public-keys 
	"Loads the public keys from https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
	[threadpool callback]
	(let [{:keys [_ headers body error]} (http/get public-key-url)]
	    (if error
	       (throw (Exception. "Could not retrieve public key"))
	       (do
	       		(update-public-keys public-keys body)
	       		(callback threadpool headers)))))

(defn- schedule-public-key-update 
	"Schedules the next update of the public key based on response header cache-control info (see https://firebase.google.com/docs/auth/admin/verify-id-tokens)"
	[threadpool response-header]
	(let [cache-control-header (:cache-control response-header)
				seconds-to-next-update (Integer. (str/replace (re-find #"max-age=\d+" (str cache-control-header)) "max-age=" ""))]
			(at/after 
				(* 1000 seconds-to-next-update) 
				(fn [] (load-public-keys threadpool schedule-public-key-update)) 
				threadpool 
				:desc "Refresh public keys")))

; Allow specific domains using regex

(defn- verify-domain 
	"Test the domain using regex. If valid returns the unsigned token data"
	[projectid-regex data]
	(let [project-matches (re-matches (re-pattern (str projectid-regex)) (:projectid data))] 
		(if (nil? project-matches) nil data)))	

; Formatting data for return

(defn- format-result 
	"Format result for easy use. Removes nesting from map"
	[data]
	(when (not (nil? data))
		{ :projectid (:aud data)
			:uid (:user_id data)
			:email (:email data)
			:email_verified (:email_verified data)
			:sign_in_provider (-> data :firebase :sign_in_provider)
			:exp (:exp data)
			:auth_time (:auth_time data)}))	

; Dealing with JWT tokens

(defn- pad-token 
	"Pads token to so that length is a multiple of 4 as required by base64"
	[token]
	(let [len (count token)
				remainder (mod len 4)]
		(if (zero? remainder)
			  token
			  ;a base64 string must have a length that is multiple of 4
			  (let [padding (- 4 remainder)] 
			  		;"="" is the padding character for base64 encoded strings
			      	(str token (apply str (repeat padding "=")))))))

(defn- get-token-header 
	"Retrieves header from token. Header is used to find appropriate public key (see https://firebase.google.com/docs/auth/admin/verify-id-tokens)"
	[token]
	(let [token-array (str/split token #"\." 3)]
		(json/read-value (base64/decode (pad-token (first token-array))) mapper)))

(defn- validate-claims [data]
	(let [now (quot (System/currentTimeMillis) 1000)]
	(and
		(= (str "https://securetoken.google.com/" (:aud data)) (:iss data))
		(> (:exp data) now)
		(<= (:iat data) now)
		(not (str/blank? (:sub data)))
		(<= (:auth_time data) now))))

(defn- authenticate 
	"Core library method. Validates token using public key and returns formatted data"
	[projectid-regex token opts]
	(let [header  (get-token-header token)
				cert (keys/str->public-key ((keyword (:kid header)) @public-keys))
				unsigned-data (if (keys/public-key? cert) (jwt/unsign token cert (merge {:alg :rs256} opts)) nil)
				validated? (validate-claims unsigned-data)]
				(when validated?
					(verify-domain projectid-regex (format-result unsigned-data)))))

; public methods

(defn validate-token 
	"Public method that validates token and makes sure the issuing domain is also valid"
	[projectid-regex token & opts]
	(try
		(if (nil? @public-keys) 
			(let [threadpool (at/mk-pool)] ;make threadpool for public key updates
				(load-public-keys threadpool schedule-public-key-update) 
				(authenticate projectid-regex token (merge {} opts)))
			(authenticate projectid-regex token (merge {} opts)))
		(catch Exception _ nil)))
