(defproject org.clojars.tech-product-hp/charmander "1.0.7-unstable"
  :description "Charmander: a set of libraries to make working with firebase easier in clojure. A fork of https://github.com/alekcz/charmander"
  :url "https://github.com/hp-tech-product/charmander"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.10.0"]
                  [clj-http "3.10.1"]
                  [metosin/jsonista "0.2.2"]
                  [buddy/buddy-sign "3.1.0"]
                  [buddy/buddy-core "1.6.0"]
                  [base64-clj "0.1.1"]
                  [overtone/at-at "1.2.0"]
                  [com.google.errorprone/error_prone_annotations "2.0.2"]
                  [io.grpc/grpc-netty-shaded "1.22.1" :exclusions [com.google.errorprone/error_prone_annotations io.grpc/grpc-core]]
                  [io.grpc/grpc-protobuf "1.22.1"]
                  [io.grpc/grpc-stub "1.22.1"]
                  [io.grpc/grpc-api "1.22.1"]
                  [io.grpc/grpc-core "1.22.1" :exclusions [com.google.errorprone/error_prone_annotations io.grpc/grpc-api]]
                  [com.google.cloud/google-cloud-firestore "1.12.0" :exclusions [io.grpc/grpc-netty-shaded io.grpc/grpc-core io.grpc/grpc-api]]
                  [com.google.firebase/firebase-admin "6.8.1" :exclusions [[com.google.guava/guava-jdk5] [com.google.cloud/google-cloud-firestore]]]
                  ;[com.google.auth/google-auth-library-oauth2-http "0.16.2"]
                  [environ "1.1.0"]
                  [danlentz/clj-uuid "0.1.9"]
                  [org.clojure/core.async "0.4.500"]
                  [org.slf4j/slf4j-log4j12 "1.7.12"]
                  [criterium "0.4.5"]
                  [com.taoensso/tufte "2.1.0"]
                  [alekcz/googlecredentials "3.0.1"]]
                  
  :repl-options {:init-ns charmander.database}       
  :aot :all
  :plugins [[lein-ancient "0.6.15"]
            [lein-cloverage "1.1.2"]])
