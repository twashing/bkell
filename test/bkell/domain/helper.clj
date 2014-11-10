(ns bkell.domain.helper
  (:require [adi.core :as adi]))


(defn setup-db! []
  (let [schema-bkell (read-string (slurp "resources/db/schema-adi.edn"))
        data-bkell (read-string (slurp "resources/db/default.edn"))

        ds (adi/connect! "datomic:mem://adi-examples-bkell" schema-bkell true true)
        _ (adi/insert! ds data-bkell)]
    ds))
