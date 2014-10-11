(ns bkell.component.spittoon
  (:require  [taoensso.timbre :as timbre]
             [hara.component :as hco]

             [adi.core :as adi]
             [adi.utils :refer [iid ?q]]
             [bkell.config :as cfg]))


(defn db-load-schema [file-name]
  (cfg/load-edn file-name))

(defn db-install-schema [data-store default-file]
  (let [df (eval (cfg/load-edn default-file))]
    (adi/insert! data-store df)))

(defn db-getconnection [env]

  (let [install-schema? true
        create-db? true

        schema-file "db/schema-adi.edn"
        default-file "db/default.edn"
        db-url "datomic:mem://bkeeping"
        ds (adi/datastore db-url (db-load-schema schema-file) install-schema? create-db?)]

    (if create-db? (db-install-schema ds default-file))
    ds))


(defrecord Spittoon []
  Object
  (toString [sp]
    (str "#sp" (into {} sp)))

  hco/IComponent

  (-start [sp]

    (timbre/trace "Spittoon.start CALLED > system[" sp "]")
    (let [db (db-getconnection sp)]
      (assoc sp :db db)))

  (-stop [sp]

    (timbre/trace "Spittoon.stop CALLED > system[" sp "]")
    (dissoc sp :db)))

(defmethod print-method Spittoon
  [v w]
    (.write w (str v)))
