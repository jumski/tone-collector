(ns jumski.tone-collector.view
  (:require [jumski.tone-collector.events :as events]
            [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]))

(defn v-layout-view [& {:keys [children]}]
  {:fx/type :v-box
   :padding 5
   :spacing 5
   :children (vec children)})

(defn file-cell-factory [selected-file file]
  {:text (.getName file)
   :style {:-fx-background :-fx-control-inner-background
           :-fx-background-color (if (= selected-file file)
                                   [:-fx-table-cell-border-color :-fx-background]
                                   :white)
           :-fx-text-fill (if (= selected-file file) :black :grey)
           :-fx-background-insets [0 [0 0 1 0]]
           :-fx-table-cell-border-color "derive(-fx-color, 5%)"}})

(defn list-view [{:keys [items selected-item]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props {:selection-mode :single
           :selected-item selected-item
           :on-selected-item-changed {:event :no-op}}
   :desc {:fx/type :list-view
          :cell-factory (partial file-cell-factory selected-item)
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
                                           :on-action {:event :open-dir
                                                       :dir-key :from-dir}
                                           :style {:-fx-text-fill (if from-dir :grey :red)}
                                           :text "Change source folder"}
                                          {:fx/type :label
                                           :text (if from-dir
                                                   from-dir
                                                   "Folder with files to audition and copy")}]}
                              {:fx/type :h-box
                               :spacing 5
                               :alignment :center-left
                               :children [{:fx/type :button
                                           :on-action {:event :open-dir
                                                       :dir-key :to-dir}
                                          :style {:-fx-text-fill (if (and from-dir to-dir) :grey :red)}
                                           :text "Change destination folder"}
                                          {:fx/type :label
                                           :text (if to-dir
                                                   to-dir
                                                   "Destination folder for copied files")}]}
                              {:fx/type :h-box
                               :spacing 5
                               :alignment :center-left
                               :children (->> [(if current-file {:fx/type :label
                                                                :text (str "Current file: " (.getName current-file))})
                                              {:fx/type :button
                                               :on-action (if (seq files) {:event :play-file} {})
                                               :style {:-fx-text-fill (if (seq files) :black :grey)}
                                               :text "▶ PLAY"}
                                              {:fx/type current-file-button
                                               :state state
                                               :on-action {:event :skip-file}
                                               :text "⏩SKIP"}
                                              {:fx/type current-file-button
                                               :state state
                                               :on-action {:event :copy-file}
                                               :text "🕮 COPY"}]
                                             (filter identity))}
                              {:fx/type list-view
                               :items files
                               :selected-item current-file}])))

(defn root-view [state]
  {:fx/type :stage
   :title "Textual dir viewer"
   :showing true
   :x 20
   :y 20
   :width 500
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type on-init-view
                  :state state}}})
