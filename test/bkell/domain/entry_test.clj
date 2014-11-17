(ns bkell.domain.entry-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
            [bkell.domain.test-helper :as hlp]
            [slingshot.slingshot :refer [try+ throw+]]
            [slingshot.slingshot :refer [try+ throw+]]
            [adi.core :as adi]
            [clojure.set :as set]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]

            [bkell.bkell :as bkell]
            [bkell.config :as config]
            [bkell.domain.entry :as ent]
            [bkell.domain.account :as acc]
            [bkell.spittoon :as sp]))


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
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.entry-test)

  (ns bkell.bkell)
  (require '[bkell.domain.account :as acc]
           '[bkell.domain.entry :as ent]
           '[bkell.domain.test-helper :as hlp])

  (def a1 {:name "trade-creditor"
           :type :expense
           :counterWeight :debit})

  (def a2 {:name "electricity"
           :type :asset
           :counterWeight :debit})

  (def a3 {:name "widgets"
           :type :asset
           :counterWeight :debit})

  (def accounts [a1 a2 a3])

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

  (def entry2 {:date (java.util.Date.)
              :content [{:type :credit
                         :amount 2600
                         :account "trade-creditor"}

                        {:type :debit
                         :amount 1000
                         :account "electricity"}

                        {:type :debit
                         :amount 1600
                         :account "Zzz"}]})

  (def ds (hlp/setup-db!))
  (def group-name "webkell")

  (acc/add-accounts ds group-name accounts)

  (def entry-transformed (ent/transform-entry-accounts ds group-name entry))

  (def r1 {:date #inst "2014-11-10T20:31:33.635-00:00",
           :content [{:amount 2600, :type :credit, :account 17592186045471}
                     {:amount 1000, :type :debit, :account 17592186045469}
                     {:amount 1600, :type :debit, :account 17592186045470}]})

  (ent/transform-entry-accounts ds group-name entry2)

  (def r2 {:date #inst "2014-11-10T20:38:29.774-00:00",
           :content [{:amount 2600, :type :credit, :account 17592186045471}
                     {:amount 1000, :type :debit, :account 17592186045469}
                     {:amount 1600, :type :debit, :account nil}]})

  (ent/corresponding-accounts-exist? r2)

  (ent/entry-balanced? ds group-name entry-transformed)

  (ent/add-entry ds group-name entry)

  (ent/list-entries ds group-name)

  (adi/select ds {:journal/entries '_} :ids)

  #{{:db {:id 17592186045466}, :journal {:name "generalledger"}}}
  #{{:journal {:name "generalledger"}}}


  (adi/select ds {:journal
                  {:entries '_
                   :name "generalledger"
                   :book
                   {:name "main"
                    :group/name group-name}}}
              ;;:ids
              :return {:journal {:entries {:content :checked}}})


  (def z3 #{{:journal {:entries #{{:content #{{:type :credit,
                                                :amount 2600.0}
                                               {:type :debit,
                                                :amount 1600.0}
                                               {:type :debit,
                                                :amount 1000.0}},
                                    :date #inst "2014-11-11T01:07:52.113-00:00"}},
                        :name "generalledger"}}})


  (def z2 #{{:db {:id 17592186045466},
              :journal {:entries #{{:+ {:db {:id 17592186045473}},
                                    :content #{{:+ {:db {:id 17592186045474}},
                                                :type :debit,
                                                :amount 1000.0}
                                               {:+ {:db {:id 17592186045475}},
                                                :type :debit,
                                                :amount 1600.0}
                                               {:+ {:db {:id 17592186045476}},
                                                :type :credit, :amount 2600.0}},
                                    :date #inst "2014-11-11T01:07:52.113-00:00" },
                                   :name "generalledger"}}}})


  (def z1 #{{:db {:id 17592186045466},
             :journal {:entries #{{:+ {:db {:id 17592186045473}},
                                   :date #inst "2014-11-11T01:07:52.113-00:00"}},
                       :name "generalledger"}}})

  )
