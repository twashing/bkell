(ns bkell.component.bkell
  (:require [taoensso.timbre :as timbre]
            [hara.component :as hco]
            [adi.data.common :refer [iid]]))

(defrecord Bkell []
  Object
  (toString [bk]
    (str "#bk" (into {} bk)))

  hco/IComponent
  (-start [bk]

    (timbre/trace "Bkell.start CALLED > system[" bk "]")
    (assoc bk :status "started"))

  (-stop [bk]

    (timbre/trace "Bkell.stop CALLED > system[" bk "]")
    (dissoc bk :status)))

(defmethod print-method Bkell
  [v w]
  (.write w (str v)))
