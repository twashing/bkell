(ns bkell.domain.entry-test
  (:require [adi.core :as adi]
            [bkell.bkell :as bkell]
            [bkell.config :as config]
            [bkell.domain.account :as acc]
            [bkell.domain.entry :as ent]
            [bkell.domain.test-helper :as hlp]
            [bkell.spittoon :as sp]
            [clojure.set :as set]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer :all]
            [midje.repl :repl :all]
            [midje.sweet :refer :all]
            [slingshot.slingshot :refer [try+ throw+]]))


(def env (:test (config/load-edn "config.edn")))


(defspec test-transform-entry-accounts
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)

                      entry {:date (java.util.Date.)
                             :content [{:type :credit
                                        :amount 2600
                                        :account "trade-creditor"}

                                       {:type :debit
                                        :amount 1000
                                        :account "electricity"}

                                       {:type :debit
                                        :amount 1600
                                        :account "widgets"}]}

                      r1 (ent/transform-entry-accounts ds group-name entry)

                      yanked-accounts (->> r1
                                           :content
                                           (map #(:account %)))]

                  (and (every? #(not (nil? %)) yanked-accounts)
                       (every? #(not (nil? %)) (map #(acc/find-account-by-id ds group-name %)
                                                    yanked-accounts))))))

(defspec test-entry-balanced
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)

                      entry {:date (java.util.Date.)
                             :content [{:type :credit
                                        :amount 2600
                                        :account "trade-creditor"}

                                       {:type :debit
                                        :amount 1000
                                        :account "electricity"}

                                       {:type :debit
                                        :amount 1600
                                        :account "widgets"}]}

                      entry-transformed (ent/transform-entry-accounts ds group-name entry)]

                  (ent/entry-balanced? ds group-name entry-transformed))))

(defspec test-add-entry
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)

                      entry {:date (java.util.Date.)
                             :content [{:type :credit
                                        :amount 2600
                                        :account "trade-creditor"}

                                       {:type :debit
                                        :amount 1000
                                        :account "electricity"}

                                       {:type :debit
                                        :amount 1600
                                        :account "widgets"}]}]

                  (= '(:db :journal)
                     (sort (keys (first (ent/add-entry ds group-name entry))))))))

(defspec test-list-entry
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)

                      edate  (java.util.Date.)
                      entry {:date edate
                             :content [{:type :credit
                                        :amount 2600
                                        :account "trade-creditor"}

                                       {:type :debit
                                        :amount 1000
                                        :account "electricity"}

                                       {:type :debit
                                        :amount 1600
                                        :account "widgets"}]}
                      _ (ent/add-entry ds group-name entry)

                      result (ent/list-entries ds group-name)]

                  (and (= 1 (count result))

                       (= '(:content :date)
                          (sort (keys (first result))))

                       (= (sort-by :type (-> result first :content))
                          (sort-by :type #{{:type :credit, :amount 2600.0}
                                           {:type :debit, :amount 1600.0}
                                           {:type :debit, :amount 1000.0}}))))))

(comment
  (midje.repl/load-facts 'bkell.domain.entry-test))

(defspec test-add-entry-multiple
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)

                      e1 {:date (java.util.Date.)
                             :content [{:type :credit
                                        :amount 2600
                                        :account "trade-creditor"}

                                       {:type :debit
                                        :amount 1000
                                        :account "electricity"}

                                       {:type :debit
                                        :amount 1600
                                        :account "widgets"}]}

                      e2 {:date (java.util.Date.)
                             :content [{:type :credit
                                        :amount 2300
                                        :account "trade-creditor"}

                                       {:type :debit
                                        :amount 1000
                                        :account "electricity"}

                                       {:type :debit
                                        :amount 1300
                                        :account "widgets"}]}

                      _ (ent/add-entry ds group-name e1)
                      _ (ent/add-entry ds group-name e2)

                      result (ent/list-entries ds group-name)]

                  (= 2 (count result)))))

(defspec test-update-entry-balanced
  5
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      one (hlp/setup-accounts ds group-name)

                      e1 {:date (java.util.Date.)
                          :content [{:type :credit
                                     :amount 2600
                                     :account "trade-creditor"}

                                    {:type :debit
                                     :amount 1000
                                     :account "electricity"}

                                    {:type :debit
                                     :amount 1600
                                     :account "widgets"}]}

                      e2 {:date (java.util.Date.)
                          :content [{:type :credit
                                     :amount 2300
                                     :account "trade-creditor"}

                                    {:type :debit
                                     :amount 1000
                                     :account "electricity"}

                                    {:type :debit
                                     :amount 1300
                                     :account "widgets"}]}

                      r1 (ent/add-entry ds group-name e1)
                      r2 (ent/add-entry ds group-name e2)]

                  (let [rentry (-> r2 first :journal :entries first)
                        rentry2 (assoc rentry :date (java.util.Date.))

                        uentry (ent/update-entry ds group-name (:db/id rentry2) rentry2)]

                    (= '(:db :journal)
                       (sort (keys (first uentry))))))))

(defspec test-update-entry-unbalanced
  5
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      one (hlp/setup-accounts ds group-name)

                      e1 {:date (java.util.Date.)
                          :content [{:type :credit
                                     :amount 2600
                                     :account "trade-creditor"}

                                    {:type :debit
                                     :amount 1000
                                     :account "electricity"}

                                    {:type :debit
                                     :amount 1600
                                     :account "widgets"}]}

                      e2 {:date (java.util.Date.)
                          :content [{:type :credit
                                     :amount 2300
                                     :account "trade-creditor"}

                                    {:type :debit
                                     :amount 1000
                                     :account "electricity"}

                                    {:type :debit
                                     :amount 1300
                                     :account "widgets"}]}

                      r1 (ent/add-entry ds group-name e1)
                      r2 (ent/add-entry ds group-name e2)]

                  (let [rentry (-> r2 first :journal :entries first)
                        rentry2 (assoc rentry :content [{:type :credit
                                                         :amount 2300
                                                         :account "trade-creditor"}

                                                        {:type :debit
                                                         :amount 1000
                                                         :account "electricity"}

                                                        {:type :debit
                                                         :amount 1000
                                                         :account "widgets"}])

                        rbad (try+ (ent/update-entry ds group-name (:db/id rentry2) rentry2)
                                   (catch Object e e))]

                    (and (-> rbad nil? not)
                         (= rbad {:type :unbalanced-entry}))))))

(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.entry-test)

  (ns bkell.bkell)
  (require '[bkell.domain.account :as acc]
           '[bkell.domain.entry :as ent]
           '[bkell.domain.test-helper :as hlp]
           '[datomic.api :as d])


  (def entry {:date (java.util.Date.)
              :content [{:type :credit
                         :amount 2600
                         :account "trade-creditor"}

                        {:type :debit
                         :amount 1000
                         :account "electricity"}

                        {:type :debit
                         :amount 1600
                         :account "widgets"}]})


  (def gname "webkell")

  (def ds (hlp/setup-db!))
  (def accounts (hlp/setup-accounts ds gname))

  (ent/add-entry ds gname entry)

  (ent/list-entries ds gname)

  (adi/select ds {:journal
                  {:entries '_}}
              :ids)

  (adi/select ds {:journal
                  {:entries '_
                   :name "generalledger"
                   :book
                   {:name "main"
                    :group/name gname}}}
              :ids
              :pull {:journal {:entries {:content :checked}}})

  (adi/select ds {:journal
                  {:entries '_
                   :name "generalledger"
                   :book
                   {:name "main"
                    :group/name gname}}}
              :pull {:journal {:entries {:content :checked}}} :raw)

  (d/q '[:find ?self
         :where [?_ :entry/journal ?self]]
       (d/db (:connection ds)))


  (d/q '[:find (pull ?self [*])
         :where
         [?self :journal/name "generalledger"]
         [?self :journal/book ?e11095]
         [?_ :entry/journal ?self]
         [?e11095 :book/name "main"]
         [?e11095 :book/group ?e11096]
         [?e11096 :group/name "webkell"]]
       (d/db (:connection ds))))
