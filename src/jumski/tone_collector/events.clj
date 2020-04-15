(ns jumski.tone-collector.events
  (:require
    [cljfx.api :as fx]
    [jumski.tone-collector.file :refer [wav-files-in-dir]])
  (:import
    [javafx.stage DirectoryChooser]
    [javafx.event ActionEvent]
    [javafx.scene Node]))

;;; handlers

(defn maybe-load-files [state dir-key dir]
  (if (= :from-dir dir-key)
    (let [files (wav-files-in-dir (.getPath dir))]
      (-> state
          (assoc :files files)
          (assoc :current-file (first files))))
    state))

(defmulti handle :event)

(defmethod handle :play-file [{:keys [state]}]
  {:play (first (:files state))})

(defmethod handle :skip-file [{:keys [state]}]
  (let [new-state (update state :files rest)
        file-to-play (first (:files new-state))]
    {:state new-state
     :play file-to-play}))

(defmethod handle :copy-file [{:keys [state]}]
  (if (seq (:files state))
    (let [[file-to-move file-to-play] (:files state)]
      {:state (update state :files rest)
       :copy {:file file-to-move :to-dir (:to-dir state)}
       :play file-to-play})
    {}))

(defmethod handle :select-file [{file :fx/event state :state}]
  {:state (assoc state :current-file file)
   :play file})

(defmethod handle :no-op [& args]
  {})

(defmethod handle :open-dir [{:keys [^ActionEvent fx/event dir-key state]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Open dir"))]
    (when-let [file @(fx/on-fx-thread (.showDialog chooser window))]
      {:state (-> state
                  (assoc dir-key (.getPath file))
                  (maybe-load-files dir-key file))})))
