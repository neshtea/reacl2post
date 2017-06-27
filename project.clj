(defproject reacl2post "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]

                 [active-clojure "0.20.0" :exclusions [org.clojure/clojure]]

                 [reacl "2.0.0"]]

  :plugins [[lein-figwheel "0.5.10"]]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/"]
                        :figwheel true
                        :compiler {:main "reacl2post.core"
                                   :asset-path "js/fp-out"
                                   :optimizations :none
                                   :output-to "resources/public/js/fp.js"
                                   :output-dir "resources/public/js/fp-out"}}]})
