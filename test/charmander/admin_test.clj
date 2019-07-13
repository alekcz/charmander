(ns charmander.admin-test
  (:require [clojure.test :refer :all]
  					[clojure.string :as str]
						[clojure.pprint :as pp]
						[environ.core :refer [env]]
						[clj-uuid :as uuid]
  				  [charmander.admin :refer :all])
	(:import 	com.google.auth.oauth2.GoogleCredentials
						com.google.firebase.FirebaseApp
						com.google.firebase.FirebaseOptions
						com.google.firebase.FirebaseOptions$Builder))

(comment

	"Template for tests"

	(deftest test-tempate
		(testing "Testing functionname"
			(let [data "" other ""]
				(do
					(is (= (#'charmander.admin/privatefunction inputs) answer))
					(is (= 1 (- 2 1)))))))
)

;Initialise Firebase Admin
(#'charmander.admin/init)

; Tests for the Admin SDK

(deftest test-create-user
	(testing "Testing the creating  of new users"
			(let [unique (str (uuid/v1))]
				(let [response  (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")
							response2 (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(do
						(is (= (:email response) (str unique "@domain.com"))) 
						(is (= response2 {:error true, :error-code "email-already-exists"})) 
						(is (not (= response response2)))
						(#'charmander.admin/delete-user (:uid response)))))))
						
(deftest test-all-get-users
	(testing "Testing the retrieval of user by uid or by email"
			(let [unique (str (uuid/v1))]
				(let [response (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(do
						(is (= (#'charmander.admin/get-user (:uid response)) response))
						(is (= (#'charmander.admin/get-user-by-email (:email response)) response))
						(is (= (#'charmander.admin/get-user-by-email (:email response)) (#'charmander.admin/get-user (:uid response))))
						(#'charmander.admin/delete-user (:uid response)))))))

(deftest test-set-display-name
	(testing "Testing the  setting of user display name"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-display-name (:uid prep) "Charmander")]
						(do
							(is (= (:uid response) (:uid prep)))
							(is (= (:display-name response) "Charmander")))
							(is (not (=  (:display-name response) (:display-name  prep))))
							(#'charmander.admin/delete-user (:uid prep)))))))

(deftest test-set-phone-number
	(testing "Testing the setting of user phone number"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-phone-number (:uid prep) "+27123456789")]
						(do
							(is (= (:uid response) (:uid prep)))
							(is (= (:phone-number response) "+27123456789")))
							(is (not (=  (:phone-number response) (:phone-number  prep))))
							(#'charmander.admin/delete-user (:uid prep)))))))

(deftest test-set-user-email-1
	(testing "Testing the setting of user email"
			(let [unique (str (uuid/v1)) unique2 (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-email (:uid prep) (str unique2 "@domain.com"))]
						(do
							(is (= (:uid response) (:uid prep)))
							(is (= (:email response) (str unique2 "@domain.com")))
							(is (not (=  (:email response) (:email  prep))))
							(#'charmander.admin/delete-user (:uid prep))))))))

(deftest test-set-user-email-2
	(testing "Testing the setting of user email"
			(let [unique (str (uuid/v1)) unique2 (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure") prep2 (#'charmander.admin/create-user (str unique2 "@domain.com")  "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-email (:uid prep) (str unique2 "@domain.com"))]
						(do
							(is (:error response))
							(#'charmander.admin/delete-user (:uid prep))
							(#'charmander.admin/delete-user (:uid prep2))))))))


(deftest test-set-password-1
	(testing "Testing the  setting of user password"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-password (:uid prep) "Charizard")]
						(do
							(is (= (:uid response) (:uid prep)))
							(#'charmander.admin/delete-user (:uid prep))))))))

(deftest test-set-password-2
	(testing "Testing the  setting of user password"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-password (:uid prep) "")]
						(do
							(is (:error response))
							(#'charmander.admin/delete-user (:uid prep))))))))

;(println (env :firebase-service-key))
;(println (#'charmander.admin/delete-user "nHdBq5wEu3WOEd3IfjLiaouXkr03"))
;(println (#'charmander.admin/create-user "email@domain.com" "superDuperSecure"))
;(println (#'charmander.admin/get-user "foHCpMoaT7P3WXeBTgWR261Z2mX2"))
;(println (#'charmander.admin/get-user-by-email "email@domain.com"))
;(println (#'charmander.admin/set-user-display-name "foHCpMoaT7P3WXeBTgWR261Z2mX2" "email"))
;(println (#'charmander.admin/set-user-password "foHCpMoaT7P3WXeBTgWR261Z2mX2" "superDuperExtra53cur3"))
;(println (#'charmander.admin/generate-email-verification-link "email@domain.com"))
;(println (#'charmander.admin/generate-password-reset-link "email@domain.com"))