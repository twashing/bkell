(ns bkell.spittoon
  (:require [adi.core :as adi]
            [adi.utils :refer [iid ?q]]
            [bkell.config :as config]))

(defn db-getconnection
  ([env]
     (db-getconnection env false false))
  ([env install-schema? recreate-db?]
     (let [schema-file (:db-schema-file env)
           default-file (:db-default-file env)
           db-url (:db-url env)]

       (adi/datastore db-url (config/load-edn schema-file) install-schema? recreate-db?))))

(defn db-setup-default
  ([env]
     (let [default-file (:db-default-file env)
           default-loaded  (eval (config/load-edn default-file))]

       (db-setup-default env (db-getconnection env true true) default-loaded)))

  ([env ds default-data]
     (adi/insert! ds default-data)))


(declare db-conn)

(defn db-create
  ([env] (db-create env (:db-schema-file env)))
  ([env schema-file]
     (adi/datastore (:db-url env) (config/load-edn schema-file) true true)))

(defn db-conn
  ([env] (db-conn env (:db-schema-file env)))
  ([env schema-file]
     (adi/datastore (:db-url env) (config/load-edn schema-file) false false)))

(defn db-init
  ([env] (db-init env (:db-schema-file env)))
  ([env schema-file]
     (let [ds (db-conn env schema-file)
           default-file (:db-default-file env)]

       (import env "nogroup" (eval (config/load-edn default-file)) schema-file))))

(defn db-import
  ([env group data] (db-import env group data (db-conn env (:db-schema-file env))))
  ([env group data data-store]

     ;; we need insert-in, then we can constrain the data being inserted, to the client group
     (adi/insert! data-store data)))
