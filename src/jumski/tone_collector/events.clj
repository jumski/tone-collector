(ns jumski.tone-collector.events
  (:require
    [cljfx.api :as fx]
    [jumski.tone-collector.file :refer [wav-files-in-dir]])
  (:import
    [javafx.stage DirectoryChooser]
    [javafx.event ActionEvent]
    [javafx.scene Node]))

(defn maybe-load-files [state dir-key dir]
  (if (= :from-dir dir-key)
    (let [files (wav-files-in-dir (.getPath dir))]
      (-> state
          (assoc :files files)
          (assoc :current-file (first files))))
    state))

(defmulti handle :event)

(defmethod handle :confirm-info-dialog [{:keys [state]}]
  {:state (assoc state :info-dialog-confirmed true)})

(defmethod handle :play [{:keys [state]}]
  (let [{:keys [midi]} state]
    (if (:mapping-note-for-action state)
      {}
      {:play (first (:files state))})))

(defmethod handle :skip [{:keys [state]}]
  (let [new-state (update state :files rest)
        file-to-play (first (:files new-state))]
    {:state new-state
     :play file-to-play}))

(defmethod handle :copy [{:keys [state]}]
  (if (seq (:files state))
    (let [[file-to-move file-to-play] (:files state)]
      {:state (update state :files rest)
       :copy {:file file-to-move :to-dir (:to-dir state)}
       :play file-to-play})
    {}))

(defmethod handle :open-dir [{:keys [^ActionEvent fx/event dir-key state]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Open dir"))]
    (when-let [file @(fx/on-fx-thread (.showDialog chooser window))]
      {:state (-> state
                  (assoc dir-key (.getPath file))
                  (maybe-load-files dir-key file))})))

(defmethod handle :open-midi-input-dialog [{:keys [fx/event state]}]
  {:choose-midi-input state})

(defmethod handle :set-midi-input [{:keys [state input]}]
  {:state (assoc-in state [:midi :input] input)})

(defmethod handle :midi-note-on [{:keys [state note]}]
  (let [midi (:midi state)
        mapping-note-for-action (:mapping-note-for-action midi)]
    (if mapping-note-for-action
      {:dispatch {:event :map-note-to-action :note note :action mapping-note-for-action}}
      {:dispatch {:event :trigger-action-for-note :note note}})))

(defmethod handle :map-note-to-action [{:keys [state note action]}]
  {:state (-> state
              (assoc-in [:midi action] note)
              (assoc-in [:midi :mapping-note-for-action] nil))})

(defmethod handle :trigger-action-for-note [{:keys [state note]}]
  (let [note->action (clojure.set/map-invert
                       (select-keys (:midi state) [:play :skip :copy]))
        action-to-trigger (note->action note)]
    (if action-to-trigger
      {:dispatch {:event action-to-trigger}})))

(defmethod handle :cancel-mapping-note-for-action [{:keys [state]}]
  {:state (assoc-in state [:midi :mapping-note-for-action] nil)})

(defmethod handle :start-mapping-note-for-action [{:keys [state action]}]
  {:state (assoc-in state [:midi :mapping-note-for-action] action)})
