(ns charmander.admin-test
  (:require [clojure.test :refer :all]
  					[clojure.string :as str]
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

; Tests for the Admin SDK

(deftest test-build-firebase-options
		(testing "Testing Firebase Options Builder"
			(let [file "resources/test/test-key.json" database-name "project_id"]
				(let [options (#'charmander.admin/build-firebase-options file database-name)]
					(do
						(is (= (. options getDatabaseUrl) "https://project_id.firebaseio.com"))
						(is (= (type options) com.google.firebase.FirebaseOptions)))))))

;(deftest test-validate-service-key)

;(#'charmander.admin/init "firebaseKey.json" "alekcz-dev")
;(println (#'charmander.admin/delete-user "vMnMJvS28kWr5pb6sByHULMLelJ3"))
;(println (#'charmander.admin/create-user "email@domain.com" "superDuperSecure"))
;(println (#'charmander.admin/get-user "foHCpMoaT7P3WXeBTgWR261Z2mX2"))
;(println (#'charmander.admin/get-user-by-email "email@domain.com"))
;(println (#'charmander.admin/set-user-display-name "foHCpMoaT7P3WXeBTgWR261Z2mX2" "email"))
;(println (#'charmander.admin/set-user-password "foHCpMoaT7P3WXeBTgWR261Z2mX2" "superDuperExtra53cur3"))
