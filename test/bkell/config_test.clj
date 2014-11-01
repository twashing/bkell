(ns bkell.config-test
  (:require [bkell.config :as config]
            [midje.sweet :refer :all]
            [midje.repl]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(defspec test-get-config
  100
  (prop/for-all [_ gen/int]

                (= '(:host :db-schema-file :db-default-file :db-url)
                   (keys (config/get-config :test)))))

(defspec test-get-project-config
  100
  (prop/for-all [_ gen/int]

                (= '(:project-info :host :db-schema-file :db-default-file :db-url)
                   (keys (config/get-project-config :test)))))

(comment
  (midje.repl/autotest))
