(ns bkell.domain.account
  (:require [adi.core :as adi]))


(defn no-duplicate-accounts [ds group-name account]
  (let [a (adi/select ds {:account
                           {:name (:name account)
                            :book
                            {:name "main"
                             :group/name group-name}}}
                      :ids)]
    (empty? a)))

(defn add-account [ds group-name account]
  {:pre [(no-duplicate-accounts ds group-name account)]}

  (adi/update! ds
               {:book
                {:name "main"
                 :group/name group-name}}
               {:book/accounts account}))

