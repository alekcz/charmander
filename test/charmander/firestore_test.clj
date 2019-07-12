(ns charmander.firestore-test
  (:require [clojure.test :refer :all]
  					[clojure.string :as str]
						[clojure.pprint :as pp]
						[clj-uuid :as uuid]
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

;Initialise Firebase Admin
(#'charmander.admin/init)

; Tests for the Firestore SDK

(deftest test-create-and-read-document
		(testing "Testing create and reading documents in Firestore"
			(let [unique1 (str (uuid/v1)) unique2 (str (uuid/v1))]
				(do
					(#'charmander.firestore/create-document unique1 unique2 {:name "Document"})
					(let [docu (#'charmander.firestore/get-document unique1 unique2)]
						(is (= (:id docu) unique2))
						(is (= (-> docu :data :name) "Document"))
						(is (= (:names docu) nil))
						(is (= (contains? docu :id) true))
						(is (= (contains? docu :data) true)))
						(#'charmander.firestore/delete-document unique1 unique2)))))
						

;(#'charmander.firestore/create-document "collection" "document" {:name "Document"})
; (#'charmander.firestore/add-document-to-collection "testing/document/subcollection" {:name "Subdocument"})
; (#'charmander.firestore/add-document-to-collection "testing/document/subcollection" {:name "Subdocument2" :priority 1})
; (#'charmander.firestore/add-document-to-collection "testing/document/subcollection" {:name "Subdocument3" :priority 1})
; (#'charmander.firestore/add-document-to-collection "testing/document/subcollection" {:name "Subdocument4" :priority 2})
; (#'charmander.firestore/add-document-to-collection "testing/document/subcollection" {:name "Subdocument2" :priority 3})
; (#'charmander.firestore/add-document-to-collection "testing/document/subcollection" {:name "Subdocument3" :priority 3})

;(pp/pprint (#'charmander.firestore/get-document "collection" "document"))
;(pp/pprint (#'charmander.firestore/get-document-and-subcollections "collection" "document"))
(pp/pprint (#'charmander.firestore/query-collection "testing/document/subcollection"))
(println "\n\n")
(pp/pprint (#'charmander.firestore/query-collection "testing/document/subcollection" :property "priority" :value 3))

;(#'charmander.firestore/set-document "collection" "document" {:namek "Document"})
;(#'charmander.firestore/update-document "collection" "document" {:namek "Documenty" :name "Document"})
;(#'charmander.firestore/delete-document "collection" "document")