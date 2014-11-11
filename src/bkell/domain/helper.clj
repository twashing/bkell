(ns bkell.domain.helper
  (:require [adi.core :as adi]
            [clojure.set :as set]
            [slingshot.slingshot :refer [try+ throw+]]))

(defn find-by-id [ds id]
  (adi/select ds id :first))

(defn find-country-by-code
  ([ds code]
     (find-country-by-code ds code [:ids]))

  ([ds code opts]
     (let [args [ds {:country {:id code}}]
           argsF (set/union args opts)]

       (apply adi/select argsF))))

(defn find-currency-by-code
  ([ds code]
     (find-currency-by-code ds code [:ids]))

  ([ds code opts]
     (let [args [ds {:currency {:id code}}]
           argsF (set/union args opts)]

       (apply adi/select argsF))))

(defn generate-nominal-group [ds gname uname country currency]

  (let [rcountry (find-country-by-code ds country)
        rcurrency (find-currency-by-code ds currency)]

    (if (empty? rcountry)
      (throw+ {:type :assertion-error :message "No Country with code [" country "]"}))

    (if (empty? rcurrency)
      (throw+ {:type :assertion-error :message "No Currency with code [" currency "]"}))

    {:+/db/id [[:group-new]]

     :name gname

     :users #{{:+/db/id [[:user-new]]
               :username uname
               :password "default"
               :firstname uname
               :lastname uname
               :email uname
               :defaultgroup [[:group-new]]
               :country (-> rcountry first :db :id)}}

     :owner [[:user-new]]

     :defaultCurrency (-> rcurrency first :db :id)

     :books #{{:name "main"
               :accounts #{{:name "cash"
                            :type :asset
                            :counterWeight :debit}
                           {:name "debt"
                            :type :liability
                            :counterWeight :credit}
                           {:name "revenue"
                            :type :revenue
                            :counterWeight :credit}
                           {:name "expense"
                            :type :expense
                            :counterWeight :debit}}

               :journals #{{:name "generalledger"
                            :entries #{}}}}}}))
