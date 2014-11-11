(ns bkell.domain.group
  (:require [adi.core :as adi]
            [slingshot.slingshot :refer [try+ throw+]]
            [bkell.domain.helper :as hlp]))


(declare find-group-by-name)

(defn no-duplicate-group? [ds group-name]
  (let [a (find-group-by-name ds group-name)]
    (empty? a)))

(defn generate-nominal-group [ds gname country currency]

  (let [rcountry (hlp/find-country-by-code ds country)
        rcurrency (hlp/find-currency-by-code ds currency)
        generated-name (str "user-" gname)]

    (if (empty? rcountry)
      (throw+ {:type :assertion-error :message "No Country with code [" country "]"}))

    (if (empty? rcurrency)
      (throw+ {:type :assertion-error :message "No Currency with code [" currency "]"}))

    {:+/db/id [[:group-new]]

     :name gname

     :users #{{:+/db/id [[:user-new]]
               :username generated-name
               :password "default"
               :firstname generated-name
               :lastname generated-name
               :email generated-name
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

(defn add-group [ds gname country currency]
  {:pre [(no-duplicate-group? ds gname)]}

  (let [generated-group (generate-nominal-group ds gname country currency)]

    (adi/update! ds
                 {:system {:groups '_}}
                 {:system {:groups generated-group}})))

(defn find-group-by-name [ds gname]
  (adi/select ds {:group {:name gname}}))

(defn find-user-by-name [ds uname]
  (adi/select ds {:user {:username uname}}))
