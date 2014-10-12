(ns bkell.bkell-test
  (:require [bkell.bkell :as bkell]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [adi.utils :refer [iid ?q]]

            [midje.repl]))


#_(defspec started-system-has-expected-components
  100
  (prop/for-all [_ gen/int]

                (bkell/start)
                (= '(:bkell :spittoon) (keys @bkell/system))))

;; stop

;; reset

;; spittoon - created (and loaded) database


(comment

  (midje.repl/autotest)

  )
