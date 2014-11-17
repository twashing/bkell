(ns bkell.domain.account-test
  (:require [midje.sweet :refer :all]
            [midje.repl :repl :all]
            [clojure.test :refer :all]
            [bkell.domain.test-helper :as hlp]
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


(defn account-generator []
  (gen/hash-map :name gen/string-ascii
                :type (gen/elements [:asset :liability :revenue :expense :capital])
                :counterWeight (gen/elements [:debit :credit])))

(defspec test-add-account
  10
  (prop/for-all [account (account-generator)]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      result (acc/add-account ds group-name account)]

                  (and (set/subset? #{:counterWeight :name :type}
                                    (-> result first :book :accounts first keys set))

                       (-> result nil? not)))))

(defspec test-create-account
  5
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      aname "fubar"
                      atype :asset
                      result (acc/create-account ds group-name aname atype)]

                  (and (set/subset? #{:counterWeight :name :type}
                                    (-> result first :book :accounts first keys set))

                       (-> result nil? not)))))

(defspec test-create-account-bad-type
  5
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      aname "fubar"
                      atype :fubar
                      result (try+ (acc/create-account ds group-name aname atype)
                                   (catch AssertionError e &throw-context))]

                  (= '(:cause :message :object :stack-trace :throwable)
                     (sort (keys result))))))

(defspec test-restrict-duplicate-account
  10
  (prop/for-all [account (account-generator)]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      _ (acc/add-account ds group-name account)
                      result (try+ (acc/add-account ds group-name account)
                                   (catch AssertionError e &throw-context))]

                  (= (sort '(:object :message :cause :stack-trace :throwable))
                     (sort (keys result))))))

(defspec test-addaccount-goesto-correctgroup
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

(defspec test-no-duplicate-accounts
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      a1 {:name "one" :type :asset :counterWeight :debit}
                      a2 {:name "two" :type :asset :counterWeight :debit}
                      accounts [a1 a2]]

                  (acc/no-duplicate-accounts? ds group-name accounts))))

(defspec test-add-accounts
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

(defspec test-list-accounts
  5
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)

                      result (acc/list-accounts ds group-name)]

                  (= result
                     #{{:account {:name "expense", :type :expense, :counterWeight :debit}}
                       {:account {:name "revenue", :type :revenue, :counterWeight :credit}}
                       {:account {:name "debt", :type :liability, :counterWeight :credit}}
                       {:account {:name "cash", :type :asset, :counterWeight :debit}}}))))

(defspec test-does-account-have-connected-entries?
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)
                      _ (hlp/add-test-entry ds group-name)

                      aname "electricity"
                      new-account {:name "trade-creditor2"
                                   :type :expense
                                   :counterWeight :debit}

                      r1 (acc/does-account-have-connected-entries? ds group-name aname)
                      r2 (acc/does-account-have-connected-entries? ds group-name "cash")]

                  (and r1
                       (not r2)))))

(defspec test-update-account-with-connected-entry-GOOD
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)
                      _ (hlp/add-test-entry ds group-name)

                      aname "electricity"
                      newa-good {:name "electricity2"}



                      rgood (try+ (acc/update-account ds group-name aname newa-good)
                                  (catch AssertionError e &throw-context))]

                  (and (-> rgood vector?)
                       (=  '(:account :db)
                           (-> rgood first keys sort))))))


(defspec test-update-account-with-connected-entry-BAD
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)
                      _ (hlp/add-test-entry ds group-name)

                      aname "electricity"

                      newa-bad {:name "electricity"
                                :type :liability
                                :counterWeight :credit}

                      rbad (try+ (acc/update-account ds group-name aname newa-bad)
                                 (catch Object e e))]

                  (and (-> rbad nil? not)
                       (= rbad {:type :connected-account-onlyname-violation})))))

#_(defspec test-update-account-without-connected-entry
  10
  (prop/for-all [_ gen/int]

                (let [group-name "webkell"
                      ds (hlp/setup-db!)
                      _ (hlp/setup-accounts ds group-name)
                      _ (hlp/add-test-entry ds group-name)

                      aname "electricity"
                      new-account {:name "trade-creditor2"
                                   :type :expense
                                   :counterWeight :debit}

                      r1 (acc/does-account-have-connected-entries? ds group-name aname)
                      r2 (acc/does-account-have-connected-entries? ds group-name "cash")
                      ;;_ (acc/update-account ds group-name aid new-account)
                      ]

                  (and r1
                       (not r2)))))


(comment
  (bkell/log-debug!)
  (bkell/log-info!)
  (midje.repl/autotest)
  (midje.repl/load-facts 'bkell.domain.account-test :current)

  (def group-name "webkell")
  (def ds (hlp/setup-db!))

  (def a1 {:name "one" :type :asset :counterWeight :debit})
  (def a2 {:name "two" :type :asset :counterWeight :debit})
  (def accounts [a1 a2])

  (acc/no-duplicate-accounts ds group-name accounts)
  (acc/add-accounts ds group-name accounts)

  (adi/select ds 17592186045470 :raw)
  (adi/select ds 17592186045470 :first)

  (acc/list-accounts ds group-name)

  (def r1 #{{:account {:name "expense", :type :expense, :counterWeight :debit}}
            {:account {:name "revenue", :type :revenue, :counterWeight :credit}}
            {:account {:name "debt", :type :liability, :counterWeight :credit}}
            {:account {:name "cash", :type :asset, :counterWeight :debit}}})

  )
