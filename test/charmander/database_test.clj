(ns charmander.database-test
  (:require [clojure.test :refer :all]
  					[clojure.string :as str]
						[clojure.pprint :as pp]
						[clj-uuid :as uuid]
						[charmander.admin :as charm-admin]
  				  [charmander.database :as charm-db])
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

;Test fixtures
(defn firestore-fixture [f]
	(charm-admin/init "stub-web")
	(f))

(use-fixtures :once firestore-fixture)

; Tests for the Realtime Database SDK

(deftest test-create-and-read-object
		(testing "Testing create and reading object in Realtime Database"
			(let [path (str "testing/" (uuid/v1) "/" (uuid/v1)) control-data {:name "Real Object"}]
        (let [control (charm-db/push-object path {:name "Document"}) 
              result (charm-db/get-object (str path))]
              (println control)
              (println result)
              (charm-db/delete-object path)
              (is (nil? (charm-db/get-object path)))))))
