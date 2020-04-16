(defproject jumski/tone-collector "0.1.0-SNAPSHOT"
  :description "Select and copy samples with your MIDI controller"
  :url "https://github.com/jumski/tone-collector"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins []
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cljfx "1.6.7"]
                 [overtone/midi-clj "0.5.0"]]
  :main ^:skip-aot jumski.tone-collector.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}})
