(ns bkell.bkell
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.namespace.repl :refer (refresh)]
            [adi.core :as adi]
            [adi.schema :as as]
            [adi.utils :refer [iid ?q]]
            [hara.component :as hco]
            [missing-utils.core :as mu]

            [bkell.component.bkell :as cb]
            [bkell.component.spittoon :as cs]))


;; Bkell Log config
(timbre/set-config! [:shared-appender-config :spit-filename] "logs/bkell.log")
(timbre/set-config! [:appenders :spit :enabled?] true)


;; Bkell State
(def ^{:doc "Bkell's component system map"} system (atom nil))


(def topology {:bkell    [cb/map->Bkell :spittoon]
               :spittoon [cs/map->Spittoon]})

(def config   {:bkell {}
               :spittoon {}})

(defn start []
  (reset! system (hco/start (hco/system topology config))))

(defn stop []
  (if-not (nil? @system)
    (hco/stop @system)))

(defn reset []
  (stop)
  (refresh :after 'bkell.bkell/start))


(defn ^{:doc "This help function"}
  help []
  (let [shell-members (mu/fns-in-ns 'bkell.bkell)
        extract-doc-fn (fn [msym]
                         (str msym
                              ": "
                              (:doc (meta (ns-resolve 'bkell.bkell msym)))
                              (with-out-str (newline))))]

    (apply println
           (concat ["Bookkeeping Shell"
                    (with-out-str (newline))
                    (with-out-str (newline))]
                   (map extract-doc-fn shell-members)))))

(defn ^{:doc "Reloads project configuration and libraries"} reload-project [] (user/reload-project))
