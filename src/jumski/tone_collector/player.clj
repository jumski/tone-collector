(ns jumski.tone-collector.player
  (:import [javafx.scene.media MediaPlayer Media]
           [javafx.util Duration]))

(def *player (atom nil))
(def *last-file (atom nil))

(def start (Duration/seconds 0))
(def stop (Duration/seconds 2))

(defn create [file]
  (let [media (-> file .toURI .toString Media.)
        player (doto
                 (MediaPlayer. media)
                 (.setStartTime start)
                 (.setStopTime stop))]
    player))

(defn play [player]
  (if player
    (doto player .stop .play)))
