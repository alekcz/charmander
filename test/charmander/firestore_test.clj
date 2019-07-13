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


(def batch '(	{:name "Charming" :priority 0 :fires ["Pallet Town","Vermilion City","Viridian City"]}					
							{:name "Happy" :priority 1}
							{:name "Adventurous" :priority 1}
							{:name "Regal" :priority 2}
							{:name "Mighty" :priority 3}
							{:name "Amiable" :priority 3}
							{:name "Naughty" :place 6 :fires ["Pallet Town","Pewter City"]}
							{:name "Determined" :priority 6 :fires ["Lavender Town","Fuchsia City","Saffron City"]}
							{:name "Eager" :position 6 :fires ["Pallet Town","Celadon City","Cerulean City","Cinnabar Island"]}
							{:name "Ready" :priority 10}))

(def docpath (str (uuid/v1) ""))
(def path (str docpath "/doc/" (uuid/v1)))

(defn- add-batch []
	(doseq [x batch]
		(#'charmander.firestore/create-document path (:name x) x)))

(defn- delete-batch []
	(doseq [x batch]
		(#'charmander.firestore/delete-document path (:name x))))

;Test fixtures
(defn firestore-fixture [f]
	(#'charmander.admin/init)
	(add-batch)
	(#'charmander.firestore/create-document docpath "doc" {:name "Charmander"})
	(f)
	(delete-batch))

(use-fixtures :once firestore-fixture)

; Tests for the Firestore SDK

(deftest test-create-and-read-document
		(testing "Testing create and reading documents in Firestore"
			(let [unique1 (str (uuid/v1)) unique2 (str (uuid/v1))]
				(do
					(#'charmander.firestore/create-document unique1 unique2 {:name "Document"})
					(let [docu (#'charmander.firestore/get-document unique1 unique2)]
						(is (= (:id docu) unique2))
						(is (= (-> docu :data :name) "Document"))
						(is (= (contains? docu :id) true))
						(is (= (contains? docu :data) true)))
						(#'charmander.firestore/delete-document unique1 unique2)
						(is (nil? (#'charmander.firestore/get-document unique1 unique2)))))))	

(deftest test-create-read-update-document
		(testing "Testing create and reading documents in Firestore"
			(let [unique1 (str (uuid/v1)) unique2 (str (uuid/v1))]
				(do
					(#'charmander.firestore/create-document unique1 unique2 {:name "Document"})
					(let [docu (#'charmander.firestore/get-document unique1 unique2)]
						(is (= (:id docu) unique2))
						(is (= (-> docu :data :name) "Document"))
						(is (= (contains? docu :id) true))
						(is (= (contains? docu :data) true)))
					(#'charmander.firestore/update-document unique1 unique2 {:namey "Document"})
					(let [docu (#'charmander.firestore/get-document unique1 unique2)]
						(is (= (:id docu) unique2))
						(is (= (-> docu :data :namey) "Document"))
						(is (= (-> docu :data :name) "Document"))
						(#'charmander.firestore/delete-document unique1 unique2)
						(is (nil? (#'charmander.firestore/get-document unique1 unique2))))))))	

(deftest test-create-read-set-document
		(testing "Testing create and reading documents in Firestore"
			(let [unique1 (str (uuid/v1)) unique2 (str (uuid/v1))]
				(do
					(#'charmander.firestore/create-document unique1 unique2 {:name "Document"})
					(let [docu (#'charmander.firestore/get-document unique1 unique2)]
						(is (= (:id docu) unique2))
						(is (= (-> docu :data :name) "Document"))
						(is (= (contains? docu :id) true))
						(is (= (contains? docu :data) true)))
					(#'charmander.firestore/set-document unique1 unique2 {:namey "Document"})
					(let [docu (#'charmander.firestore/get-document unique1 unique2)]
						(is (= (:id docu) unique2))
						(is (= (-> docu :data :namey) "Document"))
						(is (= (-> docu :data :name) nil))
						(#'charmander.firestore/delete-document unique1 unique2)
						(is (nil? (#'charmander.firestore/get-document unique1 unique2))))))))

(deftest test-get-document-with-subcollections
		(testing "Testing retrieving documents that has a subcollections"
			(let [docu (#'charmander.firestore/get-document docpath "doc")]
				(is (= (:id docu) "doc"))
				(is (= (-> docu :data :name) "Charmander"))
				(is (= (contains? docu :id) true))
				(is (= (contains? docu :data) true))
				(is (= (contains? (:data docu) :subcollections) true))
				(is (= (count (-> docu :data :subcollections)) 1)))))

(deftest test-get-document-and-subcollections
		(testing "Testing retrieving documents and its subcollections"
			(let [docu (#'charmander.firestore/get-document-and-subcollections docpath "doc")]
				(is (= (:id docu) "doc"))
				(is (= (-> docu :data :name) "Charmander"))
				(is (= (contains? docu :id) true))
				(is (= (contains? docu :data) true))
				(is (= (contains? (:data docu) :subcollections) true))
				(is (= (count (:data (first (-> docu :data :subcollections)))) 10)))))

(deftest test-query-no-params
		(testing "Testing a query with no parameters"
			(let [result (#'charmander.firestore/query-collection path)]
				(is (= (count result) (count batch))))))

(deftest test-query-equals-1
		(testing "Testing a query with equals"
			(let [result (#'charmander.firestore/query-collection path :where "name" :equals "Charming")]
				(is (= (count result) 1))
				(is (= (-> (first result) :data :name) "Charming")))))

(deftest test-query-equals-2
		(testing "Testing a query with equals"
			(let [result (#'charmander.firestore/query-collection path :where "name" :equals "Charizard")]
				(is (= (count result) 0)))))

(deftest test-query-equal-to-1
		(testing "Testing a query with equal-to"
			(let [result (#'charmander.firestore/query-collection path :where "name" :equal-to "Adventurous")]
				(is (= (count result) 1))
				(is (= (-> (first result) :data :name) "Adventurous")))))

(deftest test-query-less-than-1
		(testing "Testing a query with <"
			(let [value 3]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :less-than value)]
					(is (= (count result) 4))
					(is (every? #(< % 3) (for [x result] (-> x :data :priority))))))))

(deftest test-query-less-than-2
		(testing "Testing a query with <"
			(let [value -4]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :less-than value)]
					(is (= (count result) 0))))))

(deftest test-query-greater-than-1
		(testing "Testing a query with >"
			(let [value 3]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :greater-than value)]
					(is (= (count result) 2))
					(is (every? #(> % 3) (for [x result] (-> x :data :priority))))))))

(deftest test-query-greater-than-2
		(testing "Testing a query with >"
			(let [value 40]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :greater-than value)]
					(is (= (count result) 0))))))

(deftest test-query-less-than-or-equal-to-1
		(testing "Testing a query with <="
			(let [value 3]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :less-than-or-equal-to value)]
					(is (= (count result) 6))
					(is (every? #(< % 4) (for [x result] (-> x :data :priority))))))))

(deftest test-query-less-than-or-equal-to-2
		(testing "Testing a query with <="
			(let [value -4]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :less-than-or-equal-to value)]
					(is (= (count result) 0))))))
					
(deftest test-query-greater-than-or-equal-to-1
		(testing "Testing a query with >="
			(let [value 3]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :greater-than-or-equal-to value)]
					(is (= (count result) 4))
					(is (every? #(> % 2) (for [x result] (-> x :data :priority))))))))

(deftest test-query-greater-than-or-equal-to-2
		(testing "Testing a query with >="
			(let [value 40]
				(let [result (#'charmander.firestore/query-collection path :where "priority" :greater-than-or-equal-to value)]
					(is (= (count result) 0))))))
					
(deftest test-query-between-1
		(testing "Testing a query with < x <"
			(let [value1 2 value2 3]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :between [value1 value2])]
						(is (= (count result) 0))))))

(deftest test-query-between-2
		(testing "Testing a query with < x <"
			(let [value1 2 value2 3]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :between [value1 value2] :include-lower false)]
						(is (= (count result) 0))))))						

(deftest test-query-between-3
		(testing "Testing a query with < x <"
			(let [value1 2 value2 3]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :between [value1 value2] :include-upper false)]
						(is (= (count result) 0))))))						

(deftest test-query-between-4
		(testing "Testing a query with <= x <"
			(let [value1 2 value2 3]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :between [value1 value2] :include-lower true)]
						(is (= (count result) 1))
						(is (every? #(> % 1) (for [x result] (-> x :data :priority))))))))

(deftest test-query-between-5
		(testing "Testing a query with < x <="
			(let [value1 2 value2 3]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :between [value1 value2] :include-upper true)]
						(is (= (count result) 2))
						(is (every? #(< % 4) (for [x result] (-> x :data :priority))))))))						

(deftest test-query-between-6
		(testing "Testing a query with < x <"
			(let [value1 2 value2 7]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :between [value1 value2])]
						(is (= (count result) 3))
						(is (every? #(< % 7) (for [x result] (-> x :data :priority))))
						(is (every? #(> % 2) (for [x result] (-> x :data :priority))))))))				

(deftest test-query-from-1
		(testing "Testing a query with <= x <="
			(let [value1 2 value2 6]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :from [value1 value2])]
						(is (= (count result) 4))
						(is (every? #(< % 7) (for [x result] (-> x :data :priority))))
						(is (every? #(> % 1) (for [x result] (-> x :data :priority))))))))				

(deftest test-query-from-2
		(testing "Testing a query with <= x <="
			(let [value1 -40 value2 -30]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :from [value1 value2])]
						(is (= (count result) 0))))))

(deftest test-query-from-3
		(testing "Testing a query with <= x <="
			(let [value1 -40 value2 40]
					(let [result (#'charmander.firestore/query-collection path :where "priority" :from [value1 value2])]
						(is (= (count result) 8))
						(is (every? #(< % 41) (for [x result] (-> x :data :priority))))
						(is (every? #(> % -41) (for [x result] (-> x :data :priority))))))))														

(deftest test-query-contains
		(testing "Testing a query with array-contains"
			(let [value "Lavender Town"]
					(let [result (#'charmander.firestore/query-collection path :where "fires" :contains value)]
						(is (= (-> (first result) :data :name) "Determined"))
						(is (= (count result) 1))))))		

(deftest test-query-contains-2
		(testing "Testing a query with array-contains"
			(let [value "Pallet Town"]
					(let [result (#'charmander.firestore/query-collection path :where "fires" :contains value)]
						(is (= (count result) 3))))))	

(deftest test-query-contains-3
		(testing "Testing a query with array-contains"
			(let [value "Cerulean City"]
					(let [result (#'charmander.firestore/query-collection path :where "fires" :contains value)]
						(is (= (-> (first result) :data :name) "Eager"))
						(is (= (count result) 1))))))							

(deftest test-query-contains-4
		(testing "Testing a query with array-contains"
			(let [value "Vermilion City"]
					(let [result (#'charmander.firestore/query-collection path :where "fires" :contains value)]
						(is (= (-> (first result) :data :name) "Charming"))
						(is (= (count result) 1))))))		

(deftest test-query-contains-5
		(testing "Testing a query with array-contains"
			(let [value "Violet City"]
					(let [result (#'charmander.firestore/query-collection path :where "fires" :contains value)]
						(is (= (count result) 0))))))		

;(pp/pprint (#'charmander.firestore/get-document "collection" "document"))
;(pp/pprint (#'charmander.firestore/get-document-and-subcollections "collection" "document"))
;(pp/pprint (#'charmander.firestore/get-collection "testing/document/subcollection"))
;(pp/pprint (#'charmander.firestore/query-collection "testing/document/subcollection" :where "priority" :between [1 2] :include-lower false))

;(#'charmander.firestore/set-document "collection" "document" {:namek "Document"})
;(#'charmander.firestore/update-document "collection" "document" {:namek "Documenty" :name "Document"})
;(#'charmander.firestore/delete-document "collection" "document")