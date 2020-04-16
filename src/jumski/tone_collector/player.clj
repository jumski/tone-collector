(ns jumski.tone-collector.player
  (:import [javafx.scene.media MediaPlayer Media]
           [javafx.util Duration]))

(def *player (atom nil))
(def start (Duration/seconds 0))
(def stop (Duration/seconds 2))

(defn replace-player!
  "Stops previous player if it is present,
  resets it to a new instance and returns it."
  [media]
  (do
    (if-let [player @*player]
      (.stop player))
    (let [player (doto
                   (MediaPlayer. media)
                   (.setStartTime start)
                   (.setStopTime stop))]
      (reset! *player player)
      player)))

(defn play-file [file]
  (let [media (-> file .toURI .toString Media.)
        player (replace-player! media)]
    (.play player)))
