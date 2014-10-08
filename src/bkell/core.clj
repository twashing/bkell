(ns bkell.core
  (:require [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(def run-one
  (prop/for-all [e (gen/vector gen/keyword)]
                (not (nil? e))))

(defn non-empty-set [elem-g]
  (gen/such-that seq (gen/fmap set (gen/vector elem-g))))


(comment

  gen/keyword

  (gen/vector gen/keyword)

  (non-empty-set gen/keyword)

  (tc/quick-check 100 run-one))
