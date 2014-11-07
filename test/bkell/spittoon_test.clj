(ns bkell.spittoon-test
  (:require [taoensso.timbre :as timbre]
            [bkell.spittoon :as sp]
            [midje.sweet :refer :all]
            [midje.repl]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [slingshot.slingshot :refer [try+ throw+]]

            [bkell.bkell :as bkell]
            [bkell.config :as config]))


(defn container-type-except-map [inner-type]
  (gen/one-of [(gen/vector inner-type)
               (gen/list inner-type)]))

(def any-except-map
  (gen/recursive-gen container-type-except-map gen/simple-type))

(def environment-mode :test)

;; env - [nil | invalid-hash-shape | valid-hash-shape]
;; :db-schema-file
;; :db-default-file - [nil | invalid-file-location | valid-file-location]
;; :db-url
(defspec test-goodinputto-dbcreate
  10
  (prop/for-all [_ gen/int]

                (let [env (environment-mode (config/load-edn "test/config.edn"))
                      schema-file "db/schema-adi.edn"]

                  (= '(:meta :connection :schema)
                     (keys (sp/db-create env schema-file))))))

(defspec test-badinputto-db-create
  10
  (prop/for-all [env any-except-map  ;; not a hash | empty hash | a hash without the keys '(:db-schema-file :db-schema-file :db-url)
                 schema-file gen/string  ;; not a string
                 ]

                (let [a (try+ (sp/db-create env schema-file)
                              (catch [:type :bad-input] e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys a))))))

(defspec test-goodinputto-db-conn
  10
  (prop/for-all [_ gen/int]

                (let [env (environment-mode (config/load-edn "test/config.edn"))
                      schema-file "db/schema-adi.edn"]

                  (= '(:meta :connection :schema)
                     (keys (sp/db-conn env schema-file))))))

(defspec test-badinputto-db-conn
  10
  (prop/for-all [env any-except-map  ;; not a hash | empty hash | a hash without the keys '(:db-schema-file :db-schema-file :db-url)
                 schema-file gen/string  ;; not a string
                 ]

                (let [a (try+ (sp/db-conn env schema-file)
                              (catch [:type :bad-input] e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys a))))))

(defspec test-goodinputto-db-init
  10
  (prop/for-all [_ gen/int]

                (let [env (environment-mode (config/load-edn "test/config.edn"))
                      schema-file "db/schema-adi.edn"

                      _ (sp/db-create env schema-file)
                      result (keys (first (sp/db-init env schema-file)))]

                  (= '(:system :db) result))))

(defspec test-badinputto-db-init
  10
  (prop/for-all [env any-except-map  ;; not a hash | empty hash | a hash without the keys '(:db-schema-file :db-schema-file :db-url)
                 schema-file gen/string  ;; not a string
                 ]

                (let [a (try+ (sp/db-init env schema-file)
                              (catch [:type :bad-input] e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys a))))))


;; (defspec test-goodinputto-db-import )
;; (defspec test-badinputto-db-import )

(comment
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts))
