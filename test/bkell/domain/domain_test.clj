(ns bkell.domain.domain-test
  (:require [midje.sweet :refer :all]
            [slingshot.slingshot :refer [try+ throw+]]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]

            [bkell.domain.domain :as dm]
            [bkell.spittoon :as sp]))


(def env (:test (config/load-edn "config.edn")))

(defn fixture-db-setup [f]
  (sp/db-create env)
  (sp/db-init env)
  (f))

(use-fixtures :each fixture-db-setup)


(defn account-generator []
  (gen/hash-map :name gen/string
                :type (gen/elements [:asset :liability :revenue :expense])
                :counterweight (gen/elements [:debit :credit])))

(defspec add-an-account
  10
  (prop/for-all [account (account-generator)]

                (let [_ (dm/add-account group-name account)
                      a (try+ (dm/add-account group-name account)
                              (catch [:type :duplicate-account]))])

                (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                   (sort (keys a)))))


#_(defspec disallow-duplicate-accounts
  10
  (prop/for-all [account (account-generator)]))
