(defproject alekcz/charmander "0.3.0"
  :description "Charmander: a set of libraries to make working with firebase easier in clojure"
  :url "https://github.com/alekcz/charmander"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.8.0"]
                  [http-kit "2.1.18"]
                  [cheshire "5.6.3"]
                  [buddy/buddy-sign "1.4.0"]
                  [buddy/buddy-core "1.2.0"]
                  [base64-clj "0.1.1"]
                  [overtone/at-at "1.2.0"]
                  [com.google.auth/google-auth-library-oauth2-http "0.13.0"]
                  [com.google.firebase/firebase-admin "6.8.0"  :exclusions [io.grpc/grpc-core]]
                  [com.taoensso/nippy "2.14.0"] 
                  [metosin/jsonista "0.2.2"]
                  [datascript "0.18.2"]])
