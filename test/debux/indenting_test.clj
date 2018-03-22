(ns debux.indenting-test
  (:require [clojure.test :refer :all]
            [debux.dbgn :refer [dbgn]]
            [debux.common.util :as ut]))

(def traces (atom []))
(def form (atom nil))

(use-fixtures :each (fn [f]
                      (with-redefs [ut/send-trace! (fn [code-trace] (swap! traces conj (update code-trace :form ut/tidy-macroexpanded-form {})))
                                    ut/send-form!  (fn [traced-form] (reset! form (ut/tidy-macroexpanded-form traced-form {})))]
                        (f)
                        (reset! traces [])
                        (reset! form nil))))

(deftest indent-test
  (is (= (dbgn (+ 1 (* 2 3))) 7))
  (is (= '[{:form (* 2 3)
            :indent-level 1
            :result 6}
           {:form (+ 1 (* 2 3))
            :indent-level 0
            :result 7}] @traces)))

(deftest indent-test2
  (dbgn (+ 1 (* 2 3) (+ 4 5)))
  (is (= '[{:form (* 2 3)
            :indent-level 1
            :result 6}
           {:form (+ 4 5)
            :indent-level 1
            :result 9}
           {:form (+ 1 (* 2 3) (+ 4 5))
            :indent-level 0
            :result 16}] @traces)))
