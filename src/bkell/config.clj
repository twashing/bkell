(ns bkell.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [taoensso.timbre :as timbre]
            [environ.core :as env]
            [adi.utils :refer [iid ?q]]))

(defn load-edn [fname]
  (let [pbreader (java.io.PushbackReader. (io/reader (io/resource fname)))]
    (edn/read pbreader)))

(defn get-config-raw []
  (load-edn "config.edn"))

(defn get-config

  ([mode]
     (get-config mode (get-config-raw)))

  ([mode config-raw]
     (let [
           ;; 1. try config.edn
           config (mode config-raw)

           ;; 2. whose values would overide HOST ENV values. see: https://github.com/weavejester/environ
           configM (apply array-map
                          (mapcat
                           (fn [[k v]]
                             [k
                              (if-not (empty? v)
                                v
                                (env/env k))])
                           (seq config)))
           configM (assoc configM :host (env/env :host))]

       (timbre/debug "config MERGED[" configM "]")
       configM)))

(defn get-project-config

  ([env-key]
     (get-project-config env-key (get-config-raw)))

  ([env-key config-raw]

     (let [config-pulled (get-config env-key config-raw)

           project (-> "project.clj" slurp read-string)
           pname (nth project 1)
           pversion (nth project 2)
           pdescription (nth project 4)
           purl (nth project 6)

           configP (assoc config-pulled
                     :project-info {:project-name pname
                                    :project-version pversion
                                    :project-description pdescription
                                    :project-url purl})]

       (timbre/trace "config PROJECT[" configP "]")
       configP)))
