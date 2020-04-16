(defproject jumski/tone-collector "0.1.0-SNAPSHOT"
  :description "Label your sample collection without hassle"
  :url "https://github.com/jumski/tone-collector"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cljfx "1.6.7"]
                 [overtone/midi-clj "0.5.0"]]
  :main ^:skip-aot jumski.tone-collector.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :native-image {:name "tone-collector"
                                      :jvm-opts ["-Dclojure.compiler.direct-linking=true"]
                                      :opts ["--verbose"
                                             "--no-server"
                                             "-H:+ReportUnsupportedElementsAtRuntime"]}}})
