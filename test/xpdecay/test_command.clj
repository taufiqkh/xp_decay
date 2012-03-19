(ns xpdecay.test-command
  "Tests the command library."
  (:use xpdecay.command
        clojure.test))

; Test basic functionality, with options providing simple name mapping.
(deftest map-subcommand-options-both-empty
  (let [args (object-array ["dummy"])
        empty-options []]
    (is (= {} (map-subcommand-options args empty-options)))))

(deftest map-subcommand-options-one
  (let [args (object-array ["dummy" "foo"])
        options [:first]]
    (is (= {:first "foo"} (map-subcommand-options args options)))))

(deftest map-subcommand-options-more-options
  "When more options than are arguments are provided, only returns the options for the arguments."
  (let [options [:first :second]]
    (is (= {:first "foo"} (map-subcommand-options (object-array ["dummy" "foo"]) options)))
    (is (= {} (map-subcommand-options (object-array 0) options)))))

(deftest map-subcommand-options-more-arguments
  "When more arguments than are options are provided, returns the options for the arguments and the remainder in a sequence."
  (let [args-vec ["dummy" "foo" "whee" "blargh" "shwoop"]
        args (object-array args-vec)]
    (is (= {:first "foo", :second "whee", :_remain ["blargh" "shwoop"]}
           (map-subcommand-options args [:first :second])))
    (is (= {:_remain (rest args-vec)} (map-subcommand-options args [])))))