(ns charmander.benchmark-test
  (:require [clojure.test :refer :all]
  			[clojure.string :as str]
  			[charmander.core :as charm]
			[environ.core :refer [env]]
			[charmander.admin :as charm-admin]
            [charmander.database :as charm-db]
            [charmander.firestore :as charm-store]
			[clj-uuid :as uuid]
			[jsonista.core :as json]
            [criterium.core :as criterium]
            [taoensso.tufte :as tufte :refer (defnp p profiled profile)]))

(tufte/add-basic-println-handler! {})

(defn performance-database []
	(let [	len 10000
            path "perf-testing"
			ran {:names ["Pew pew" "Pew"]}
			result (atom [])
            control (uuid/v1)]			
		 (doall 
            (map
		 	    #(charm-db/set-object (str path "/" control "/" (uuid/v1)) {:name %})
                (range len)))))
        ;(charm-db/set-object (str path "/" control "/" (uuid/v1)) ran)))
        ; (while (not= len (count @result)) (do))
		; (doall 
        ;     (map
		; 	    #(while (false? (.isDone %)) (do))  
        ;         @result))))

(defn get-x [] (Thread/sleep 500)             "x val")
(defn get-y [] (Thread/sleep (rand-int 1000)) "y val")

(defn write-in []
    (do
        (charm-db/set-object (str "perf-testing/" (uuid/v1) "/" (uuid/v1)) {:name 1})
        []))
        

(defn -main [& args]
    (charm-admin/init)
    (println "starting benchmark")
    (time (performance-database))
    ; (do
    ;     (profile ; Profile any `p` forms called during body execution
    ;         {} ; Profiling options; we'll use the defaults for now
    ;         (dotimes [_ 2]
    ;             (doall 
    ;                 (p :performance-database (performance-database))
    ;                 (Thread/sleep 3000))))
    ;             ;(doall (p :write-in (write-in)))))
    ;     ;; How do these fns perform? Let's check:

    ;     (profile ; Profile any `p` forms called during body execution
    ;         {} ; Profiling options; we'll use the defaults for now
    ;         (dotimes [_ 5]
    ;             (p :get-x (get-x))
    ;             (p :get-y (get-y)))))
	; ;(time (performance-sample-small))
    ; ; (println "Starting 1000 x 1KB")
	; ; (criterium/quick-bench (performance-sample-large))
    ; ; (println "Starting 10000 x 20B")
    ; ; (criterium/quick-bench (performance-sample-small))
    ; ; (charm-db/delete-object (str "firestream/performance/perf1"))
    ; ; (charm-db/delete-object (str "firestream/performance/perf2"))
    (println "donezo"))