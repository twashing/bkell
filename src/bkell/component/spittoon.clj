(ns bkell.component.spittoon
  (:require  [taoensso.timbre :as timbre]
             [hara.component :as hco]
             [bkell.spittoon :as sp]))



(defrecord Spittoon []
  Object
  (toString [sp]
    (str "#sp" (into {} sp)))

  hco/IComponent

  (-start [sp]

    (timbre/trace "Spittoon.start CALLED > system[" sp "]")
    (let [db (sp/db-getconnection sp)]
      (assoc sp :db db)))

  (-stop [sp]

    (timbre/trace "Spittoon.stop CALLED > system[" sp "]")
    (dissoc sp :db)))

(defmethod print-method Spittoon
  [v w]
    (.write w (str v)))
