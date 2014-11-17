(ns bkell.domain.account
  (:require [adi.core :as adi]
            [clojure.set :as set]
            [bkell.domain.helper :as hlp]))


(declare find-account-by-name
         add-account)

(defn no-duplicate-account? [ds group-name account]
  (let [a (find-account-by-name ds group-name (:name account))]
    (empty? a)))

(defn no-duplicate-accounts? [ds group-name accounts]
  (let [results (reduce (fn [rslt ech]
                          (conj rslt (no-duplicate-account? ds group-name ech)))
                        []
                        accounts)]

    (every? true? results)))

(def account-type-mappings {:expense :debit
                            :revenue :credit
                            :liability :credit
                            :asset :debit
                            :capital :credit})

(defn create-account [ds group-name aname atype]
  {:pre [(some #{atype} (keys account-type-mappings))]}

  (let [account {:name aname
                 :type atype
                 :counterWeight (atype account-type-mappings)}]
    (add-account ds group-name account)))

(defn add-account [ds group-name account]
  {:pre [(no-duplicate-account? ds group-name account)]}

  (adi/update! ds
               {:book
                {:name "main"
                 :group/name group-name}}
               {:book/accounts account}))

(defn add-accounts [ds group-name accounts]
  {:pre [(no-duplicate-accounts? ds group-name accounts)
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


(defn find-account-by-id [ds gname aid]
  (:account (hlp/find-by-id ds aid)))

(defn list-accounts [ds gname]
  (adi/select ds {:account
                  {:book
                   {:name "main"
                    :group/name gname}}}))


(defn update-account [ds id account]

  ;; can only update if no other entries point to it
  ;; can only update :name or :type (:counterWeight is automatically changed)

  )


(comment

  (acc/find-account-by-id ds "webkell" 123)

  (adi/query ds '[:find ?self
                  :where [?self :account/name "cash"]
                  [?self :account/book ?e22978]
                  [?e22978 :book/name "main"]
                  [?e22978 :book/group ?e22979]
                  [?e22979 :group/name "webkell"]]
             [])

)
