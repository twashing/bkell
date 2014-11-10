(ns bkell.domain.entry
  (:require [adi.core :as adi]))


(defn find-corresponding-account-byname [ds group-name aname] )
(defn find-corresponding-account-byid [ds group-name aid] )

(defn corresponding-accounts-exist? [ds group-name entry] )
(defn entry-balanced? [ds group-name entry] )

(defn add-entry [ds group-name entry]
  {:pre [(not (nil? group-name))
         (not (nil? entry))
         (not (clojure.string/blank? (:date entry)))]}

  #_(let [account-list []]
           (and
            (corresponding-accounts-exist? ds group-name entry)
            (entry-balanced? ds group-name entry)))

  ;; TODO - transform :account to a ref, in entry content (http://docs.caudate.me/adi/#pointers) ... return new structure
  ;; TODO - assert entry balanced, then... update
  (adi/update! ds
               {:journal
                {:name "generalledger"
                 :book
                 {:name "main"
                  :group/name group-name}}}
               {:journal/entries entry}))
