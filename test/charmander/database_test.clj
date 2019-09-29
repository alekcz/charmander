(ns charmander.database-test
  (:require [clojure.test :refer :all]
  					[clojure.string :as str]
						[clojure.pprint :as pp]
						[clj-uuid :as uuid]
            [clojure.core.async :as async]
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
	(charm-db/init)
	(f))

(use-fixtures :once firestore-fixture)

; Tests for the Realtime Database SDK

(deftest test-create-and-read-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v1)) 
          control-data {:name "Real Object"} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/get-object (str path "/" control) channel)]
            (let [result (async/<!! channel)]
              (is (= result control-data))
              (charm-db/delete-object path)
              (charm-db/get-object path channel)
              (is (= false (async/<!! channel)))))))) ;we place false on the channel to signify nothing was found

(deftest test-update-and-read-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v1)) 
          control-data {:name "Real Object"} 
          control-data-2 {:name "Fake Object" :hoax true} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/get-object (str path "/" control) channel)]
            (let [result (async/<!! channel)]
              (is (= result control-data))
              (charm-db/update-object (str path "/" control) control-data-2)
              (charm-db/get-object (str path "/" control) channel)
              (let [new-result (async/<!! channel)]
                (is (= new-result control-data-2))
                (is (not= (-> new-result :hoax) (-> result :hoax)))
                (charm-db/delete-object path)
                (charm-db/get-object (str path "/" control) channel)
                (is (= false (async/<!! channel))))))))) ;we place false on the channel to signify nothing was found

(deftest test-create-and-query-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/1/" (uuid/v1)) 
          control-data {:name "Real Object" :read false} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/get-object path channel :order-by-child "read" :equals false)]
            (let [result (first (map val (async/<!! channel)))]
              (is (= result control-data))
              (charm-db/delete-object path)
              (charm-db/get-object path channel)
              (is (= false (async/<!! channel)))))))) ;we place false on the channel to signify nothing was found

(deftest create-and-query-objects
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v1)) 
          control-data {:name "Real Object" :read false}
          control-data2 {:name "Real Object 2" :read true}
          control-data3 {:name "Real Object 3" :read false} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-object path channel :order-by-child "read" :equals false)]
            (let [result (async/<!! channel)]
              (let [result-list (map val result)]
                (is (= (first result-list) control-data))
                (is (= (second result-list) control-data3))
                (is (= 2 (count result-list)))
                (charm-db/delete-object path)
                (charm-db/get-object path channel)
                (is (= false (async/<!! channel))))))))) ;we place false on the channel to signify nothing was found

