(ns bkell.domain.test-helper
  (:require [adi.core :as adi]
            [bkell.domain.account :as acc]
            [bkell.domain.entry :as ent]))


(defn setup-db! []
  (let [schema-bkell (read-string (slurp "resources/db/schema-adi.edn"))
        data-bkell (read-string (slurp "resources/db/default.edn"))

        ds (adi/connect! "datomic:mem://adi-examples-bkell" schema-bkell true true)
        _ (adi/insert! ds data-bkell)]
    ds))

(defn setup-accounts [ds group-name]

  (let [a1 {:name "trade-creditor"
            :type :expense
            :counterWeight :debit}

        a2 {:name "electricity"
            :type :asset
            :counterWeight :debit}

        a3 {:name "widgets"
            :type :asset
            :counterWeight :debit}

        accounts [a1 a2 a3]]

    (acc/add-accounts ds group-name accounts)))


(defn add-test-entry [ds gname]

  (let [entry {:date (java.util.Date.)
               :content [{:type :credit
                          :amount 2600
                          :account "trade-creditor"}

                         {:type :debit
                          :amount 1000
                          :account "electricity"}

                         {:type :debit
                          :amount 1600
                          :account "widgets"}]}]

    (ent/add-entry ds gname entry)))
