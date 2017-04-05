(ns event-data-newsfeed-agent.feeds
  "Manage newsfeed feed list."
  (:require [clojure.tools.logging :as l]
            [clojure.data.json :as json])
  (:require [clj-time.coerce :as clj-time-coerce]
            [clj-time.format :as clj-time-format]
            [throttler.core :refer [throttle-fn]]
            [clj-time.core :as clj-time])
  (:import [java.net URL]
           [java.io InputStreamReader]
           [com.rometools.rome.feed.synd SyndFeed SyndEntry SyndContent]
           [com.rometools.rome.io SyndFeedInput XmlReader]
           [org.apache.commons.codec.digest DigestUtils]
           [org.apache.commons.lang3 StringEscapeUtils]))

(defn choose-best-link
  "From a seq of links for the same resource, choose the best one."
  [& urls]
  (->> urls
      ; Remove those that aren't URLs.
      (keep #(try (new URL %) (catch Exception _ nil)))
      (remove nil?)
      ; Rank by desirability. Lowest is best.
      (sort-by #(cond
                  ; feeds.feedburner.com's URLs go via a Google proxy. Ignore those if possible.
                  (= (.getHost %) "feedproxy.google.com") 5
                  :default 1))
      first
      str))

(def date-format
  (:date-time-no-ms clj-time-format/formatters))

(defn parse-section
  "Parse a SyndEntry into an Action. Discard the summary and use the url type only, the Percolator will follow the link."
  [feed-url fetch-date-str ^SyndEntry entry]
  (let [title (.getTitle entry)
        ; Only 'link' is specified as being the URL, but feedburner includes the real URL only in the ID.
        url (choose-best-link (.getLink entry) (.getUri entry))
        ; Updated date is the date the blog is reported to have been published via the feed. Failing that, now.
        updated (try
                   (clj-time-coerce/from-date (or (.getUpdatedDate entry)
                                                  (.getPublishedDate entry)))
                   (catch Exception e (clj-time/now)))

        ; Use the URL of the blog post as the action identifier.
        ; This means that the same blog post in different feeds (or even sources) will have the same ID.
        action-id (DigestUtils/sha1Hex ^String url)]
    
    {:id action-id
     :url url
     :relation-type-id "discusses"
     :occurred-at (clj-time-format/unparse date-format updated)
     :observations [{:type :content-url :input-url url :sensitive true}]
     :extra {:feed-url feed-url}
     :subj {
      :type "post-weblog"
      ; Title appears as CDATA containing an HTML encoded string (different to XML encoded!) 
      :title (StringEscapeUtils/unescapeHtml4 title)}}))

(defn actions-from-xml-reader
  [url ^XmlReader reader]
  (let [input (new SyndFeedInput)
        feed (.build input reader)
        entries (.getEntries feed)
        parsed-entries (map (partial parse-section url (clj-time-format/unparse date-format (clj-time/now))) entries)]
  parsed-entries))

(defn get-items
  "Get list of parsed Actions from the feed url."
  [feed-url]
  (l/info "Retrieve latest from feed:" feed-url)
  (let [reader (new XmlReader (new URL feed-url))]
    (actions-from-xml-reader feed-url reader)))

(def get-items-throttled (throttle-fn get-items 20 :minute))


