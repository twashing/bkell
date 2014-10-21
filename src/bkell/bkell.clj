(ns bkell.bkell
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.namespace.repl :refer (refresh)]
            [adi.core :as adi]
            [adi.schema :as as]
            [adi.utils :refer [iid ?q]]
            [hara.component :as hco]
            [missing-utils.core :as mu]

            [bkell.config :as config]
            [bkell.spittoon :as spit]
            [bkell.component.bkell :as cb]
            [bkell.component.spittoon :as cs]))


;; Bkell Log config
(timbre/set-config! [:shared-appender-config :spit-filename] "logs/bkell.log")
(timbre/set-config! [:appenders :spit :enabled?] true)

(defn log-trace! [] (timbre/set-level! :trace))
(defn log-debug! [] (timbre/set-level! :debug))
(defn log-info! [] (timbre/set-level! :info))
(defn log-warn! [] (timbre/set-level! :warn))
(defn log-error! [] (timbre/set-level! :error))
(defn log-fatal! [] (timbre/set-level! :fatal))
(defn log-report! [] (timbre/set-level! :report))


;; Bkell State
(def ^{:doc "Bkell's component system map"} system nil)


#_(def topology {:bkell    [cb/map->Bkell :spittoon]
               :spittoon [cs/map->Spittoon]})

#_(def config   {:bkell {}
               :spittoon {}})


(def topology {:bkell    [cb/map->Bkell] })

(def config   {:bkell {}})

(defn start []
  (def system (hco/start (hco/system topology config))))

(defn stop []
  (if-not (nil? system)
    (hco/stop system)))

(defn reset []
  (stop)
  (start))



(defn import-create! [data group]

  ;; Tranducers here... yeah :)
  )


(defn import! [data group]
  )


;; (+ 1 2 3) C-u C-x C-e



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

(comment

  (start)
  (reset)
  (stop))
