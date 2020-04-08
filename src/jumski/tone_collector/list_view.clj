(ns jumski.tone-collector.list-view
  (:require [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view]))

(def only-wav-files
  "Transducer, filtering only paths to wav files, based on extension."
  (let [wav-regex #"(?i).*\.wav$"]
    (comp (filter #(.isFile ^java.io.File %))
          (filter #(re-matches wav-regex (.getPath ^java.io.File %))))))

(defn wav-files-in-dir
  "Returns lazy sequence of java.io.File in given `dir`."
  [dir]
  (->> (clojure.java.io/file dir)
       file-seq
       (sequence only-wav-files)))

(def *state
  (atom {:files []
         :selected-item nil}))

(defn list-view [{:keys [items selected-item]}]
  {:fx/type fx.ext.list-view/with-selection-props
   :props {:selection-mode :single
           :selected-item (if (nil? selected-item)
                            (first items)
                            selected-item)
           :on-selected-item-changed {:event/type ::select-media}}
   :desc {:fx/type :list-view
          :cell-factory (fn [x] {:text (.getPath x)})
          :items items}})

(defn load-button [& args]
  {:fx/type :button
   :text "Load media!"
   :on-action {:event/type ::load-media}})

(defn root-view [{:keys [state]}]
  {:fx/type :stage
   :showing true
   :width 600
   :height 800
   :x 10
   :y 10
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :alignment :center
                  :children [{:fx/type list-view
                              :items (:files state)}
                             {:fx/type load-button}
                             {:fx/type :label
                              :text (if (nil? (:selected-item state))
                                      ""
                                      (.getPath (:selected-item state)))}]}}})

(defmulti event-handler :event/type)
(defmethod event-handler ::select-media [{item :fx/event}]
  (swap! *state assoc :selected-item item))
(defmethod event-handler ::load-media [& args]
  (let [files (wav-files-in-dir "resources/")]
    (do
        (swap! *state assoc :files files)
        (swap! *state assoc :selected-item (first files)))))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type root-view
                                     :state state}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
