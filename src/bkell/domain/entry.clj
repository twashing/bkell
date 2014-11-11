(ns bkell.domain.entry
  (:require [adi.core :as adi]
            [slingshot.slingshot :refer [try+ throw+]]
            [bkell.domain.account :as acc]))


(defn entry-balanced? [ds group-name entry]
  (let [result (reduce (fn [rslt ech]

                         (let [ac-weight (:account-counterweight ech)
                               se-weight (:type ech)]

                           (if (or (and (= :debit ac-weight)
                                        (= :debit se-weight))
                                   (and (= :credit ac-weight)
                                        (= :credit se-weight)))

                             ;; increase :lhs if debit(ing) a debit account OR credit(ing) a credit account
                             (merge rslt {:lhs
                                       (+ (:lhs rslt)
                                          (:amount ech))})

                             (merge rslt {:rhs
                                       (+ (:rhs rslt)
                                          (:amount ech))}))))
                       {:lhs 0.0 :rhs 0.0}   ;; beginning tally
                       (:content entry))]

    (= (:lhs result) (:rhs result))))

(defn corresponding-accounts-exist? [entry]
  (->> entry
       :content
       (mapv #(not (nil? (:account %))))
       (every? true?)))

(defn transform-entry-accounts [ds gname entry]
  (assoc entry :content (mapv (fn [ech]

                                (let [raccount (acc/find-account-by-name ds gname (:account ech) [:ids])]
                                  (assoc ech
                                    :account (-> raccount first :db :id)
                                    :account-counterweight (-> raccount first :account :counterWeight))))

                              (:content entry))))

(defn add-entry [ds group-name entry]
  {:pre [(not (nil? group-name))
         (not (nil? entry))
         (not (clojure.string/blank? (:date entry)))]}

  (let [entry-transformed (transform-entry-accounts ds group-name entry)
        check-accounts-exist? (corresponding-accounts-exist? ds group-name entry-transformed)
        check-entry-balanced? (entry-balanced? ds group-name entry-transformed)]

    (if check-accounts-exist?
      (if check-entry-balanced?
        (adi/update! ds
               {:journal
                {:name "generalledger"
                 :book
                 {:name "main"
                  :group/name group-name}}}
               {:journal/entries entry})
        (throw+ {:type :unbalanced-entry}))
      (throw+ {:type :non-existant-accounts}))))
