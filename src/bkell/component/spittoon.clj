(ns bkell.component.spittoon
  (:require  [taoensso.timbre :as timbre]
             [hara.component :as hco]
             [adi.data.common :refer [iid]]
             [bkell.spittoon :as sp]))


(defrecord Spittoon []

  Object
  (toString [sp]
    (str "#sp" (into {} sp)))

  hco/IComponent
  (-start [sp]
    (timbre/trace "Spittoon.start CALLED > system[" sp "]")
    (let [envr (:env sp)]
    
      (if (:recreate? sp)
        (let [db (sp/db-create envr)
              _ (sp/db-init envr)]
          (assoc sp :db db))
        (assoc sp :db (sp/db-conn envr)))))

  (-stop [sp]
    (timbre/trace "Spittoon.stop CALLED > system[" sp "]")
    (dissoc sp :db)))

(defmethod print-method Spittoon
  [v w]
    (.write w (str v)))
