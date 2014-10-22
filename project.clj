(defproject bkell "0.1.0-SNAPSHOT"
  :description "Bkell provides a Shell and API for maintaining balanced records for business transactions"
  :url "https://github.com/twashing/bkell"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [com.taoensso/timbre "3.3.1"]
                 [adi "0.1.6"]
                 [com.datomic/datomic-free "0.9.4899"]
                 [im.chit/hara.component "2.1.4"]
                 [environ "1.0.0"]
                 [missing-utils "0.1.5"]
                 [slingshot "0.12.1"]]

  :repl-options {:init-ns bkell.bkell}

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/test.check "0.5.9"]
                                  [midje "1.6.3"]
                                  [alembic "0.2.1"]]
                   :plugins [[lein-midje "3.1.1"]
                             [cider/cider-nrepl "0.7.0"]]}})
