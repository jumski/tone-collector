(ns jumski.tone-collector.e33
  (:require [cljfx.api :as fx])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))

(def *state
  (atom {:dir nil}))

(defmulti handle ::event)

(defmethod handle ::open-dir [{:keys [^ActionEvent fx/event]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Open dir"))]
    (when-let [dir @(fx/on-fx-thread (.showDialog chooser window))]
      {:state {:dir dir}})))

(defn root-view [{:keys [dir content]}]
  {:fx/type :stage
   :title "Textual dir viewer"
   :showing true
   :width 800
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :padding 30
                  :spacing 15
                  :children [{:fx/type :h-box
                              :spacing 15
                              :alignment :center-left
                              :children [{:fx/type :button
                                          :text "Open dir..."
                                          :on-action {::event ::open-dir}}
                                         {:fx/type :label
                                          :text (str dir)}]}
                             {:fx/type :text-area
                              :v-box/vgrow :always
                              :editable false
                              :text (str dir)}]}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #(root-view %))
    :opts {:fx.opt/map-event-handler
           (-> handle
               (fx/wrap-co-effects {:state (fx/make-deref-co-effect *state)})
               (fx/wrap-effects {:state (fx/make-reset-effect *state)
                                 :dispatch fx/dispatch-effect})
               (fx/wrap-async))}))

(fx/mount-renderer *state renderer)
