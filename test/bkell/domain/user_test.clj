(ns bkell.domain.user-test
  (:require [bkell.domain.group :as gp]
            [bkell.domain.user :as us]
            [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
            [slingshot.slingshot :refer [try+ throw+]]

            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]

            [bkell.domain.test-helper :as hlp]))


(defspec test-no-user?
  10
  (prop/for-all [_ gen/int]

                (let [user-name "fubar"
                      ds (hlp/setup-db!)]

                  (us/no-duplicate-user? ds user-name))))

(defspec test-add-user
  10
  (prop/for-all [_ gen/int]

                (let [user-name "one"
                      country-code "CA"
                      currency-code "CDN"
                      ds (hlp/setup-db!)]

                  (let [a (us/add-user ds user-name country-code currency-code)]

                    (and (= (sort '(:+ :name :users :owner :defaultCurrency :books))
                            (-> a first :system :groups first keys sort))

                         (not (empty? (gp/find-group-by-name ds "group-one"))))))))

(comment

  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.user-test)

  )
