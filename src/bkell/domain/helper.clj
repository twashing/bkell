(ns bkell.domain.helper
  (:require [adi.core :as adi]
            [clojure.set :as set]))

(defn find-by-id [ds id]
  (adi/select ds id :first))

(defn find-country-by-code
  ([ds code]
     (find-country-by-code ds code [:ids]))

  ([ds code opts]
     (let [args [ds {:country {:id code}}]
           argsF (set/union args opts)]

       (apply adi/select argsF))))

(defn find-currency-by-code
  ([ds code]
     (find-currency-by-code ds code [:ids]))

  ([ds code opts]
     (let [args [ds {:currency {:id code}}]
           argsF (set/union args opts)]

       (apply adi/select argsF))))
