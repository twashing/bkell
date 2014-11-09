(ns bkell.domain.domain
  (:require [adi.core :as adi]))


(defn add-account [ds group-name account]
  (adi/update! ds
               {:book
                {:name "main"
                 :group/name group-name}}
               {:book/accounts account}))

(defn add-entry [ds group-name entry]
  (adi/update! ds
               {:journal
                {:db/id [[:temp-journal-id]]
                 :name "generalledger"
                 :book
                 {:name "main"
                  :group/name group-name}}}
               {:journal/entries (assoc entry :journal [[:temp-journal-id]])}))
