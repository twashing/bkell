(ns bkell.spittoon-test
  (:require [bkell.spittoon :as spit]
            [midje.sweet :refer :all]
            [midje.repl]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [adi.utils :refer [iid ?q]]

            [bkell.bkell :as bkell]
            [bkell.config :as config]))


(defspec test-db-getconnection
  100
  (prop/for-all [_ gen/int]

                (= '(:conn :options :schema)
                   (keys (spit/db-getconnection
                           (config/get-config :test) true true)))))

(defspec test-db-setup-default
  100
  (prop/for-all [_ gen/int]

                (= '(:db-before :db-after :tx-data :tempids)
                   (spit/db-setup-default
                    (config/get-config :test)))))

(comment
  (bkell/log-info!)
  (midje.repl/autotest))
