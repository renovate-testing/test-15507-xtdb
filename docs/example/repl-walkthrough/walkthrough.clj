;; load a repl with the latest xtdb-core dependency, e.g. using clj:
;; $ clj -Sdeps '{:deps {com.xtdb/xtdb-core {:mvn/version "RELEASE"}}}'

(ns walkthrough.xtdb-standalone
  (:require [xtdb.api :as xt])
  (:import (xtdb.api IXtdb)))

;; this in-memory configuration is the easiest way to try XTDB, no Kafka needed
(def node
  (xt/start-node {}))


;; transaction containing a `put` operation, optionally specifying a valid time
(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :dbpedia.resource/Pablo-Picasso ; id
    :name "Pablo"
    :last-name "Picasso"
    :location "Spain"}
   #inst "1881-10-25T09:20:27.966-00:00"]
  [::xt/put
   {:xt/id :dbpedia.resource/Pablo-Picasso ; id
    :name "Pablo"
    :last-name "Picasso"
    :location "Sain2"}
   #inst "1881-10-25T09:20:27.966-00:00"]]) ; valid time, Picasso's birth


;; transaction containing a `match` operation
(xt/submit-tx
 node
 [[::xt/match ; check old version
   :dbpedia.resource/Pablo-Picasso
   {:xt/id :dbpedia.resource/Pablo-Picasso
    :name "Pablo"
    :last-name "Picasso"
    :location "Spain"}
   #inst "1973-04-08T09:20:27.966-00:00"]
  [::xt/put ; put new version if it matches
   {:xt/id :dbpedia.resource/Pablo-Picasso
    :name "Pablo"
    :last-name "Picasso"
    :height 1.63
    :location "France"}
   #inst "1973-04-08T09:20:27.966-00:00"]]) ; valid time, Picasso's death


;; transaction containing a `delete` operation, historical versions remain
(xt/submit-tx
 node
 [[::xt/delete :dbpedia.resource/Pablo-Picasso
   #inst "1973-04-08T09:20:27.966-00:00"]])


;; transaction containing an `evict` operation, historical data is destroyed
(xt/submit-tx
 node
 [[::xt/evict :dbpedia.resource/Pablo-Picasso]])


;; query the node as-of now
(xt/q
 (xt/db node)
 '{:find [(pull e [*])]
   :where [[e :name "Pablo"]]})

;; `put` the new version of the document again
(xt/submit-tx
 node
 [[::xt/put
   {:xt/id :dbpedia.resource/Pablo-Picasso
    :name "Pablo"
    :last-name "Picasso"
    :height 1.63
    :location "France"}
   #inst "1973-04-08T09:20:27.966-00:00"]])


;; again, query the node as-of now
(xt/q
 (xt/db node)
 '{:find [(pull e [*])]
   :where [[e :name "Pablo"]]})

;; again, query the node as-of now, as-at #inst "1973-04-07T09:20:27.966-00:00"
(xt/q
 (xt/db node #inst "1973-04-07T09:20:27.966-00:00")
 '{:find [(pull e [*])]
   :where [[e :name "Pablo"]]})
