(ns xpdecay.test-command-array-slice
  "Tests the array slice function of the command library."
  (:use clojure.test
        xpdecay.command))

(deftest empty-array-slice
  "Slice of an empty array"
  (let [empty-array (object-array 0)]
    (is (= [] (array-slice empty-array 0 0)))
    (is (= [] (array-slice empty-array -1 5)))
    (is (= [] (array-slice empty-array 6 1)))))

(deftest empty-array-slice
  "Slice of an empty array starting at specified point"
  (let [empty-array (object-array 0)]
    (is (= [] (array-slice empty-array 0)))
    (is (= [] (array-slice empty-array -1)))
    (is (= [] (array-slice empty-array 6)))))

(deftest out-of-bounds-array-slice
  "Out-of-bounds slice indices"
  (let [array (int-array 5 [5 4 3 2 1])]
    (is (= [] (array-slice array 5 6)))
    (is (= [] (array-slice array 8 9)))
    (is (= [] (array-slice array -4 -1)))))

(deftest out-of-bounds-array-slice
  "Out-of-bounds slice indices from the specified start"
  (let [array (int-array 5 [5 4 3 2 1])]
    (is (= [] (array-slice array 5)))))

(deftest partial-bounds-array-slice
  "Partially out-of-bounds slice indices"
  (let [array (int-array 5 [5 4 3 2 1])]
    (is (= [2 1] (array-slice array 3 7)))
    (is (= [5 4] (array-slice array -1 1)))
    (is (= [5] (array-slice array -1 0)))))

(deftest greater-start-array-slice
  "Array slice where start index is greater than the end"
  (is (= [] (array-slice (int-array 4 [1 2 3 4]) 3 2))))

(deftest full-array-slice
  "Full slice of an array"
  (let [test-seq [5 4 3 2 1]
        test-array (int-array (count test-seq) test-seq)]
    (is (= test-seq (array-slice test-array 0 (dec (count test-seq)))))
    (is (= test-seq (array-slice test-array 0)))))

(deftest partial-array-slice
  "Array slice with both indices specified"
  (let [test-seq [5 4 3 2 1]
        test-array (int-array (count test-seq) test-seq)]
    (is (= [5 4 3] (array-slice test-array 0 2)))
    (is (= [4 3 2] (array-slice test-array 1 3)))
    (is (= [3 2 1] (array-slice test-array 2 4)))))

(deftest partial-array-slice-start
  "Array slice with only start specified"
  (let [test-seq [5 4 3 2 1]
        test-array (int-array (count test-seq) test-seq)]
    (is (= [4 3 2 1] (array-slice test-array 1)))
    (is (= [1] (array-slice test-array 4)))))