(ns jumski.tone-collector.main-gui
  (:require
    [cljfx.api :as fx]
    [jumski.tone-collector.player :refer [play-file]]
    [jumski.tone-collector.view :as view]
    [jumski.tone-collector.events :as events]
    [clojure.java.io :as io]))

;;; state

(def *state
  (atom {:from-dir nil
         :to-dir nil
         :files []}))

;;; effects

(defn play-effect [file _]
  (if file
    (play-file file)))

(defn copy-effect [{:keys [^java.io.File file to-dir]} _]
  (let [to-file (io/file to-dir (.getName file))]
    (io/copy file to-file)))

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
                                 :dispatch fx/dispatch-effect})
               (fx/wrap-async))}))

(fx/mount-renderer *state renderer)
