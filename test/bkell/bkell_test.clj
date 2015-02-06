(ns bkell.bkell-test
  (:require [bkell.bkell :as bkell]
            [bkell.config :as config]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [midje.repl]))


(defspec started-system-has-expected-components
  10
  (prop/for-all [_ gen/int]

                (bkell/start {:bkell {}
                              :spittoon {:env (:test (config/load-edn "config.edn"))
                                         :recreate? true}})
                (= '(:bkell :spittoon) (keys bkell/system))))

(defspec stopped-system-is-nil
  10
  (prop/for-all [_ gen/int]

                (let [_ (bkell/start {:bkell {}
                                      :spittoon {:env (:test (config/load-edn "config.edn"))
                                                 :recreate? true}})
                      _ (bkell/stop)]
                  (= nil (-> bkell/system :spittoon :db)))))


(comment
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts))
