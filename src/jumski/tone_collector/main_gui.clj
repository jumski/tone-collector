(ns jumski.tone-collector.main-gui
  (:require
    [cljfx.api :as fx]
    [jumski.tone-collector.view :as view]
    [jumski.tone-collector.events :as events]
    [jumski.tone-collector.effects :as effects]))

(def default-state
  {:info-dialog-confirmed false
   :from-dir nil
   :to-dir nil
   :files []
   :player nil
   :midi {:input nil
          :mapping-note-for-action nil
          :play nil
          :skip nil
          :copy nil}})

(defonce *state (atom default-state))

(def event-handler
  (-> events/handle
      (fx/wrap-co-effects {:state (fx/make-deref-co-effect *state)})
      (fx/wrap-effects {:state (fx/make-reset-effect *state)
                        :play effects/play-effect
                        :copy effects/copy-effect
                        :choose-midi-input effects/choose-midi-input-effect
                        :dispatch fx/dispatch-effect})
      (fx/wrap-async)))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #(view/root-view %))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
