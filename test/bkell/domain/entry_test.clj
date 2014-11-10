(ns bkell.domain.entry-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
            [bkell.domain.helper :as hlp]
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

(defn gen-test-config []
  {:bkell {}
   :spittoon {:env env :recreate? true}})


(defspec add-entry
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      a1 {:account "trade-creditor"
                          :type :expense
                          :counterWeight :debit}

                      a2 {:account "electricity"
                          :type :asset
                          :counterWeight :debit}

                      a3 {:account "widgets"
                          :type :asset
                          :counterWeight :debit}

                      accounts [a1 a2 a3]

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

                      _ (acc/add-accounts ds group-name accounts)]

                  (ent/add-entry ds group-name entry))))


;; test with "find-corresponding-account-byid"


#_(defspec addaccount-goesto-correctgroup
  10
  (prop/for-all [account (account-generator)]

                (let [group-name "webkell"
                      group-name-alt "guest"
                      ds (hlp/setup-db!)

                      result (acc/add-account ds group-name account)

                      result-check (adi/select ds {:account
                                                   {:name (:name account)
                                                    :book
                                                    {:name "main"
                                                     :group/name group-name-alt}}}
                                               :ids)]
                  (empty? result-check))))



(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.account-test)

  (def group-name "webkell")
  (def ds (hlp/setup-db!))

  (def a1 {:name "one" :type :asset :counterWeight :debit})
  (def a2 {:name "two" :type :asset :counterWeight :debit})
  (def accounts [a1 a2])

  (acc/no-duplicate-accounts ds group-name accounts)
  (acc/add-accounts ds group-name accounts)

  )
