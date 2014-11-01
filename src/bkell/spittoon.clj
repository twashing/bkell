(ns bkell.spittoon
  (:require [adi.core :as adi]
            [adi.data.common :refer [iid]]
            [slingshot.slingshot :refer [try+ throw+]]
            [bkell.config :as config]))


(declare db-conn)
(declare db-import)

(defn db-create
  ([env] (db-create env (:db-schema-file env)))
  ([env schema-file]
     (try+
      (adi/connect! (:db-url env) (config/load-edn schema-file) true true)
      (catch Exception e
        (throw+ {:type :bad-input})))))

(defn db-conn
  ([env] (db-conn env (:db-schema-file env)))
  ([env schema-file]
     (try+
      (adi/connect! (:db-url env) (config/load-edn schema-file) false false)
      (catch Exception e
        (throw+ {:type :bad-input})))))

(defn db-init
  ([env] (db-init env (:db-schema-file env)))
  ([env schema-file]
     (let [ds (try+ (db-conn env schema-file) (catch Exception e (throw+ {:type :bad-input})))
           default-file (try+ (:db-default-file env) (catch Exception e (throw+ {:type :bad-input})))]

       (adi/insert! ds (eval (config/load-edn default-file)) schema-file))))

(defn db-import
  ([env group data]
     (db-import env group data (db-conn env (:db-schema-file env))))
  ([env group data data-store]

     ;; we need insert-in, then we can constrain the data being inserted, to the client group

     ;; import users (under :users), if you are the group owner
     ;; import accounts, journals, and journal entries (under :books), if your user is a member of the group

     ;; assumes a login mechanism in the shell and this function is either
     ;; i. executed in a scope where the current_user exists or
     ;; ii. the current_user is passed into the function
     (adi/insert! data-store data)))
