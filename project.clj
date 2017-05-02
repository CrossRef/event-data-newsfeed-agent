(defproject event-data-newsfeed-agent "0.1.9"
  :description "Crossref Event Data Newsfeed Agent"
  :url "http://eventdata.crossref.org"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.crossref.event-data-agent-framework "0.1.17"]
                 [com.rometools/rome "1.6.1"]
                 [commons-codec/commons-codec "1.10"]
                 [event-data-common "0.1.20"]
                 [org.apache.commons/commons-lang3 "3.5"]
                 [throttler "1.0.0"]]
  :main ^:skip-aot event-data-newsfeed-agent.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :prod {:resource-paths ["config/prod"]}
             :dev  {:resource-paths ["config/dev"]}})
