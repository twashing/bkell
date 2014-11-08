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

  ;; TODO - (maybe) monadically execute
  (-start [sp]
    (timbre/trace "Spittoon.start CALLED > system[" sp "]")
    (if (:recreate? sp)
      (let [db (sp/db-create (:env sp))
            _ (sp/db-init (:env sp))]
        (assoc sp :db db))
      (assoc sp :db (sp/db-conn (:env sp)))))

  (-stop [sp]
    (timbre/trace "Spittoon.stop CALLED > system[" sp "]")
    (dissoc sp :db)))

(defmethod print-method Spittoon
  [v w]
    (.write w (str v)))
