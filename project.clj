(defproject event-data-newsfeed-agent "0.1.6"
  :description "Crossref Event Data Newsfeed Agent"
  :url "http://eventdata.crossref.org"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.crossref.event-data-agent-framework "0.1.3"]
                 [com.rometools/rome "1.6.1"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [mysql-java "5.1.21"] 
                 [korma "0.4.0"]]
  :main ^:skip-aot event-data-newsfeed-agent.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :prod {:resource-paths ["config/prod"]}
             :dev  {:resource-paths ["config/dev"]}})
