(ns apiary.github
  (:require
   [apiary.config :as config]
   [tentacles.issues :as issues]
   [tentacles.users :as users]
   [clj-time.core :as t]
   [clj-time.periodic :as p]
   [clj-time.predicates :as pr]
   [clj-time.format :as f]))

(defn- weekstart
  "Monday, at midnight, Zulu"
  []
  (let [start-at (t/minus (t/now) (t/days 6))
        range (take 7 (p/periodic-seq start-at (t/days 1)))
        n (first (filter pr/monday? range))]
    (t/date-time (t/year n) (t/month n) (t/day n))))

(defn- extract-effort-estimate
  "Extract the effort estimate saved in the github issue"
  [issue]
  (let [matches (re-find config/effort-estimate-regexp
                         (get issue :body ""))
        number (if matches (nth matches 1) "0")]
    (Integer. number)))

(defn- parse-date-time
  "Parse datetime string that github uses "
  [time-str]
  (f/parse (f/formatters :date-time-no-ms) time-str))

(defn- closed-during
  "Filer list of issues to just those closed between start and end"
  [start end issues]
  (let [period (t/interval start end)]
    (filter #(t/within? period
                        (parse-date-time
                         (get % :closed_at "19014-01-01T01:01:01Z")))
            issues)))

(defn closed-issues-updated-since
  "Github doesn't offer filtering of closed issues, so we grab the
  first 100 issues that are closed and updated since the given date,
  which we can latter filter down more"
  [start]
  (issues/issues config/org config/repo {:auth config/auth :state "closed"
                               :since start :sort "updated"
                               :direction "asc" :filter "* all"
                               :per-page 100}))

(defn period-seq-dec
  "Like clj-time period-seq but in the opposite direction"
  [start period]
  (map (fn [i]
         (t/minus start (.multipliedBy period i)))
       (iterate inc 0)))

(defn week->issues
  "Issues closed during week starting at weekstart"
  [weekstart]
  (let [issues (closed-issues-updated-since weekstart)]
    (closed-during weekstart (t/plus weekstart (t/weeks 1)) issues)))

(defn issues
  "Returns a lazy sequence of (closed) issues, grouped by week."
  []
  (let [weeks (period-seq-dec (weekstart) (t/weeks 1))]
    (map week->issues weeks)))

(defn score
  "Score issued that closed during the week of weekstart. Returns a
  hash of github user ids to scores"
  [issues]
  (let [per-issue-scores (map #(vector (keyword (or (get-in % [:assignee :login])
                                                    "unknown"))
                                       (extract-effort-estimate %))
                              issues)]
    ;; Convert sequence of vectors [:github-id score] to map of total scores.
    (into {}
          (map
           #(vector (key %)
                    (apply + (map (fn [i] (nth i 1))
                                  (val %))))
           (group-by  #(nth % 0) per-issue-scores)))))

(defn scores
  "Returns a lazy sequence of score maps, one per week"
  []
  (map score (issues)))

(defn summary
  "Summarize issues, assignee, title, and effort"
  [issues]
  (map #(hash-map :title (:title %)
                  :assignee (get-in % [:assignee :login] "unknown")
                  :url (:html_url %)
                  :effort (extract-effort-estimate %))
       issues))

(defn next-issues
  "Issues tagged with next"
  []
  (issues/issues config/org config/repo {:auth config/auth :state "open"
                                         :filter "* all" :per-page 100
                                         :labels ["next"]}))
