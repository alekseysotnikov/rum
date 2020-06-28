(ns daiquiri.interpreter-test
  (:require [clojure.test :refer [are is deftest]]
            [daiquiri.interpreter :refer [interpret]]))

;; Ported from https://github.com/r0man/sablono/blob/master/test/sablono/interpreter_test.cljc

(deftest test-short-hand-div-forms
  (let [el (interpret [:#test.klass1])]
    (is (= "div" (.. el -type)))
    (is (= "test" (.. el -props -id)))
    (is (= "klass1" (.. el -props -className)))))

(deftest test-static-children-as-arguments
  (let [el (interpret
             [:div
              [:div {:class "1" :key 1}]
              [:div {:class "2" :key 2}]])
        c1 (aget (.. el -props -children) 0)
        c2 (aget (.. el -props -children) 1)]
    (is (= "div" (.. el -type)))

    (is (= "div" (.. c1 -type)))
    (is (= "1" (.. c1 -key)))
    (is (= "1" (.. c1 -props -className)))

    (is (= "div" (.. c2 -type)))
    (is (= "2" (.. c2 -key)))
    (is (= "2" (.. c2 -props -className)))))

(deftest test-class-duplication
  (let [el (interpret [:div.a.a.b.b.c {:class "c"}])]
    (is (= "div" (.. el -type)))
    (is (= "a a b b c c" (.. el -props -className)))))

(deftest test-issue-80
  (let [el (interpret
             [:div
              [:div {:class (list "foo" "bar")}]
              [:div {:class (vector "foo" "bar")}]
              (let []
                [:div {:class (list "foo" "bar")}])
              (let []
                [:div {:class (vector "foo" "bar")}])
              (when true
                [:div {:class (list "foo" "bar")}])
              (when true
                [:div {:class (vector "foo" "bar")}])
              (do
                [:div {:class (list "foo" "bar")}])
              (do
                [:div {:class (vector "foo" "bar")}])])]
    (is (= "div" (.. el -type)))
    (is (= 8 (count (.. el -props -children))))
    (doseq [c (.. el -props -children)]
      (is (= "div" (.. c -type)))
      (is (= "foo bar" (.. c -props -className))))))

(deftest test-issue-90
  (let [el (interpret [:div nil (case :a :a "a")])]
    (is (= "div" (.. el -type)))
    (is (= "a" (.. el -props -children)))))

(deftest test-issue-57
  (let [payload {:username "john" :likes 2}
        el (interpret
             (let [{:keys [username likes]} payload]
               [:div
                [:div (str username " (" likes ")")]
                [:div "!Pixel Scout"]]))
        c1 (aget (.. el -props -children) 0)
        c2 (aget (.. el -props -children) 1)]

    (is (= "div" (.. el -type)))

    (is (= "div" (.. c1 -type)))
    (is (= "john (2)" (.. c1 -props -children)))

    (is (= "div" (.. c2 -type)))
    (is (= "!Pixel Scout" (.. c2 -props -children)))))

