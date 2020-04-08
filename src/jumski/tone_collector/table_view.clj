(ns jumski.tone-collector.table-view
  (:require [cljfx.api :as fx]))

(defn wav-files-in-dir
  "Returns lazy sequence of java.io.File in given `dir`."
  [dir]
  (->> (clojure.java.io/file dir)
       file-seq
       (sequence only-wav-files)))

(def only-wav-files
  "Transducer, filtering only paths to wav files, based on extension."
  (let [wav-regex #"(?i).*\.wav$"]
    (comp (filter #(.isFile ^java.io.File %))
          (filter #(re-matches wav-regex (.getPath ^java.io.File %))))))

(def *state (atom {:values (wav-files-in-dir "resources/")}))

(defn root-view [{:keys [state]}]
  {:fx/type :stage
   :showing true
   :width 600
   :height 800
   :x 10
   :y 10
   :scene {:fx/type :scene
           :root {:fx/type :table-view
                  :columns [{:fx/type :table-column
                             :text "name"
                             :cell-value-factory identity
                             :cell-factory (fn [x]
                                             {:text (.getName x)})}
                            {:fx/type :table-column
                             :text "path"
                             :cell-value-factory identity
                             :cell-factory (fn [x]
                                             {:text (.getPath x)})}]
                  :items (:values state)}}})

(defmulti event-handler :event/type)

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state]
                                    {:fx/type root-view
                                     :state state}))
    :opts {:fx.opt/map-event-handler event-handler}))

(fx/mount-renderer *state renderer)
