(ns bkell.bkell-test
  (:require [bkell.bkell :refer :all]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(defspec first-element-is-min-after-sorting
  100
  (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
                (= (apply min v)
                   (first (sort v)))))
