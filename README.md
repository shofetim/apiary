# apiary

Project and team tracking.

## Example Usage

Fire up nrepl in apiary.core and eval:

```clojure

(ns apiary.core
  (:require [apiary.github :as g]))

;; Sample usage
(def last-weeks-score (second (g/scores)))
(def last-weeks-issue-summary (g/summary (second (g/issues))))
```

Review scores and issues by week, explore.

## License

Copyright © 2014 Jordan T. Schatz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
