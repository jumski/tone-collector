(ns jumski.tone-collector.player
  (:import [javafx.scene.media MediaPlayer Media]
           [javafx.util Duration]))

(defn play-file [file]
  (let [media (-> file .toURI .toString Media.)
        start (Duration/seconds 0)
        stop (Duration/seconds 2)
        player (doto
                 (MediaPlayer. media)
                 (.setStartTime start)
                 (.setStopTime stop))]
    (.play player)))
