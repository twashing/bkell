(ns bkell.domain.helper
  (:require [adi.core :as adi]))

(defn find-by-id [ds id]
  (adi/select ds id :first))
