(ns jumski.tone-collector.main-gui
  (:require
    [cljfx.api :as fx]
    [cljfx.ext.list-view :as fx.ext.list-view]
    [jumski.tone-collector.file :refer [wav-files-in-dir]]
    [jumski.tone-collector.player :refer [play-file]]
    [clojure.java.io :as io])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]
           [javafx.scene.input KeyCode KeyEvent]))

;;; state

(def *state
  (atom {:from-dir nil
         :to-dir nil
         :files []}))

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

(defn current-file-button [{:keys [state on-action text]}]
  (let [{:keys [files from-dir to-dir]} state
        current-file (first files)]
    (if (and current-file from-dir to-dir)
      {:fx/type :button
       :on-action on-action
       :style {:-fx-text-fill :black}
       :text text}
      {:fx/type :button
       :style {:-fx-text-fill :grey}
       :text text})))

(defn on-init-view [{:keys [state]}]
  (let [current-file (first (:files state))
        from-dir (:from-dir state)
        to-dir (:to-dir state)
        files (:files state)]
    (v-layout-view :children [{:fx/type :h-box
                               :spacing 5
                               :alignment :center-left
                               :children [{:fx/type :button
                                           :on-action {::event ::open-dir
                                                       :dir-key :from-dir}
                                           :text "Select from folder"}
                                          {:fx/type :label
                                           :text from-dir} ]}
                              {:fx/type :h-box
                               :spacing 5
                               :alignment :center-left
                               :children [{:fx/type :button
                                           :on-action {::event ::open-dir
                                                       :dir-key :to-dir}
                                           :text "Select to-dir folder"}
                                          {:fx/type :label
                                           :text to-dir}]}
                              {:fx/type :h-box
                               :spacing 5
                               :alignment :center-left
                               :children [{:fx/type current-file-button
                                           :state state
                                           :on-action {::event ::play-file}
                                           :text "▶ PLAY"}
                                          {:fx/type current-file-button
                                           :state state
                                           :on-action {::event ::skip-file}
                                           :text "⏩SKIP"}
                                          {:fx/type current-file-button
                                           :state state
                                           :on-action {::event ::copy-file}
                                           :text "🕮 MOVE"}]}
                              {:fx/type list-view
                               :items files
                               :on-key-pressed {::event ::key-pressed}
                               :selected-item current-file}
                              {:fx/type :text-area
                               :v-box/vgrow :always
                               :wrap-text true
                               :editable false
                               :text (str state)}])))

(defn root-view [state]
  {:fx/type :stage
   :title "Textual dir viewer"
   :showing true
   :x 20
   :y 20
   :width 500
   :height 600
   :scene {:fx/type :scene
           ; :on-key-pressed {::event ::key-pressed}
           :root {:fx/type on-init-view
                  :state state}}})

;;; handlers

(defn set-dir [state dir file]
  (assoc state dir (.getPath file)))

(defn maybe-load-files [state dir-key dir]
  (if (= :from-dir dir-key)
    (let [files (wav-files-in-dir (.getPath dir))]
      (-> state
          (assoc :files files)
          (assoc :current-file (first files))))
    state))

(defmulti handle ::event)

(def play-keycodes
  #{KeyCode/ENTER
    KeyCode/SPACE
    KeyCode/P})

(def copy-keycodes
  #{KeyCode/C
    KeyCode/Y})

(def skip-keycodes
  #{KeyCode/N})

(defmethod handle ::key-pressed [{event :fx/event state :state}]
  (let [kcode (.getCode ^KeyEvent event)]
    (println "kcode" kcode)
    (if (play-keycodes kcode)
      {:play state}
      {})))

(defmethod handle ::play-file [{:keys [state]}]
  {:play (first (:files state))})

(defmethod handle ::skip-file [{:keys [state]}]
  (let [new-state (update state :files rest)
        file-to-play (first (:files new-state))]
    {:state new-state
     :play file-to-play}))

(defmethod handle ::copy-file [{:keys [state]}]
  (if (seq (:files state))
    (let [[file-to-move file-to-play] (:files state)]
      {:state (update state :files rest)
       :copy {:file file-to-move :to-dir (:to-dir state)}
       :play file-to-play})
    {}))

(defmethod handle ::select-file [{file :fx/event state :state}]
  {:state (assoc state :current-file file)
   :play file})

(defmethod handle ::open-dir [{:keys [^ActionEvent fx/event dir-key state]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Open dir"))]
    (when-let [file @(fx/on-fx-thread (.showDialog chooser window))]
      {:state (-> state
                  (assoc dir-key (.getPath file))
                  (maybe-load-files dir-key file))})))

(defn play-effect [file _]
  (if file
    (play-file file)))

(defn copy-effect [{:keys [^java.io.File file to-dir]} _]
  (let [to-file (io/file to-dir (.getName file))]
    (io/copy file to-file)))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc #(root-view %))
    :opts {:fx.opt/map-event-handler
           (-> handle
               (fx/wrap-co-effects {:state (fx/make-deref-co-effect *state)})
               (fx/wrap-effects {:state (fx/make-reset-effect *state)
                                 :play play-effect
                                 :copy copy-effect
                                 :dispatch fx/dispatch-effect})
               (fx/wrap-async))}))

(fx/mount-renderer *state renderer)

(comment
  (renderer)
  (reset! *state (maybe-load-files @*state :from-dir (clojure.java.io/file"resources/")))
  (defn sf [f]
    (handle {::event ::select-file :fx/event f :state @*state}))

  (defn random-file []
    (->> @*state
         :files
         rand-nth
         sf))
  )
