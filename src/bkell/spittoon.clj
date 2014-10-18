(ns bkell.spittoon
  (:require [adi.core :as adi]
            [adi.utils :refer [iid ?q]]
            [bkell.config :as cfg]))

(defn db-getconnection
  ([env]
     (db-getconnection env false false))
  ([env install-schema? recreate-db?]
     (let [schema-file (:db-schema-file env)
           default-file (:db-default-file env)
           db-url (:db-url env)]

       (adi/datastore db-url (cfg/load-edn schema-file) install-schema? recreate-db?))))

(defn db-setup-default
  ([env]
     (let [default-file (:db-default-file env)
           default-loaded  (eval (cfg/load-edn default-file))]

       (db-setup-default env (db-getconnection env true true) default-loaded)))

  ([env ds default-data]
     (adi/insert! ds default-data)))
