(defproject org.clojars.majorcluster/pedestal-api-helper "0.10.1"
  :description "Useful simple tools to be used for pedestal APIs"
  :url "https://github.com/majorcluster/pedestal-api-helper"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/core.async "1.6.673"]]
  :source-paths ["src/clj"]
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  :test-paths ["test","integration"]
  :profiles {:dev {:plugins [[com.github.clojure-lsp/lein-clojure-lsp "1.3.17"]]
                   :dependencies [[nubank/matcher-combinators "3.8.5"]
                                  [io.pedestal/pedestal.service "0.6.0"]
                                  [io.pedestal/pedestal.route "0.6.0"]
                                  [io.pedestal/pedestal.jetty "0.6.0"]
                                  [org.slf4j/slf4j-simple "2.0.7"]
                                  [nubank/mockfn "0.7.0"]]}}
  :aliases {"diagnostics"     ["clojure-lsp" "diagnostics"]
            "format"          ["clojure-lsp" "format" "--dry"]
            "format-fix"      ["clojure-lsp" "format"]
            "clean-ns"        ["clojure-lsp" "clean-ns" "--dry"]
            "clean-ns-fix"    ["clojure-lsp" "clean-ns"]
            "lint"            ["do" ["diagnostics"]  ["format"] ["clean-ns"]]
            "lint-fix"        ["do" ["format-fix"] ["clean-ns-fix"]]})
