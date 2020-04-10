(ns jumski.tone-collector.e33
  (:require
    [cljfx.api :as fx]
    [cljfx.ext.list-view :as fx.ext.list-view]
    [jumski.tone-collector.file :refer [wav-files-in-dir]])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))

;;; state

(def *state
  (atom {:from-dir nil
         :to-dir nil
         :files []
         :selected-file nil}))

(defn ready-to-roll? [{:keys [from-dir to-dir]}]
  (not (or (nil? from-dir)
           (nil? to-dir))))

;;; views

(defn v-layout-view [& {:keys [children]}]
  {:fx/type :v-box
   :padding 5
   :spacing 5
   :children (vec children)})

(defn list-view [& a] {:fx/type :label :text (str @*state)})
; (defn list-view [{:keys [items selected]}]
;   {:fx/type fx.ext.list-view/with-selection-props
;    :props {:selection-mode :single
;            :selected-item (if (nil? selected-item)
;                             (first items)
;                             selected-item)
;            :on-selected-item-changed {::event ::select-file}}
;    :desc {:fx/type :list-view
;           :cell-factory (fn [x] {:text (.getPath x)})
;           :items items}})


(defn on-init-view [{:keys [from-dir to-dir]}]
  (v-layout-view :children [{:fx/type :h-box
                              :spacing 5
                              :alignment :center-left
                              :children [{:fx/type :button
                                          :on-action {::event ::open-dir
                                                      :dir :from-dir}
                                          :text "Select from folder"}
                                         {:fx/type :label
                                          :text from-dir}]}
                            {:fx/type :h-box
                             :spacing 5
                             :alignment :center-left
                             :children [{:fx/type :button
                                        :on-action {::event ::open-dir
                                                    :dir :to-dir}
                                         :text "Select to-dir folder"}
                                        {:fx/type :label
                                         :text to-dir}]}
                            {:fx/type list-view
                             :items (if (nil? from-dir) [] (wav-files-in-dir from-dir))
                             :selected nil}
                            {:fx/type :text-area
                             :v-box/vgrow :always
                             :wrap-text true
                             :editable false
                             :text (str @*state)}]))

(defn root-view [{:keys [from-dir to-dir] :as state}]
  {:fx/type :stage
   :title "Textual dir viewer"
   :showing true
   :x 20
   :y 20
   :width 500
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type on-init-view
                  :from-dir from-dir
                  :to-dir to-dir}}})

;;; handlers

(defn set-dir [state dir file]
  (assoc state dir (.getPath file)))

(defn maybe-load-files [state dir file]
  (if (= :from-dir dir)
    (let [files (wav-files-in-dir (.getPath file))]
      (-> state
          (assoc :files files)
          (assoc :selected-file (first files))))
    state))

(defmulti handle ::event)

(defmethod handle ::open-dir [{:keys [^ActionEvent fx/event dir state]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Open dir"))]
    (when-let [file @(fx/on-fx-thread (.showDialog chooser window))]
      {:state (-> state
                  (set-dir dir file)
                  (maybe-load-files dir file))})))

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
