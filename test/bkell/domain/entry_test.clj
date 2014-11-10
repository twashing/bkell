(ns bkell.domain.entry-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
            [bkell.domain.test-helper :as hlp]
            [slingshot.slingshot :refer [try+ throw+]]
            [spyscope.core :as spy]
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


(defn setup-accounts [ds group-name]

  (let [a1 {:name "trade-creditor"
            :type :expense
            :counterWeight :debit}

        a2 {:name "electricity"
            :type :asset
            :counterWeight :debit}

        a3 {:name "widgets"
            :type :asset
            :counterWeight :debit}

        accounts [a1 a2 a3]]

    (acc/add-accounts ds group-name accounts)))

(defspec test-transform-entry-accounts
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (setup-accounts ds group-name)

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

#_(defspec test-add-entry
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (setup-accounts ds group-name)]

                  (ent/add-entry ds group-name entry))))


;; test with "find-corresponding-account-byid"
;; test that entry gets into the correct group

(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.entry-test)

  (ns bkell.bkell)
  (require '[bkell.domain.account :as acc]
           '[bkell.domain.entry :as ent])

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

  (def ds (-> system :spittoon :db))
  (def group-name "webkell")

  (acc/add-accounts ds group-name accounts)

  (ent/transform-entry-accounts ds group-name entry)

  (def r1 {:date #inst "2014-11-10T20:31:33.635-00:00",
           :content [{:amount 2600, :type :credit, :account 17592186045471}
                     {:amount 1000, :type :debit, :account 17592186045469}
                     {:amount 1600, :type :debit, :account 17592186045470}]})


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

  (ent/transform-entry-accounts ds group-name entry2)

  (def r2 {:date #inst "2014-11-10T20:38:29.774-00:00",
           :content [{:amount 2600, :type :credit, :account 17592186045471}
                     {:amount 1000, :type :debit, :account 17592186045469}
                     {:amount 1600, :type :debit, :account nil}]})

  (ent/corresponding-accounts-exist? r2)
  )
