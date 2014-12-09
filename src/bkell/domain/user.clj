(ns bkell.domain.user
  (:require [adi.core :as adi]
            [bkell.domain.helper :as hlp]))

(declare find-user-by-username)

(defn no-duplicate-user? [ds uname]
  (let [a (find-user-by-username ds uname)]
    (empty? a)))

(defn generate-nominal-group [ds uname country currency]
  (hlp/generate-nominal-group ds (str "group-" uname) uname country currency))

(defn add-user [ds uname country currency]
  {:pre [(no-duplicate-user? ds uname)]}

  (let [generated-group (generate-nominal-group ds uname country currency)]

    (adi/update! ds
                 {:system {:groups '_}}
                 {:system {:groups generated-group}})))

(defn find-user-by-username [ds uname]
  (adi/select ds {:user {:username uname}}))

(defn list-users [ds gname]
  (adi/select ds
              {:user
               {:username '_
                :groups
                {:name gname}}}
              :pull {:user :checked}))
