(defproject jumski/tone-collector "0.1.0-SNAPSHOT"
  :description "Label your sample collection without hassle"
  :url "https://github.com/jumski/tone-collector"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[io.aviso/pretty "0.1.37"]]
  :middleware [io.aviso.lein-pretty/inject]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cljfx "1.6.7"]
                 [slingshot "0.12.2"]
                 [io.aviso/pretty "0.1.37"]
                 [expound "0.8.4"]]
  :main ^:skip-aot jumski.tone-collector.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}})
