(ns jumski.tone-collector.e33
  (:require [cljfx.api :as fx])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))

;;; state

(def *state
  (atom {:source-dir nil
         :destination-dir nil}))

(defn ready-to-roll? [state]
  false)

;;; views

(defn v-layout-view [& {:keys [children]}]
  {:fx/type :v-box
   :padding 5
   :spacing 5
   :children (vec children)})

(defn on-init-view [{:keys [source destination]}]
  (v-layout-view :children [{:fx/type :h-box
                              :spacing 5
                              :alignment :center-left
                              :children [{:fx/type :button
                                          :on-action {::event ::open-dir
                                                      :dir :source-dir}
                                          :text "Select Source folder"}
                                         {:fx/type :label
                                          :text source}]}
                            {:fx/type :h-box
                             :spacing 5
                             :alignment :center-left
                             :children [{:fx/type :button
                                        :on-action {::event ::open-dir
                                                    :dir :destination-dir}
                                         :text "Select Destination folder"}
                                        {:fx/type :label
                                         :text destination}]}
                            {:fx/type :text-area
                             :v-box/vgrow :always
                             :editable false
                             :text (str @*state)}]))

(defn sample-loaded-view [_]
  (v-layout-view :children []))

(defn root-view [{:keys [source-dir destination-dir] :as state}]
  {:fx/type :stage
   :title "Textual dir viewer"
   :showing true
   :width 800
   :height 600
   :scene {:fx/type :scene
           :root (if (ready-to-roll? state)
                   {:fx/type sample-loaded-view
                    :source source-dir
                    :destination destination-dir}
                   {:fx/type on-init-view
                    :source source-dir
                    :destination destination-dir})}})

;;; handlers

(defmulti handle ::event)

(defmethod handle ::open-dir [{:keys [^ActionEvent fx/event dir state]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Open dir"))]
    (when-let [file @(fx/on-fx-thread (.showDialog chooser window))]
      {:state (assoc state dir (.getPath file))})))

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
