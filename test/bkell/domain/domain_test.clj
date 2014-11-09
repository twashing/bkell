(ns bkell.domain.domain-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            #_[clojure.test :refer :all]
            #_[slingshot.slingshot :refer [try+ throw+]]
            #_[spyscope.core :as spy]
            #_[slingshot.slingshot :refer [try+ throw+]]

            #_[clojure.test.check.clojure-test :refer :all]
            #_[clojure.test.check.generators :as gen]
            #_[clojure.test.check.properties :as prop]

            #_[bkell.bkell :as bkell]
            #_[bkell.config :as config]
            #_[bkell.domain.domain :as dm]
            #_[bkell.spittoon :as sp]))

(def env (:test (config/load-edn "config.edn")))

(defn gen-test-config []
  {:bkell {}
   :spittoon {:env env :recreate? true}})

(defn fixture-db-setup [f]
  (bkell/start (gen-test-config))
  (f))

(use-fixtures :each fixture-db-setup)


(defn account-generator []
  (gen/hash-map :name gen/string
                :type (gen/elements [:asset :liability :revenue :expense])
                :counterweight (gen/elements [:debit :credit])))

(defspec foobar
  10
  (prop/for-all [a true]

                a))

#_(defspec add-an-account
  10
  (prop/for-all [ds (-> bkell/system :spittoon :db)
                 group-name "webkell"
                 ;;account (account-generator)
                 ]

                (println "1: " ds)

                #_(dm/add-account ds group-name account)

                #_(= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                   (sort (keys (try+ (dm/add-account ds group-name account)
                                     (catch [:type :duplicate-account] _)))))))


#_(defspec disallow-duplicate-accounts
  10
  (prop/for-all [account (account-generator)]))


(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.domain-test))
