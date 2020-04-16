(ns jumski.tone-collector.effects
  (:require
    [overtone.midi :as midi]
    [clojure.java.io :as io]
    [jumski.tone-collector.player :as player]))

(defn play-effect [aplayer _]
  (if aplayer
    (player/play aplayer)))

(defn copy-effect [{:keys [^java.io.File file to-dir]} _]
  (let [to-file (io/file to-dir (.getName file))]
    (io/copy file to-file)))

(defn choose-midi-input-effect [state dispatch!]
  (let [midi-input (midi/midi-in)
        midi-event-handler (fn [{:keys [command note]}]
                             (if (= :note-on command)
                               (dispatch! {:event :midi-note-on
                                           :note note})))]
    (midi/midi-handle-events midi-input midi-event-handler)
    (dispatch! {:event :set-midi-input :input midi-input})))

