(ns event-data-newsfeed-agent.core
  (:require [org.crossref.event-data-agent-framework.core :as c]
            [org.crossref.event-data-agent-framework.util :as agent-util]
            [org.crossref.event-data-agent-framework.web :as agent-web]
            [crossref.util.doi :as cr-doi])
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :refer [reader]])
  (:require [event-data-newsfeed-agent.feeds :as feeds])
  (:require [korma.core :as k]
            [korma.db :as kdb]
            [config.core :refer [env]]
            [clj-time.coerce :as coerce]
            [clj-time.core :as clj-time])
   (:gen-class))

(def source-token "c1bfb47c-39b8-4224-bb18-96edf85e3f7b")
(def version "0.1.6")

(kdb/defdb db (kdb/mysql {:db (:db-name env)
                          :host (:db-host env) 
                          :port (Integer/parseInt (:db-port env))
                          :user (:db-user env)
                          :password (:db-password env)}))


; The date we saw a blog URL on a feed.
; Due to historical data missing, :seen and :feed_url can be missing.
(k/defentity seen-blog-urls
  (k/table "seen_blog_url")
  (k/pk :id)
  (k/entity-fields :id :blog_url :feed_url :seen)
  (k/transform (fn [{seen :seen :as obj}]
                 (if-not seen obj
                   (assoc obj :seen (str (coerce/from-sql-date seen)))))))

(defn evidence-record-from-blog-items
  "Accept a blog feed item and required artifacts, derive an Evidence Record"
  [blog-items newsfeed-list-url this-newsfeed-url domain-list-url domain-set]
  (log/info "Build evidence record")
  (let [; Decorate with whether or not the blog url has been seen before.
        decorated-blog-items (map (fn [blog-url-item]
                                     (let [item-url (:link blog-url-item)
                                           seen (first (k/select seen-blog-urls (k/where {:blog_url item-url})))]
                                       {:seen-before (some? seen)
                                        :seen-before-date (:seen seen)
                                        :seen-before-feed (:feed_url seen)
                                        :url item-url
                                        :blog-item blog-url-item})) blog-items)
         
         seen-items (filter :seen-before decorated-blog-items)
         unseen-items (remove :seen-before decorated-blog-items)
         
         ; Show working data per blog item.
         per-item (into {}
                    (map (fn [item]
                     (c/send-heartbeat "newsfeed-agent/feed/analyze-item" 1)
                     (let [body (-> item :blog-item :summary)
                           link (-> item :blog-item :link)
                           ; this is a map of {candidate-url {:doi doi :version service-version}]}
                           url-doi-matches (agent-web/extract-dois-from-body-via-landing-page-urls domain-set body)
                           found-dois (keep #(-> % second :doi) url-doi-matches)]
                       (c/send-heartbeat "newsfeed-agent/feed/found-item" (count url-doi-matches))
                            
                       [link
                        {:data item
                         :dois found-dois
                         :url-doi-matches url-doi-matches}])) unseen-items))
         
         ; All events derived from this newsfeed crawl.
         events (mapcat (fn [[blog-url item]]
                          (map (fn [doi]     
                                 {:uuid (str (java.util.UUID/randomUUID))
                                  :source_token source-token
                                  :subj_id blog-url
                                  :obj_id (cr-doi/normalise-doi doi)
                                  :relation_type_id "discusses"
                                  :source_id "newsfeed"
                                  :action "added"
                                  :occurred_at (str (coerce/from-string (-> item :data :blog-item :updated)))
                                  :subj {:title (-> item :data :blog-item :title)
                                         :issued (str (-> item :data :blog-item :updated))
                                         :pid blog-url
                                         :URL blog-url
                                         :type "post-weblog"}})
                                 (:dois item)))
                          per-item)]
         
         (log/info "Return evidence record")
         
         ; Return the whole evidence record for this retrieval. 
         {:artifacts [; List of newsfeeds used in this crawl.
                      newsfeed-list-url
                      ; List of domains used to filter URLs in this crawl.
                      domain-list-url]
          :input {; This newsfeed url.
                  :newsfeed-url this-newsfeed-url
                  
                  ; The URLs of blogs found this crawl.
                  :blog-urls (map #(-> % :blog-item :link) decorated-blog-items)
                  
                  ; Blog URLs that were seen before, where and when.
                  :blog-urls-seen (map #(select-keys % [:seen-before :seen-before-date :seen-before-feed :url]) seen-items)
                  
                  ; Blog URLs that were not seen before. We're interested in these.
                  :blog-urls-unseen (map #(-> % :blog-item :link) unseen-items)}
          
          :processing per-item
          
          :deposits events}))

(defn check-all-newsfeeds
  "Check all newsfeeds. "
  [artifacts send-evidence-callback]
  (log/info "Start crawl all newsfeeds at" (str (clj-time/now)))
  (let [[newsfeed-list-url newsfeed-list-file] (artifacts "newsfeed-list")
        [domain-list-url domain-list-file] (artifacts "domain-list")
        ; Get the set of domains for this pass.
        domain-set (agent-util/text-file-to-set domain-list-file)]
    (log/info "Got newsfeed-list artifact:" newsfeed-list-url)
    (log/info "Got domain-list artifact: " domain-list-url)
    (with-open [rdr (reader newsfeed-list-file)]
       (doseq [this-newsfeed-url (line-seq rdr)]
         (log/info "Check newsfeed url" this-newsfeed-url)
         (c/send-heartbeat "newsfeed-agent/feed/fetch-feed" 1)
         ; Items are hashmaps of {:title, :link, :id, :updated, :summary, :feed-url, :fetch-date}.
         (let [blog-items (feeds/get-items this-newsfeed-url)
               evidence-record (evidence-record-from-blog-items blog-items newsfeed-list-url this-newsfeed-url domain-list-url domain-set)
               
               ; Send evidence.
               callback-result (send-evidence-callback evidence-record)]
          (log/info "Got callback result" callback-result)
          (when callback-result
            (doseq [blog-url (-> evidence-record :input :blog-urls-unseen)]
              (log/info "Marking seen URL" blog-url)
              
              (k/insert seen-blog-urls (k/values {:blog_url blog-url
                                                  :feed_url this-newsfeed-url
                                                  :seen (coerce/to-sql-time (clj-time/now))}))))))))
  
  (log/info "Finished crawl all newsfeeds at" (str (clj-time/now))))


(def agent-definition
  {:agent-name "newsfeed-agent"
   :version version
   :schedule [{:name "check-all-newsfeeds"
               :seconds 1800 ; 30 minutes
               :fun check-all-newsfeeds
               :required-artifacts ["newsfeed-list" "domain-list"]}]
   :runners []
   :build-evidence (fn [input] nil)
   :process-evidence (fn [evidence] nil)})

(defn -main [& args]
  (c/run args agent-definition))
