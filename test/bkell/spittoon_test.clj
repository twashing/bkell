(ns bkell.spittoon-test
  (:require [bkell.spittoon :as spit]
            [midje.sweet :refer :all]
            [midje.repl]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [adi.utils :refer [iid ?q]]

            [bkell.config :as config]))


#_(defspec test-db-getconnection
  100
  (prop/for-all [_ gen/int]

                (= '(:host :db-schema-file :db-default-file :db-url)
                   (config/get-config :test))))

#_(defspec test-db-setup-default
  100
  (prop/for-all [_ gen/int]

                (= '(:project-info :host :db-schema-file :db-default-file :db-url)
                   (config/get-project-config :test))))

(comment
  (midje.repl/autotest))
