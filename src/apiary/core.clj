(ns apiary.core
  (:require [apiary.github :as g]))

;; ──────────────────────────────────────────────────────────────────────
;; Lets Go!
;;
;; - get this weeks issues
;; - get this weeks score
;; currently do nothing with them
;;
;; - get last weeks issues
;; - get last weeks score
;;
;; Review issues:
;; - Are there unscored issues?
;; - Are there issues assigned to no-one?

(def this-weeks-issues (g/summary (first (g/issues))))
(def this-weeks-score (first (g/scores)))

(def last-weeks-issues (g/summary (second (g/issues))))
(def last-weeks-score (second (g/scores)))
