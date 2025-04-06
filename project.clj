(defproject alekcz/charmander "1.0.6"
  :description "Charmander: a set of libraries to make working with firebase easier in clojure"
  :url "https://github.com/alekcz/charmander"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.12.0"]
                  [clj-http "3.13.0"]
                  [metosin/jsonista "0.3.13"]
                  [buddy/buddy-sign "3.6.1-359"]
                  [buddy/buddy-core "1.12.0-430"]
                  [base64-clj "0.1.1"]
                  [overtone/at-at "1.4.65"]
                  [com.google.errorprone/error_prone_annotations "2.37.0"]
                  [io.grpc/grpc-netty-shaded "1.71.0" :exclusions [com.google.errorprone/error_prone_annotations io.grpc/grpc-core]]
                  [io.grpc/grpc-protobuf "1.71.0"]
                  [io.grpc/grpc-stub "1.71.0"]
                  [io.grpc/grpc-api "1.71.0"]
                  [io.grpc/grpc-core "1.71.0" :exclusions [com.google.errorprone/error_prone_annotations io.grpc/grpc-api]]
                  [com.google.cloud/google-cloud-firestore "3.30.12" :exclusions [io.grpc/grpc-netty-shaded io.grpc/grpc-core io.grpc/grpc-api]]
                  [com.google.firebase/firebase-admin "9.4.3" :exclusions [[com.google.guava/guava-jdk5] [com.google.cloud/google-cloud-firestore]]]
                  ;[com.google.auth/google-auth-library-oauth2-http "0.16.2"]
                  [environ "1.2.0"]
                  [danlentz/clj-uuid "0.2.0"]
                  [org.clojure/core.async "1.8.735"]
                  [org.slf4j/slf4j-log4j12 "2.0.17"]
                  [criterium "0.4.6"]
                  [com.taoensso/tufte "2.6.3"]
                  [alekcz/googlecredentials "3.0.1"]]
  :javac-options ["--release" "8" "-g"]                
  :repl-options {:init-ns charmander.database}       
  :plugins [[lein-ancient "0.6.15"]
            [lein-cloverage "1.1.2"]])
