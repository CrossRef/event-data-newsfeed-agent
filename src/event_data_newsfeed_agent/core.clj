(ns event-data-newsfeed-agent.core
  (:require [org.crossref.event-data-agent-framework.core :as c]
            [event-data-common.status :as status]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [event-data-newsfeed-agent.feeds :as feeds]
            [clj-time.core :as clj-time]
            [clojure.core.async :refer [>!!]])
   (:gen-class))

(def user-agent "CrossrefEventDataBot (eventdata@crossref.org) (by /u/crossref-bot labs@crossref.org)")
(def license "https://creativecommons.org/publicdomain/zero/1.0/")
(def version (System/getProperty "event-data-newsfeed-agent.version"))

(def source-token "c1bfb47c-39b8-4224-bb18-96edf85e3f7b")

(defn check-all-newsfeeds
  "Check all newsfeeds. "
  [artifacts bundle-chan]
  (log/info "Start crawl all newsfeeds at" (str (clj-time/now)))
  (let [[newsfeed-list-url newsfeed-list] (artifacts "newsfeed-list")
        ; Get the set of domains for this pass.
        newsfeed-set (clojure.string/split newsfeed-list #"\n")]
    (log/info "Got newsfeed-list artifact:" newsfeed-list-url)
      (doseq [this-newsfeed-url newsfeed-set]
       (log/info "Check newsfeed url" this-newsfeed-url)
       (status/add! "newsfeed-agent" "process" "scan-newsfeeds" 1)
       (let [actions (feeds/get-items-throttled this-newsfeed-url)
             package {:source-token source-token
                      :source-id "newsfeed"
                      :license license
                      :agent {:version version :artifacts {:newsfeed-list-artifact-version newsfeed-list-url}}
                      :pages [{:actions actions}]}]
      (log/info "Sending package...")
      (>!! bundle-chan package)))
      
    (log/info "Finished scan.")))

(def agent-definition
  {:agent-name "newsfeed-agent"
   :version version
   :schedule [{:name "check-all-newsfeeds"
               :seconds 1800 ; 30 minutes
               :fun check-all-newsfeeds
               :required-artifacts ["newsfeed-list"]}]
   :runners []
   :build-evidence (fn [input] nil)
   :process-evidence (fn [evidence] nil)})

(defn -main [& args]
  (c/run agent-definition))
