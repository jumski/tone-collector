(ns jumski.tone-collector.main-gui
  (:require
    [cljfx.api :as fx]
    [jumski.tone-collector.player :refer [play-file]]
    [jumski.tone-collector.view :as view]
    [jumski.tone-collector.events :as events]
    [overtone.midi :as midi]
    [clojure.java.io :as io]))

;;; state

(def default-state
  (atom {:info-dialog-confirmed false
         :from-dir nil
         :to-dir nil
         :files []
         :midi {:input nil
                :waiting-for-note nil
                :play nil
                :skip nil
                :copy nil}}))

(defonce *state default-state)

(defn reset-default-state! []
  (reset! *state default-state))

;;; effects

(defn play-effect [file _]
  (if file
    (play-file file)))

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

;;; renderer

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #(view/root-view %))
    :opts {:fx.opt/map-event-handler
           (-> events/handle
               (fx/wrap-co-effects {:state (fx/make-deref-co-effect *state)})
               (fx/wrap-effects {:state (fx/make-reset-effect *state)
                                 :play play-effect
                                 :copy copy-effect
                                 :choose-midi-input choose-midi-input-effect
                                 :dispatch fx/dispatch-effect})
               (fx/wrap-async))}))

(fx/mount-renderer *state renderer)

;;; rapid feedback for :Require
; (jumski.tone-collector.main-gui/renderer)
