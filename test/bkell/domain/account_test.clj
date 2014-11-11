(ns bkell.domain.account-test
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
            [bkell.domain.account :as acc]
            [bkell.spittoon :as sp]))


(def env (:test (config/load-edn "config.edn")))

(defn account-generator []
  (gen/hash-map :name gen/string-ascii
                :type (gen/elements [:asset :liability :revenue :expense])
                :counterWeight (gen/elements [:debit :credit])))

(defspec add-an-account
  10
  (prop/for-all [account (account-generator)]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      result (acc/add-account ds group-name account)]

                  (and (set/subset? #{:counterWeight :name :type}
                                    (-> result first :book :accounts first keys set))

                       (-> result nil? not)))))

(defspec restrict-duplicate-account
  10
  (prop/for-all [account (account-generator)]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      _ (acc/add-account ds group-name account)
                      result (try+ (acc/add-account ds group-name account)
                                   (catch AssertionError e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :throwable))
                     (sort (keys result))))))

(defspec addaccount-goesto-correctgroup
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

(defspec no-duplicate-accounts
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      a1 {:name "one" :type :asset :counterWeight :debit}
                      a2 {:name "two" :type :asset :counterWeight :debit}
                      accounts [a1 a2]]

                  (acc/no-duplicate-accounts ds group-name accounts))))

(defspec add-accounts
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      a1 {:name "one" :type :asset :counterWeight :debit}
                      a2 {:name "two" :type :asset :counterWeight :debit}
                      accounts [a1 a2]

                      _ (acc/add-accounts ds group-name accounts)]

                  (let [r1 (adi/select ds {:account
                                           {:name (:name a1)
                                            :book
                                            {:name "main"
                                             :group/name group-name}}}
                                       :ids)

                        r2 (adi/select ds {:account
                                           {:name (:name a2)
                                            :book
                                            {:name "main"
                                             :group/name group-name}}}
                                       :ids)]

                    (and (-> r1 empty? not)
                         (-> r2 empty? not))))))

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

  (adi/select ds 17592186045470 :raw)
  (adi/select ds 17592186045470 :first)

  )
