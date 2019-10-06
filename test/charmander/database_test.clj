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

(deftest test-create-read-and-delete-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object"} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/get-object (str path "/" control) channel)]
            (let [result (async/<!! channel)]
              (is (= result control-data))
              (charm-db/delete-object path)
              (charm-db/get-object path channel)
              (is (= false (async/<!! channel)))))))) ;we place false on the channel to signify nothing was found

(deftest test-update-read-and-delete-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
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

(deftest test-set-read-and-delete-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object"} 
          control-data-2 {:surname "Overwritten Object"} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/get-object (str path "/" control) channel)]
            (let [result (async/<!! channel)]
              (is (= result control-data))
              (charm-db/set-object (str path "/" control) control-data-2)
              (charm-db/get-object (str path "/" control) channel)
              (let [new-result (async/<!! channel)]
                (is (= new-result control-data-2))
                (is (nil? (:name new-result)))
                (is (contains? new-result :surname))
                (is (not= new-result result))
                (is (= (:surname control-data-2) (:surname new-result)))
                (charm-db/delete-object path)
                (charm-db/get-object (str path "/" control) channel)
                (is (= false (async/<!! channel)))))))))


(deftest create-and-get-children
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel)]
            (let [result-list (repeatedly 3 #(async/<!! channel))]
                (is (= control-data  (nth result-list 0)))
                (is (= control-data2 (nth result-list 1)))
                (is (= control-data3 (nth result-list 2)))
                (charm-db/delete-object path))))))

(deftest create-and-get-children-order-by-child
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place")]
            (let [result-list (repeatedly 3 #(async/<!! channel))]
                (is (= control-data  (nth result-list 2)))
                (is (= control-data2 (nth result-list 0)))
                (is (= control-data3 (nth result-list 1)))
                (charm-db/delete-object path))))))

(deftest create-and-get-children-equal-to
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place" :equal-to 2)]
            (let [only-value (async/<!! channel)
                  result-len (.count (.buf channel))]
                (is (= control-data3 only-value))
                (is (= 0 result-len))
                (charm-db/delete-object path))))))       

(deftest create-and-get-children-equals
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place" :equals 2)]
            (let [only-value (async/<!! channel)
                  result-len (.count (.buf channel))]
                (is (= control-data3 only-value))
                (is (= 0 result-len))
                (charm-db/delete-object path))))))                            

(deftest create-and-get-children-start-at
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place" :start-at 2)]
            (let [only-value (async/<!! channel)
                  result-len (.count (.buf channel))]
                (is (= control-data3 only-value))
                (is (= 1 result-len))
                (charm-db/delete-object path))))))                                

(deftest create-and-get-children-end-at
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place" :end-at 1)]
            (let [only-value (async/<!! channel)
                  result-len (.count (.buf channel))]
                (is (= control-data2 only-value))
                (is (= 0 result-len))
                (charm-db/delete-object path))))))   

(deftest create-and-get-children-order-by-key
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-key true)]
            (let [result-list (repeatedly 3 #(async/<!! channel))]
                (is (= control-data  (nth result-list 0)))
                (is (= control-data2 (nth result-list 1)))
                (is (= control-data3 (nth result-list 2)))
                (charm-db/delete-object path))))))                                              

(deftest create-and-get-children-order-by-key-2
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          data {:ant 3 :bee 2 :cricket  1 :dungbeetle 4}
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path data)
            _ (charm-db/get-children (str path "/" control) channel :order-by-key true)]
            (let [result-list (repeatedly 4 #(async/<!! channel))]
                (is (= (vals (into (sorted-map) data)) result-list))
                (charm-db/delete-object path))))))

(deftest create-and-get-children-limit-to-first
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place" :limit-to-first 2)]
            (let [first-value (async/<!! channel)
                  result-len (.count (.buf channel))]
                (is (= control-data2 first-value))
                (is (= 1 result-len))
                (charm-db/delete-object path))))))                  

(deftest create-and-get-children-limit-to-last
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object 1" :place 3}
          control-data2 {:name "Real Object 2" :place 1}
          control-data3 {:name "Real Object 3" :place 2} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data)
            control2 (charm-db/push-object path control-data2)
            control3 (charm-db/push-object path control-data3)       
            _ (charm-db/get-children path channel :order-by-child "place" :limit-to-last 2)]
            (let [first-value (async/<!! channel)
                  result-len (.count (.buf channel))]
                (is (= control-data3 first-value))
                (is (= 1 result-len))
                (charm-db/delete-object path))))))                                  

(deftest create-and-get-children-order-by-value
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          data {:ant 3 :bee 2 :cricket  1 :dungbeetle 4}
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path data)
            _ (charm-db/get-children (str path "/" control) channel :order-by-value true)]
            (let [result-list (repeatedly 4 #(async/<!! channel))]
                (is (= (sort (vals data)) result-list))
                (charm-db/delete-object path))))))

(deftest test-listen-to-object
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object"} 
          control-data2 {:name "Fake Object" :hoax true} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/listen-to-object (str path "/" control) channel)]
            (let [result (async/<!! channel)]
              (is (= result control-data))
              (is (= 0 (.count (.buf channel))))
              (charm-db/update-object (str path "/" control) control-data2)
              (let [new-result (async/<!! channel)]
                (is (= new-result control-data2))
                (is (not= new-result result))
                (charm-db/delete-object path)
                (charm-db/get-object path channel)
                (is (= false (async/<!! channel)))))))))

(deftest test-listen-to-child-added
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object"} 
          control-data2 {:name "Fake Object" :hoax true} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/listen-to-child-added path channel)]
            (let [result (async/<!! channel)]
              (is (= result control-data))
              (is (= 0 (.count (.buf channel))))
              (charm-db/push-object path control-data2)
              (let [new-result (async/<!! channel)]
                (is (= new-result control-data2))
                (is (not= new-result result))
                (charm-db/delete-object path)))))))

(deftest test-listen-to-child-changed
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object"} 
          control-data2 {:name "Fake Object" :hoax true} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/listen-to-child-changed path channel)]
              (is (= 0 (.count (.buf channel))))
              (charm-db/set-object (str path "/" control) control-data2)
              (let [new-result (async/<!! channel)]
                (is (= new-result control-data2))
                (charm-db/delete-object path))))))

(deftest test-listen-to-child-removed
  (testing "Testing create and reading object in Realtime Database"
    (let [path (str "testing/" (uuid/v1) "/" (uuid/v4)) 
          control-data {:name "Real Object"} 
          channel (async/chan (async/buffer 48))]
      (let [control (charm-db/push-object path control-data) 
            _ (charm-db/listen-to-child-removed path channel)]
              (is (= 0 (.count (.buf channel))))
              (charm-db/delete-object (str path "/" control))
              (let [new-result (async/<!! channel)]
                (is (= new-result control-data))
                (charm-db/delete-object path))))))                
                           