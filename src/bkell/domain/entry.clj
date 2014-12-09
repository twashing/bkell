(ns bkell.domain.entry
  (:require [adi.core :as adi]
            [bkell.domain.account :as acc]
            [bkell.domain.helper :as hlp]
            [clojure.zip :as zip]
            [slingshot.slingshot :refer [try+ throw+]]))


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

                                (let [raccount (if (number? (:account ech))
                                                 #_(acc/find-account-by-id ds gname (:account ech) [:ids])
                                                 (hlp/find-by-id ds (:account ech) [:ids])
                                                 (acc/find-account-by-name ds gname (:account ech) [:ids]))]
                                  (assoc ech
                                    :account (-> raccount first :db :id)
                                    :account-counterweight (-> raccount first :account :counterWeight))))

                              (:content entry))))

(defn strip-account-counterweight [entry]
  (assoc entry :content (mapv (fn [ech]
                                (dissoc ech :account-counterweight))
                              (:content entry))))

(defn transform-entry-ids
  "Moves an entry's adi id, from A. to B.

  A.
  {:date #inst \"2014-11-24T19:28:36.386-00:00\",
   :content
   #{{:amount 2600.0,
      :type :credit,
      :account 17592186045470,
      :+ {:db {:id 17592186045476}}}
     {:amount 1000.0,
      :type :debit,
      :account 17592186045469,
      :+ {:db {:id 17592186045475}}}
     {:amount 1600.0,
      :type :debit,
      :account 17592186045471,
      :+ {:db {:id 17592186045474}}}},
   :+ {:db {:id 17592186045473}}}

  B.
  {:date #inst \"2014-11-24T19:28:36.386-00:00\",
   :content
   #{{:amount 2600.0,
      :type :credit,
      :account 17592186045470,
      :db/id 17592186045476}
     {:amount 1600.0,
      :type :debit,
      :account 17592186045471,
      :db/id 17592186045474}
     {:amount 1000.0,
      :type :debit,
      :account 17592186045469,
      :db/id 17592186045475}},
   :db/id 17592186045473}"
  [entry]
  {:pre  [ (map? entry)]}

  (loop [loc (zip/zipper map?
                         #(seq (:content %1))
                         #(assoc %1 :content (into #{} %2))
                         entry)]

    (if (zip/end? loc)
      (zip/root loc)
      (if (contains? (zip/node loc) :+)
        (recur  (zip/next
                 (zip/edit loc
                           (fn [inp]
                             (dissoc
                              (assoc inp
                                :db/id
                                (-> inp :+ :db :id))
                              :+)))))
        (recur (zip/next loc))))))

(defn transform-entries
  "This function expects a structure like:

   [{:journal {:entries
               #{{:+ {:db {:id 17592186045473}},
                  :content #{{:+ {:db {:id 17592186045475}},
                              :amount 1000.0,
                              :type :debit,
                              :account 17592186045469}
                             {:+ {:db {:id 17592186045474}},
                              :amount 1600.0,
                              :type :debit,
                              :account 17592186045471}
                             {:+ {:db {:id 17592186045476}},
                              :amount 2600.0,
                              :type :credit,
                              :account 17592186045470}},
                  :date #inst\"2014-11-24T19:28:36.386-00:00\"}}},
     :db {:id 17592186045465}}]"
  [entries]

  (map (fn [i1]
         (update-in i1
                    [:journal :entries]
                    (fn [i2]
                      (map #(transform-entry-ids %)
                           i2))))
       entries))

(defn add-entry [ds group-name entry]
  {:pre [(not (nil? group-name))
         (not (nil? entry))
         (not (nil? (:date entry)))]}

  (let [entry-transformed (transform-entry-accounts ds group-name entry)
        check-accounts-exist? (corresponding-accounts-exist? entry-transformed)
        check-entry-balanced? (entry-balanced? ds group-name entry-transformed)
        entry-final (strip-account-counterweight entry-transformed)]

    (if check-accounts-exist?
      (if check-entry-balanced?
        (transform-entries (adi/update! ds
                                         {:journal
                                          {:name "generalledger"
                                           :book
                                           {:name "main"
                                            :group/name group-name}}}
                                         {:journal/entries entry-final}))
        (throw+ {:type :unbalanced-entry}))
      (throw+ {:type :non-existant-accounts}))))


(defn find-entry-before [ds gname date]
  (adi/select ds {:journal
                  {:name "generalledger"
                   :entries {:date #{(list '.before date)}}
                   :book
                   {:name "main"
                    :group/name gname}}}
              :pull {:journal {:entries {:content :checked}}}))

(defn find-entry-after [ds gname date]
  (adi/select ds {:journal
                  {:name "generalledger"
                   :entries {:date #{(list '.after date)}}
                   :book
                   {:name "main"
                    :group/name gname}}}
              :pull {:journal {:entries {:content :checked}}}))

(defn find-entry-between [ds gname after before]
  (adi/select ds {:journal
                   {:name "generalledger"
                    :entries {:date #{(list '.after after) (list '.before before)}}
                    :book
                    {:name "main"
                     :group/name gname}}}
              :pull {:journal {:entries {:content :checked}}}))

(defn list-entries [ds gname]
  (let [result (adi/select ds {:journal
                                      {:entries '_
                                       :name "generalledger"
                                       :book
                                       {:name "main"
                                        :group/name gname}}}
                                  :pull {:journal {:entries {:content :checked}}})]

    (-> result first :journal :entries)))

(defn update-entry [ds group-name eid entry]
  {:pre [(not (nil? group-name))
         (not (nil? (hlp/find-by-id ds eid)))
         (not (nil? entry))
         (not (nil? (:date entry)))]}

  (let [
        ;; entry-transformed #spy/d (transform-entry-accounts ds group-name #spy/d entry)
        entry-transformed (transform-entry-accounts ds group-name entry)
        check-accounts-exist? (corresponding-accounts-exist? entry-transformed)
        check-entry-balanced? (entry-balanced? ds group-name entry-transformed)
        entry-final (strip-account-counterweight entry-transformed)]

    (if check-accounts-exist?
      (if check-entry-balanced?
        (adi/update! ds
                     {:journal
                      {:name "generalledger"
                       :entries {:+/db/id eid}
                       :book
                       {:name "main"
                        :group/name group-name}}}
                     {:journal/entries entry-final})
        (throw+ {:type :unbalanced-entry}))
      (throw+ {:type :non-existant-accounts}))))

(comment
  (bkell/log-debug!)
  (bkell/log-info!)

  (ns bkell.bkell)
  (require '[bkell.domain.account :as acc]
           '[bkell.domain.entry :as ent]
           '[bkell.domain.test-helper :as hlp]
           '[bkell.domain.test-helper :as thlp])

  (def group-name "webkell")
  (def ds (thlp/setup-db!))
  (def one (thlp/setup-accounts ds group-name))

  (def entry {:date #inst "2014-11-01"
              :content [{:type :credit
                         :amount 2600
                         :account "trade-creditor"}

                        {:type :debit
                         :amount 1000
                         :account "electricity"}

                        {:type :debit
                         :amount 1600
                         :account "widgets"}]})

  (ent/add-entry ds group-name entry)

  (def result ({:journal
                {:entries
                 ({:db/id 17592186045473,
                   :content #{{:db/id 17592186045476, :amount 1600.0, :type :debit, :account 17592186045470}
                              {:db/id 17592186045474, :amount 1000.0, :type :debit, :account 17592186045469}
                              {:db/id 17592186045475, :amount 2600.0, :type :credit, :account 17592186045471}},
                   :date #inst "2014-11-01T00:00:00.000-00:00"})},
                :db {:id 17592186045454}}))

  (ent/find-entry-after ds group-name #inst "2014-01-01")
  (ent/find-entry-before ds group-name #inst "2014-12-01")
  (ent/find-entry-between ds group-name #inst "2014-01-01" #inst "2014-12-01")

  entry)
