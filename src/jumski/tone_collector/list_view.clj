(ns jumski.tone-collector.list-view
  (:require [cljfx.api :as fx]
            [cljfx.ext.list-view :as fx.ext.list-view])
  (:import [javafx.stage DirectoryChooser]
           [javafx.event ActionEvent]
           [javafx.scene Node]))

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

(defn choose-directory-button [& args]
  {:fx/type :button
   :text "Select source directory"
   :on-action {:event/type ::select-media-directory}})

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
                             {:fx/type choose-directory-button}
                             {:fx/type :label
                              :text (if (nil? (:selected-item state))
                                      ""
                                      (.getPath (:selected-item state)))}
                             {:fx/type :label
                              :text (str "Source dir: " (:selected-media-directory state))}]}}})

(defmulti handle :event/type)

(defmethod handle ::select-media [{item :fx/event}]
  (swap! *state assoc :selected-item item))
(defmethod handle ::load-media [& args]
  (let [files (wav-files-in-dir "resources/")]
    (do
        (swap! *state assoc :files files)
        (swap! *state assoc :selected-item (first files)))))
(defmethod handle ::set-media-directory [file]
  (swap! *state assoc :selected-media-directory file))

(defmethod handle ::select-media-directory [{:keys [^ActionEvent fx/event]}]
  (let [window (.getWindow (.getScene ^Node (.getTarget event)))
        chooser (doto (DirectoryChooser.)
                  (.setTitle "Select source directory"))]
    (when-let [dir @(fx/on-fx-thread (.showDialog chooser window))]
      {:state {:selected-media-directory dir}})))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type root-view
                                     :state state}))
    :opts {:fx.opt/map-event-handler
           (-> handle
               (fx/wrap-co-effects {:state (fx/make-deref-co-effect *state)})
               (fx/wrap-effects {:state (fx/make-reset-effect *state)
                                 :dispatch fx/dispatch-effect})
               (fx/wrap-async))}))

(comment

  (let [handler (fn [event]
                  (println "in handler" event)
                  {:other "ziem2" :printme "ziemniak"})
        wrapped1 (fx/wrap-co-effects handler {:otherpayload (fn [] :omghax)})
        full (fx/wrap-effects handler {:printme (fn [e d]
                                                     (println "wrap-effects - e" e)
                                                     (println "wrap-effects - d" d))})]
    (full {:event/type :omghax :payload :mypayload})
    ; (wrapped1 {:event/type :omghax :payload :mypayload})
    ; (wrapped {:event/type :omghax :payload :mypayload :printme "yolo"})
    )
  )


; (def renderer
;   (fx/create-renderer
;     :middleware (fx/wrap-map-desc (fn [state]
;                                     {:fx/type root-view
;                                      :state state}))
;     :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
