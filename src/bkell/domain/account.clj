(ns bkell.domain.account
  (:require [adi.core :as adi]))


(defn no-duplicate-account [ds group-name account]
  (let [a (adi/select ds {:account
                           {:name (:name account)
                            :book
                            {:name "main"
                             :group/name group-name}}}
                      :ids)]
    (empty? a)))

(defn no-duplicate-accounts [ds group-name accounts]
  (let [results (reduce (fn [rslt ech]
                          (conj rslt (no-duplicate-account ds group-name ech)))
                        []
                        accounts)]

    (every? true? results)))

(defn add-account [ds group-name account]
  {:pre [(no-duplicate-account ds group-name account)]}

  (adi/update! ds
               {:book
                {:name "main"
                 :group/name group-name}}
               {:book/accounts account}))

(defn add-accounts [ds group-name accounts]
  {:pre [(no-duplicate-accounts ds group-name accounts)
         (or (vector? accounts)
             (list? accounts))]}

  (let [account-list (into #{} accounts)]

    (adi/update! ds
                 {:book
                  {:name "main"
                   :group/name group-name}}
                 {:book/accounts account-list})))
