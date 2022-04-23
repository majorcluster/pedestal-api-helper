(defproject org.clojars.majorcluster/pedestal-api-helper "0.3.1"
  :description "Useful simple tools to be used for pedestal APIs"
  :url "https://github.com/mtsbarbosa/pedestal-api-helper"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "0.2.6"]]
  :source-paths ["src/clj"]
  :profiles {:dev {:dependencies [[nubank/matcher-combinators "1.2.1"]]}})
