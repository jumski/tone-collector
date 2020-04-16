(ns jumski.tone-collector.player
  (:import [javafx.scene.media MediaPlayer Media]
           [javafx.util Duration]))

(def *player (atom nil))
(def *last-file (atom nil))

(def start (Duration/seconds 0))
(def stop (Duration/seconds 2))

(defn replace-player!
  "Stops previous player if it is present,
  resets it to a new instance and returns it."
  [file]
  (do
    (if-let [player @*player]
      (.stop player))
    (let [media (-> file .toURI .toString Media.)
          player (doto
                   (MediaPlayer. media)
                   (.setStartTime start)
                   (.setStopTime stop))]
      (reset! *last-file file)
      (reset! *player player)
      player)))

(defn get-player!
  "If playing same file as previously, just deref *player, stop and return it.
  If playing new file, create new player, reset *player and return it."
  [file]
  (let [last-file @*last-file]
    (if (and (= file last-file) @*player)
      (doto @*player .stop)
      (replace-player! file))))

(defn play-file [file]
  (-> file get-player! .play))
