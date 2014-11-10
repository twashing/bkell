(ns bkell.domain.entry
  (:require [adi.core :as adi]
            [slingshot.slingshot :refer [try+ throw+]]
            [bkell.domain.account :as acc]))


(defn entry-balanced? [ds group-name entry]
  true)

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
      (throw+ {:type :non-existant-accounts})))

)
