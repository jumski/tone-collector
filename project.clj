(defproject jumski/tone-collector "0.1.0-SNAPSHOT"
  :description "Select and copy samples with your MIDI controller"
  :url "https://github.com/jumski/tone-collector"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :plugins [[lein-binplus "0.6.6"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cljfx "1.6.7"]
                 [overtone/midi-clj "0.5.0"]]
  :main ^:skip-aot jumski.tone-collector.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}
             :dev {:plugins [[lein-binplus "0.6.6"]]}})
