(ns bkell.domain.domain
  (:require [adi.core :as adi]))


(defn entry-balanced? [ds group-name entry]
  )

(defn add-entry [ds group-name entry]
  {:pre [(entry-balanced? ds group-name entry)]}

  (adi/update! ds
               {:journal
                {:db/id [[:temp-journal-id]]
                 :name "generalledger"
                 :book
                 {:name "main"
                  :group/name group-name}}}
               {:journal/entries (assoc entry :journal [[:temp-journal-id]])}))


The totals of each column are posted as follows:

Amount total value 2600 posted as a credit to the Trade creditors control a/c
Electricity total value 1000 posted as a debit to the Electricity General Ledger a/c
Widget total value 1600 posted as a debit to the Widgets General Ledger a/c


{:account "trade-creditor"
 :type :expense
 :counterWeight :debit}

{:account "electricity"
 :type :asset
 :counterWeight :debit}

{:account "widgets"
 :type :asset
 :counterWeight :debit}


{:type :credit
 :amount 2600
 :account [[:trade-creditor]]}

{:type :credit
 :amount 1000
 :account [[:electricity]]}

{:type :credit
 :amount 1600
 :account [[:widgets]]}
