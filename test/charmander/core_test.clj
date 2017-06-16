(ns charmander.core-test
  (:require [clojure.test :refer :all]
  			[clojure.string :as str]
  			[buddy.sign.jwt :as jwt]
  			[overtone.at-at :as at]
            [charmander.core :refer :all]))

(def ancient-firebase-token "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU3ZGQ5ZGNmYmIxZDkzZWY2MWE1Y2Y5N2QxMjYxZjk5YTIxNWQ4YTAifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vbmVlZHR5cmVzemEiLCJhdWQiOiJuZWVkdHlyZXN6YSIsImF1dGhfdGltZSI6MTQ4OTgzMDQ5MSwidXNlcl9pZCI6Ikg1eHpTQW9nZkVOUlk4ampHbTFVS2hRVHZ5QTMiLCJzdWIiOiJINXh6U0FvZ2ZFTlJZOGpqR20xVUtoUVR2eUEzIiwiaWF0IjoxNDk3Mzk3MjA2LCJleHAiOjE0OTc0MDA4MDYsImVtYWlsIjoiYWxla2N6QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJhbGVrY3pAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.mEQHljuKO5v2c_A38zH5KqzqYU_Nq8Q3hCEiQjFag1VL32voJndece8fjfCo0dKxFCkKNoTIgMidLiMUet2aTTk89JaCfIBlKzGs3i8o5FEzDbdb1VU5KsrKbeFkCnMu7v9B8K6d5xkAnIW6JI-1wLgTVYov8RlxHhRBYjn-iNd_CKMIUvwDMaPo4kYr70IqKmK8kgCha9x9FViBCdMncc9nPvZWN-OE22Lwmk3qjHhMfuLSYBWZa_KotvHiQFEc06Mdc0vj-JtOTKSGzl4ESrnnX4QQR6lKGUqsbwqk0h61_NQd0-tlQxelMb6td8U6ISvlzufIYTj5Lx9N1bhcgw")

(comment

	"Template for tests"

	(deftest test-tempate
		(testing "Testing functionname"
			(let [data "" other ""]
				(do
					(is (= (#'charmander.core/privatefunction inputs) answer))
					(is (= 1 (- 2 1)))))))
)

(deftest test-update-public-keys
	(testing "Testing update-public-keys"
		(let [	pubkey (atom nil) 
				datastring "{\"data\":\"pew pew\", \"status\":\"bang bang\"}" 
				dataset {:data "pew pew" :status "bang bang"}]
					(do
						(is (= (#'charmander.core/update-public-keys pubkey datastring) dataset))
						(is (= @pubkey dataset))))))


(deftest test-pad-token
	(testing "Testing pad-token"
		(let [	zero "" one "1" two "12" three "123" four "1234" 
				five "12345" six "123456" seven "1234567" eight "1234567"]
					(do
						(is (= (mod (count (#'charmander.core/pad-token zero)) 4) 0))
						(is (= (mod (count (#'charmander.core/pad-token one)) 4) 0))
						(is (= (mod (count (#'charmander.core/pad-token two)) 4) 0))
						(is (= (mod (count (#'charmander.core/pad-token three)) 4) 0))
						(is (= (mod (count (#'charmander.core/pad-token four)) 4) 0))
						(is (= (mod (count (#'charmander.core/pad-token five)) 4) 0))
						(is (= (mod (count (#'charmander.core/pad-token six)) 4) 0))
						(is (= (mod(count (#'charmander.core/pad-token seven)) 4)  0))
						(is (= (mod (count (#'charmander.core/pad-token eight)) 4) 0))
						(is (= (#'charmander.core/pad-token zero) (str zero "")))
						(is (= (#'charmander.core/pad-token one) (str one "===")))
						(is (= (#'charmander.core/pad-token two) (str two "==")))
						(is (= (#'charmander.core/pad-token three) (str three "=")))
						(is (= (#'charmander.core/pad-token four)  four))))))


(deftest test-get-token-header
	(testing "Testing get-token-header"
		(let [token ancient-firebase-token]
				(let [header (#'charmander.core/get-token-header token)]
					(do
						(is (contains? header :alg))
						(is (contains? header :kid)))))))



(deftest test-schedule-public-key-update
	(testing "Testing schedule-public-key-update"
		(let [threadpool (at/mk-pool) header {:cache-control "public, max-age=1000, pew pew"}]
			(do
				(#'charmander.core/schedule-public-key-update threadpool header)
				(is (= (count (at/scheduled-jobs threadpool)) 1))))))


(deftest test-verify-domain
	(testing "Testing verify-domain"
		(let [	data {:projectid "pew pew 12356"}
				one "pew (.*)" two "pew pew \\d+" three "not here" four "(.*) pew \\d+" five "pew pew 12356"]
			(do
				(is (= (#'charmander.core/verify-domain one data) data))
				(is (= (#'charmander.core/verify-domain two data) data))
				(is (nil? (#'charmander.core/verify-domain three data)))
				(is (= (#'charmander.core/verify-domain four data) data))
				(is (= (#'charmander.core/verify-domain five data) data))))))

				
(deftest test-validate-token
	(testing "Testing validate-token"
		(let [token ancient-firebase-token]
			(do
				(is (nil? (charmander.core/validate-token  "(.*)" token)))))))
				

; A beter and more comprehensive test is needed for `validate-token`

; Still need to develop test for `format-result` and `authenticate`

