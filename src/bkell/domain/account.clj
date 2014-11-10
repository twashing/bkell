(ns bkell.domain.account
  (:require [adi.core :as adi]
            [clojure.set :as set]))


(declare find-account)

(defn no-duplicate-account [ds group-name account]
  (let [a (find-account-by-name ds group-name (:name account))]
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

(defn find-account-by-name
  ([ds gname aname]
     (find-account-by-name ds gname aname [:ids]))

  ([ds gname aname opts]
     {:pre (vector? opts)}

     (let [select-args (set/union [ds
                                   {:account
                                    {:name aname
                                     :book
                                     {:name "main"
                                      :group/name gname}}}]
                                  opts)]
       (apply adi/select select-args))))
