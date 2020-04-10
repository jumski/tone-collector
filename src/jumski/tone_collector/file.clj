(ns jumski.tone-collector.file)

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
