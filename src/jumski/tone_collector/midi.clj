(ns jumski.tone-collector.midi
  (:require [overtone.midi :as midi]))

(def input (midi/midi-in))
(midi/midi-handle-events input #(println "event" %))


; (let [in (midi/midi-in)
;       hfn (fn [x] (println "xxxx" x))]
;   (midi/midi-handle-events in hfn))