(defproject bkell "0.1.0-SNAPSHOT"
  :description "Bkell provides a Shell and API for maintaining balanced records for business transactions"
  :url "https://github.com/twashing/bkell"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [org.clojure/test.check "0.5.9"]]

  :profiles {:dev {:source-paths ["dev"]}})
