(ns charmander.firestore-test
  (:require [clojure.test :refer :all]
  					[clojure.string :as str]
						[clojure.pprint :as pp]
  				  [charmander.firestore :refer :all])
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
;(#'charmander.firestore/create-document "collection" "document" {:name "Document"})
;(#'charmander.firestore/add-document-to-collection "collectionor/document/subcollection" {:name "Subdocument"})

;(pp/pprint (#'charmander.admin/get-document "collection" "document"))
;(pp/pprint (#'charmander.admin/get-document-and-subcollections "collection" "document"))
;(pp/pprint (#'charmander.admin/get-collection "collection/document/subcollection"))

;(#'charmander.admin/set-document "collection" "document" {:namek "Document"})
;(#'charmander.admin/update-document "collection" "document" {:namek "Documenty" :name "Document"})
;(#'charmander.admin/delete-document "collection" "document")