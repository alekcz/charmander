# Charmander

A clojure library to make working with firebase easier

## Why?

- Authentication, email verification and password resets are tedious and hard. 
- Auth0 gets pricey as you scale
- Firebase auth has a fixed cost of 0 (for now)

## Usage

```clojure

[alekcz/charmander "0.1.0"]

;; In your ns statement:
(ns my.ns
  (:require [charmander.core :as :charm]))

```


### Validating tokens

Validates firebase tokens. 

```clojure

(charm/validate-token "firebase-project-id" fresh-token)  

;;	{
;;		:projectid "firebase-project-id", 
;;		:uid "uid", 
;;		:email "name@domain.com", 
;;		:email_verified false, 
;;		:sign_in_provider "password", 
;;		:exp 0000000000, 
;;		:auth_time 0000000000
;;	}

(charm/validate-token "firebase-project-id" stale-token)

;; nil

(charm/validate-token "wrong-firebase-project-id" fresh-token)

;; nil

(charm/validate-token "(.*)-project-ids" fresh-token)

;;	{
;;		:projectid "multiple-project-ids", 
;;		:uid "uid", 
;;		:email "name@domain.com", 
;;		:email_verified false, 
;;		:sign_in_provider "password", 
;;		:exp 0000000000, 
;;		:auth_time 0000000000
;;	}

```
## Next steps

- Increase efficiency
- Look into core.async for public key refresh
- Build up API 

## License

Copyright © 2017 Alexander Oloo

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
