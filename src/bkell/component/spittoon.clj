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
    (timbre/error "Spittoon.start CALLED > system[" sp "]")
    (if (:recreate? sp)
      (assoc sp :db (do (sp/db-create (:env sp))
                        (sp/db-conn (:env sp))
                        (sp/db-init (:env sp))))
      (assoc sp :db (sp/db-conn (:env sp)))))

  (-stop [sp]
    (timbre/trace "Spittoon.stop CALLED > system[" sp "]")
    (dissoc sp :db)))

(defmethod print-method Spittoon
  [v w]
    (.write w (str v)))
