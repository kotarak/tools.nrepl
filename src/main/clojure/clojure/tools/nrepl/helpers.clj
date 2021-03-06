;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns ^{:author "Chas Emerick"}
  clojure.tools.nrepl.helpers
  (:import (java.io File StringReader)))

(defn load-file-command
  "Returns a string expression that can be sent to an nREPL session to
   load the code in given local file in the remote REPL's environment,
   preserving debug information (e.g. line numbers, etc).

   Typical usage: ((:send connection)
                    (load-file-command (java.io.File. \"/path/to/clojure/file.clj\")))

   If appropriate, the source path from which the code is being loaded may
   be provided as well (suitably trimming the file's path to a relative one
   when loaded).

   The 3-arg variation of this function expects the full source of the file to be loaded,
   the source-root-relative path of the source file, and the name of the file.  e.g.:

     (load-file-command \"…code here…\" \"some/ns/name/file.clj\" \"file.clj\")"
  ([f] (load-file-command f nil))
  ([f source-root]
    (let [abspath (if (string? f) f (.getAbsolutePath f))
          source-root (cond
                          (nil? source-root) ""
                          (string? source-root) source-root
                          (instance? File source-root) (.getAbsolutePath source-root))]
      (load-file-command (slurp abspath "UTF-8")
        (if (and (seq source-root)
              (.startsWith abspath source-root))
          (-> abspath
            (.substring (count source-root))
            (.replaceAll "^[/\\\\]" ""))
          abspath)
        (-> abspath File. .getName))))
  ([code file-path file-name]
    (apply format
      "(clojure.lang.Compiler/load (java.io.StringReader. %s) %s %s)"
      (map pr-str [code file-path file-name]))))