(defproject alekcz/charmander "0.4.0-delta"
  :description "Charmander: a set of libraries to make working with firebase easier in clojure"
  :url "https://github.com/alekcz/charmander"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.8.0"]
                  [http-kit "2.1.8"]
                  [cheshire "5.6.3"]
                  [buddy/buddy-sign "1.4.0"]
                  [buddy/buddy-core "1.2.0"]
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
                  [danlentz/clj-uuid "0.1.9"]]
                  
                  
  :plugins [[lein-ancient "0.6.15"]])
