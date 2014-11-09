(ns bkell.bkell
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.namespace.repl :refer (refresh)]
            [adi.core :as adi]
            [adi.schema :as as]
            [adi.data.common :refer [iid]]
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

(def environment-mode :dev)
(def system nil)

;; Bkell State
(def ^{:doc "Bkell's component system map"} system nil)


(def topology {:bkell    [cb/map->Bkell :spittoon]
               :spittoon [cs/map->Spittoon]})

(def config   {:bkell {}
               :spittoon {:env (environment-mode (config/load-edn "config.edn"))
                          :recreate? true}})

(defn start
  ([] (start config))
  ([config]
     (alter-var-root #'system (constantly (hco/start (hco/system topology config))))))

(defn stop []
  (alter-var-root #'system (fn [s] (when s (hco/stop system)))))

(defn reset []
  (stop)
  (start))


(defn import-create! [data group] )

(defn import! [data group] )


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
  (stop)

  (ns bkell.bkell)
  (require '[bkell.spittoon :as sp])
  (require '[bkell.config :as config])
  (require '[adi.core :as adi])

  (def env (:test (config/load-edn "config.edn")))
  (sp/db-create env)
  (sp/db-init env)

  (def schema-bkell (read-string (slurp "resources/db/schema-adi.edn")))
  (def data-bkell (read-string (slurp "resources/db/default.edn")))

  (def ds (adi/connect! "datomic:mem://adi-examples-bkell" schema-bkell true true))
  (adi/insert! ds data-bkell)


  (adi/select ds {:book
                  {:name "main"
                   :group/name "webkell"}}
              :ids)

  (adi/select ds {:journal
                  {:name "generalledger"
                   :book
                   {:name "main"
                    :group/name "webkell"}}}
              :ids)

  (adi/update! ds
               {:book
                {:name "main"
                 :group/name "webkell"}}
               {:book/accounts {:name "foo" :type :asset :counterWeight :debit}})

  (adi/update! ds
               {:journal
                {:name "generalledger"
                 :book
                 {:name "main"
                  :group/name "webkell"}}}
               {:journal/entries {:content []}})


  )
