(ns apiary.core
  (:require [apiary.github :as g]))

;; Sample usage
(def last-weeks-score (second (g/scores)))
(def last-weeks-issue-summary (g/summary (second (g/issues))))
