(ns jumski.tone-collector.main-gui
  (:require
    [cljfx.api :as fx]
    [cljfx.ext.list-view :as fx.ext.list-view]
    [jumski.tone-collector.file :refer [wav-files-in-dir]]
    [jumski.tone-collector.player :refer [play-file]])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]
           [javafx.scene.input KeyCode KeyEvent]))

;;; state

(def *state
  (atom {:from-dir nil
         :to-dir nil
         :files []}))

(defn ready-to-roll? [{:keys [from-dir to-dir]}]
  (not (or (nil? from-dir)
           (nil? to-dir))))

;;; views

(defn v-layout-view [& {:keys [children]}]
  {:fx/type :v-box
   :padding 5
   :spacing 5
   :children (vec children)})

(defn list-view [{:keys [items selected-item]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props {:selection-mode :single
           :selected-item selected-item
           :on-selected-item-changed {::event ::select-file}}
   :desc {:fx/type :list-view
          :cell-factory (fn [file] {:text (.getPath file)})
          :items items}})


(defn on-init-view [{:keys [state] :as x}]
  (v-layout-view :children [{:fx/type :h-box
                              :spacing 5
                              :alignment :center-left
                              :children [{:fx/type :button
                                          :on-action {::event ::open-dir
                                                      :dir :from-dir}
                                          :text "Select from folder"}
                                         {:fx/type :label
                                          :text (:from-dir state)}]}
                            {:fx/type :h-box
                             :spacing 5
                             :alignment :center-left
                             :children [{:fx/type :button
                                        :on-action {::event ::open-dir
                                                    :dir :to-dir}
                                         :text "Select to-dir folder"}
                                        {:fx/type :label
                                         :text (:to-dir state)}]}
                            {:fx/type list-view
                             :items (:files state)
                             :selected-item (:current-file state)}
                            {:fx/type :text-area
                             :v-box/vgrow :always
                             :wrap-text true
                             :editable false
                             :text (str state)}]))

(defn root-view [state]
  {:fx/type :stage
   :title "Textual dir viewer"
   :showing true
   :x 20
   :y 20
   :width 500
   :height 600
   :scene {:fx/type :scene
           :on-key-pressed {::event ::key-pressed}
           :root {:fx/type on-init-view
                  :state state}}})

;;; handlers

(defn set-dir [state dir file]
  (assoc state dir (.getPath file)))

(defn maybe-load-files [state dir file]
  (if (= :from-dir dir)
    (let [files (wav-files-in-dir (.getPath file))]
      (-> state
          (assoc :files files)
          (assoc :current-file (first files))))
    state))

(defmulti handle ::event)

(defmethod handle ::key-pressed [{event :fx/event :as x}]
  {:state (:state x)}
  #_(let [kcode (.getCode ^KeyEvent event)]
    (case
      KeyCode/ENTER (play-file (:current-file @*state)))))

(defmethod handle ::select-file [{file :fx/event state :state}]
  {:state (assoc state :current-file file)
   :play-file file})

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
                                 :play-file (fn [file _] (play-file file))
                                 :dispatch fx/dispatch-effect})
               (fx/wrap-async))}))

(fx/mount-renderer *state renderer)
