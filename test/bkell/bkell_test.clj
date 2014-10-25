(ns bkell.bkell-test
  (:require [bkell.bkell :as bkell]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [adi.utils :refer [iid ?q]]

            [midje.repl]))


(defspec started-system-has-expected-components
  10
  (prop/for-all [_ gen/int]

                (bkell/start)
                (= '(:bkell :spittoon) (keys bkell/system))))

(defspec stopped-system-is-nil
  10
  (prop/for-all [_ gen/int]

                (let [_ (bkell/start)
                      _ (bkell/stop)]
                  (= nil (-> bkell/system :spittoon :db)))))


(comment
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts))
