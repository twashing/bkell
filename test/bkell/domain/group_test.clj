(ns bkell.domain.group-test
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

(defspec test-no-duplicate-group?
  10
  (prop/for-all [_ gen/int]

                (let [group-name "fubar"
                      ds (hlp/setup-db!)]

                  (gp/no-duplicate-group? ds group-name))))

(defspec test-generate-nominal-group-bad-country
  10
  (prop/for-all [_ gen/int]

                (let [group-name "one"
                      country-code "fubar"
                      currency-code "CDN"
                      ds (hlp/setup-db!)]

                  (let [a (try+ (gp/generate-nominal-group ds group-name country-code currency-code)
                              (catch [:type :assertion-error] e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys a)))))))

(defspec test-generate-nominal-group-bad-currency
  10
  (prop/for-all [_ gen/int]

                (let [group-name "one"
                      country-code "CA"
                      currency-code "fubar"
                      ds (hlp/setup-db!)]

                  (let [a (try+ (gp/generate-nominal-group ds group-name country-code currency-code)
                              (catch [:type :assertion-error] e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :wrapper :throwable))
                     (sort (keys a)))))))

(defspec test-generate-nominal-group
  10
  (prop/for-all [_ gen/int]

                (let [group-name "one"
                      country-code "CA"
                      currency-code "CDN"
                      ds (hlp/setup-db!)]

                  (let [a (try+ (gp/generate-nominal-group ds group-name country-code currency-code)
                                (catch [:type :assertion-error] e &throw-context))]

                    (= (sort '(:+/db/id :name :users :owner :defaultCurrency :books))
                       (sort (keys a)))))))

(defspec test-add-group
  10
  (prop/for-all [_ gen/int]

                (let [group-name "one"
                      country-code "CA"
                      currency-code "CDN"
                      ds (hlp/setup-db!)]

                  (let [a (gp/add-group ds group-name country-code currency-code)]

                    (and (= (sort '(:+ :name :users :owner :defaultCurrency :books))
                            (-> a first :system :groups first keys sort))

                         (not (empty? (us/find-user-by-username ds "user-one"))))))))

(defspec test-list-groups
  10
  (prop/for-all [_ gen/int]

                (let [ds (hlp/setup-db!)
                      result (gp/list-groups ds)]

                  (and (= 2 (count result))

                       (= result #{{:name "guest"} {:name "webkell"}})))))


(comment

  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.group-test)

  )
