(ns bkell.spittoon-test
  (:require [taoensso.timbre :as timbre]
            [bkell.spittoon :as spit]
            [midje.sweet :refer :all]
            [midje.repl]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [adi.utils :refer [iid ?q]]

            [bkell.bkell :as bkell]
            [bkell.config :as config]))


#_(defspec test-db-getconnection
  100
  (prop/for-all [_ gen/int]

                (= '(:conn :options :schema)
                   (keys (spit/db-getconnection
                           (config/get-config :test) true true)))))

#_(defspec test-db-setup-default
  100
  (prop/for-all [_ gen/int]

                (= '(:db-before :db-after :tx-data :tempids)
                   @(spit/db-setup-default
                    (config/get-config :test)) )))

;; env - [nil | invalid-hash-shape | valid-hash-shape]
;; :db-schema-file
;; :db-default-file - [nil | invalid-file-location | valid-file-location]
;; :db-url
(defspec test-goodinputto-dbcreate
  10
  (prop/for-all [_ gen/int]

                (let [env (:test (config/load-edn "test/config.edn"))
                      schema-file "db/schema-adi.edn"]

                  (= '(:conn :options :schema)
                     (keys (spit/db-create env schema-file))))))

(comment
  (bkell/log-info!)
  (midje.repl/autotest))
