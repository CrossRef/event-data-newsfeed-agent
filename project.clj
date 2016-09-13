(defproject event-data-newsfeed-agent "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.crossref/event-data-agent-framework "0.1.0"]
                 [com.rometools/rome "1.6.1"]
                 [org.clojure/java.jdbc "0.4.2"]
                [mysql-java "5.1.21"] 
                 [korma "0.4.0"]]
  :main ^:skip-aot event-data-newsfeed-agent.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :prod {:resource-paths ["config/prod"]}
             :dev  {:resource-paths ["config/dev"]}})
