(ns bkell.domain.domain-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
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
            [bkell.domain.domain :as dm]
            [bkell.spittoon :as sp]))


(def env (:test (config/load-edn "config.edn")))

(defn gen-test-config []
  {:bkell {}
   :spittoon {:env env :recreate? true}})


(defn setup-db []
  (let [schema-bkell (read-string (slurp "resources/db/schema-adi.edn"))
        data-bkell (read-string (slurp "resources/db/default.edn"))

        ds (adi/connect! "datomic:mem://adi-examples-bkell" schema-bkell true true)
        _ (adi/insert! ds data-bkell)]
    ds))

(defn account-generator []
  (gen/hash-map :name gen/string-ascii
                :type (gen/elements [:asset :liability :revenue :expense])
                :counterWeight (gen/elements [:debit :credit])))

#_(fact "add an account"
      (let [account {:name "foo" :type :asset :counterWeight :debit}
            group-name "webkell"
            ds (setup-db)

            result (dm/add-account ds group-name account)]

        (set/subset? #{:counterWeight :name :type}
                     (-> result first :book :accounts first keys set)) => true

        (-> result nil? not) => true
        ))

(defspec add-an-account
  10
  (prop/for-all [account (account-generator)]

                (let [group-name "webkell"
                      ds (setup-db)

                      result (dm/add-account ds group-name account)]

                  (and (set/subset? #{:counterWeight :name :type}
                                    (-> result first :book :accounts first keys set))

                       (-> result nil? not)))))

#_(defspec restrict-duplicate-account
  10
  (prop/for-all [account (account-generator)]

                (let [ds (-> bkell/system :spittoon :db)
                      group-name "webkell"
                      _ (dm/add-account ds group-name account)
                      result (try+ (dm/add-account ds group-name account)
                                   (catch AssertionError e &throw-context))]

                  (println "1: " result)

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys result))))))

#_(defspec addaccount-goesto-correctgroup
    10
    (prop/for-all [account (account-generator)]))



(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.domain-test)
  )
