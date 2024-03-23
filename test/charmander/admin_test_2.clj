(ns charmander.admin-test-2
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
		(testing "Testing function name"
			(let [data "" other ""]
				(do
					(is (= (#'charmander.admin/privatefunction inputs) answer))
					(is (= 1 (- 2 1)))))))
)

;Test fixtures
(defn admin-fixture [f]
	(#'charmander.admin/init "FIREBASE_CONFIG")
	(f))

(use-fixtures :once admin-fixture)

; Tests for the Admin SDK

(deftest test-create-user
	(testing "Testing the creating  of new users"
			(let [unique (str (uuid/v1))]
				(let [response  (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")
							response2 (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(do
						(is (= (:email response) (str unique "@domain.com"))) 
						(is (not (= response response2)))
						(#'charmander.admin/delete-user (:uid response)))))))

(deftest test-create-user-with-uid
	(testing "Testing the creating of new users with uid"
			(let [unique (str (uuid/v1) "-" (uuid/v1))]
				(let [response  (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure" unique)
							response2 (#'charmander.admin/create-user (str unique "+other@domain.com") "superDuperSecure" unique)]
					(do
						(is (= (:email response) (str unique "@domain.com"))) 
						(is (not (= response response2)))
						(#'charmander.admin/delete-user unique))))))

(deftest test-all-get-users
	(testing "Testing the retrieval of user by uid or by email"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-phone-number (:uid prep) "+27123456789")]
						(do
							(is (= (#'charmander.admin/get-user (:uid response)) response))
							(is (= (#'charmander.admin/get-user-by-phone-number "+27123456789") response))
							(is (= (#'charmander.admin/get-user-by-email (:email response)) response))
							(is (= (#'charmander.admin/get-user-by-email (:email response)) (#'charmander.admin/get-user (:uid response))))
							(#'charmander.admin/delete-user (:uid response))))))))

(deftest test-get-user
	(testing "Testing the retrieval of user by uid or by email"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-phone-number (:uid prep) "+27123456789")]
						(let [response2 (#'charmander.admin/get-user "")]
							(is (contains? response2 :error))
							(#'charmander.admin/delete-user (:uid response))))))))	

(deftest test-get-user-by-phone
	(testing "Testing the retrieval of user by uid or by email"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-phone-number (:uid prep) "+27123456789")]
						(let [response2 (#'charmander.admin/get-user-by-phone-number "")]
							(is (contains? response2 :error))
							(#'charmander.admin/delete-user (:uid response))))))))							

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

(deftest test-set-display-name-2
	(testing "Testing the  setting of user display name"
		(let [unique (str (uuid/v1))]
			(let [response (#'charmander.admin/set-user-display-name "" "Charmander")]
				(is (contains? response :error))))))													

(deftest test-set-phone-number-1
	(testing "Testing the setting of user phone number"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-phone-number (:uid prep) "+27123456789")]
						(do
							(is (= (:uid response) (:uid prep)))
							(is (= (:phone-number response) "+27123456789")))
							(is (not (=  (:phone-number response) (:phone-number  prep))))
							(#'charmander.admin/delete-user (:uid prep)))))))

(deftest test-set-phone-number-2
	(testing "Testing the setting of user phone number"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-phone-number (:uid prep) "")]
						(do
							(is (contains? response :error ))
							(#'charmander.admin/delete-user (:uid prep))))))))

(deftest test-set-photo-url-1
	(testing "Testing the setting of user photo url"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-photo-url (:uid prep) "https://www.domain.com/pic.jpg")]
						(do
							(is (= (:uid response) (:uid prep)))
							(is (= (:photo-url response) "https://www.domain.com/pic.jpg")))
							(#'charmander.admin/delete-user (:uid prep)))))))

(deftest test-set-photo-url-2
	(testing "Testing the setting of user photo url"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-photo-url (:uid prep) "domain.com/pic.jpg")]
						(do
							(is (contains? response :error ))
							(#'charmander.admin/delete-user (:uid prep))))))))

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
							(is (contains? response :error ))
							(#'charmander.admin/delete-user (:uid prep))
							(#'charmander.admin/delete-user (:uid prep2))))))))

(deftest test-set-user-email-3
	(testing "Testing the setting of user email"
			(let [unique (str (uuid/v1)) unique2 (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-email (:uid prep) (str unique2))]
						(do
							(is (contains? response :error ))
							(#'charmander.admin/delete-user (:uid prep))))))))

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
							(is (contains? response :error ))
							(#'charmander.admin/delete-user (:uid prep))))))))

(deftest test-set-password-3
	(testing "Testing the  setting of user password"
			(let [unique (str (uuid/v1))]
				(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
					(let [response (#'charmander.admin/set-user-password (:uid prep) "123")]
						(do
							(is (contains? response :error ))
							(#'charmander.admin/delete-user (:uid prep))))))))

(deftest test-email-verification
	(testing "Testing the  setting of user password"
		(let [unique (str (uuid/v1))]
			(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
				(do
					(is (str/includes? (#'charmander.admin/generate-email-verification-link (str unique "@domain.com")) "https://"))
					(#'charmander.admin/delete-user (:uid prep)))))))

(deftest test-email-verification-2
	(testing "Testing the  setting of user password"
		(let [unique (str (uuid/v1))]
			(let [response (#'charmander.admin/generate-email-verification-link (str unique ""))]
				(is (contains? response :error))))))
				
(deftest test-password-reset
	(testing "Testing the  setting of user password"
		(let [unique (str (uuid/v1))]
			(let [prep (#'charmander.admin/create-user (str unique "@domain.com") "superDuperSecure")]
				(do
					(is (str/includes? (#'charmander.admin/generate-password-reset-link (str unique "@domain.com")) "https://"))
					(#'charmander.admin/delete-user (:uid prep)))))))

(deftest test-password-reset-2
	(testing "Testing the  setting of user password"
		(let [unique (str (uuid/v1))]
			(let [response (#'charmander.admin/generate-password-reset-link (str unique ""))]
				(is (contains? response :error))))))
				
(deftest test-delete-user-error
	(testing "Testing the  setting of user password"
		(let [response (#'charmander.admin/delete-user "123abc123abcNotThere")]
			(is (contains? response :error)))))				