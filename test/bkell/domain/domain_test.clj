(ns bkell.domain.domain-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
            [slingshot.slingshot :refer [try+ throw+]]
            [spyscope.core :as spy]
            [slingshot.slingshot :refer [try+ throw+]]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]

            [bkell.bkell :as bkell]
            [bkell.config :as config]
            [bkell.domain.domain :as dm]
            [bkell.spittoon :as sp]))

(def env (:test (config/load-edn "config.edn")))

(defn gen-test-config []
  {:bkell {}
   :spittoon {:env env :recreate? true}})

(defn fixture-db-setup [f]
  (bkell/start (gen-test-config))
  (f))

(use-fixtures :each fixture-db-setup)


(defn account-generator []
  (gen/hash-map :name gen/string-ascii
                :type (gen/elements [:asset :liability :revenue :expense])
                :counterWeight (gen/elements [:debit :credit])))

(defspec add-an-account
  10
  (prop/for-all [account (account-generator)]

                (let [ds (-> bkell/system :spittoon :db)
                      group-name "webkell"
                      result (dm/add-account ds group-name account)]

                  (and (-> result nil? not)
                       (some #{:counterWeight :name :type}
                             (-> result first :book :accounts first keys sort))))))

#_(defspec add-duplicate-account
  10
  (prop/for-all [account (account-generator)]

                (let [ds (-> bkell/system :spittoon :db)
                      group-name "webkell"]

                  #spy/p

                  (dm/add-account ds group-name account)

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys (try+ (dm/add-account ds group-name account)
                                       (catch [:type :duplicate-error] _))))))))

#_(defspec addaccount-goesto-correctgroup
    10
    (prop/for-all [account (account-generator)]))

#_(defspec disallow-duplicate-accounts
    10
    (prop/for-all [account (account-generator)]))


(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.domain-test))
