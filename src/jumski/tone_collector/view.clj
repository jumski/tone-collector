(ns jumski.tone-collector.view
  (:require [jumski.tone-collector.events :as events]
            [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]))

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
           :selected-item selected-item}
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

(defn midi-input-chooser [{:keys [midi]}]
  (let [input (:input midi)]
    {:fx/type :h-box
     :spacing 5
     :alignment :center-left
     :children [{:fx/type :button
                 :text "Change MIDI Input"
                 :style {:-fx-text-fill (if input :black :red)}
                 :on-action {:event :open-midi-input-dialog}}
                {:fx/type :label
                 :text (if-let [iname (:name input)]
                         (str iname " (last note: " (:last-note midi) ")"))}]}))

(defn midi-note-mapper [{:keys [midi action]}]
  (let [action-name (clojure.string/upper-case (name action))
        input (:input midi)
        mapped-note (action midi)

        no-midi-input? (nil? input)
        is-being-mapped? (= action (:mapping-note-for-action midi))
        mapping-in-progress? (:mapping-note-for-action midi)
        has-mapping? mapped-note
        action-disabled? no-midi-input?

        text-color (cond
                     no-midi-input? :grey
                     is-being-mapped? :red
                     mapping-in-progress? :grey
                     :else :black)
        text (cond
               is-being-mapped? (str "Press button for " action-name "!")
               (not has-mapping?) (str "Map " action-name)
               :else (str "Remap " action-name " [" mapped-note "]"))
        on-action (cond
                    action-disabled? {}
                    is-being-mapped? {:event :cancel-mapping-note-for-action}
                    :else {:event :start-mapping-note-for-action :action action})]
    {:fx/type :button
     :on-action on-action
     :style {:-fx-text-fill text-color}
     :text text}))

(defn midi-configuration [{:keys [state]}]
  (let [midi-config (:midi state)
        {:keys [input play skip copy]} midi-config]
    {:fx/type :v-box
     :spacing 5
     :alignment :center-left
     :children [{:fx/type midi-input-chooser
                 :midi midi-config}
                {:fx/type :h-box
                 :spacing 5
                 :alignment :center-left
                 :children [{:fx/type midi-note-mapper
                             :action :play
                             :midi midi-config}
                            {:fx/type midi-note-mapper
                             :action :skip
                             :midi midi-config}
                            {:fx/type midi-note-mapper
                             :action :copy
                             :midi midi-config}]}]}))

(defn on-init-view [{:keys [state]}]
  (let [current-file (first (:files state))
        from-dir (:from-dir state)
        to-dir (:to-dir state)
        files (:files state)]
    {:fx/type :v-box
     :padding 5
     :spacing 5
     :children [{:fx/type :h-box
                 :spacing 5
                 :alignment :center-left
                 :children [{:fx/type :button
                             :on-action {:event :open-dir
                                         :dir-key :from-dir}
                             :style {:-fx-text-fill (if from-dir :grey :red)}
                             :tooltip {:fx/type :tooltip
                                       :text "ziemniak"}
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
                 :children [{:fx/type midi-configuration
                             :state state}]}
                {:fx/type :h-box
                 :spacing 5
                 :alignment :center-left
                 :children (->> [{:fx/type :button
                                  :on-action (if (seq files) {:event :play} {})
                                  :style {:-fx-text-fill (if (seq files) :black :grey)}
                                  :text "▶ PLAY"}
                                 {:fx/type current-file-button
                                  :state state
                                  :on-action {:event :skip}
                                  :text "⏩SKIP"}
                                 {:fx/type current-file-button
                                  :state state
                                  :on-action {:event :copy}
                                  :text "🕮 COPY"}]
                                (filter identity))}
                {:fx/type list-view
                 :items files
                 :selected-item current-file}]}))

(def title-text "Tone Collector")
(def tagline-text "Select and copy samples with your MIDI controller")
(def help-text "1. Select source folder (can contain subfolders)
2. Select destination folder
3. Select MIDI input device
4. Map PLAY, SKIP and COPY actions to buttons/keys on your midi device
5. First sample on the list is the 'current sample'
6. PLAY auditions 'current sample'
7. SKIP goes to next sample, not copying anything
8. COPY copies 'current sample' to destination folder and goes to next sample
9. When run out of samples, select new or same source folder again.")


(defn info-dialog [{:keys [state]}]
  {:fx/type :v-box
   :spacing 30
   :style {:-fx-padding 20}
   :children [{:fx/type :label
               :style {:-fx-font-size 36
                       :-fx-font-weight :bold}
               :text title-text}
              {:fx/type :label
               :style {:-fx-font-size 16}
               :wrap-text true
               :text tagline-text}
              {:fx/type :label
               :wrap-text true
               :text help-text}
              {:fx/type :button
               :text "Click to continue"
               :on-action {:event :confirm-info-dialog}}]})

(defn root-view [state]
  {:fx/type :stage
   :title "Tone Collector"
   :showing true
   :x 20
   :y 20
   :width 500
   :height 600
   :scene {:fx/type :scene
           :root {:fx/type (if (:info-dialog-confirmed state) on-init-view info-dialog)
                  :state state}}})
