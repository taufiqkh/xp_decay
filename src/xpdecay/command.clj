(ns xpdecay.command
  "Provides command and subcommand processing")

(defrecord Subcommand [subcommand options])

(defn array-slice
  ([array start]
    "Returns a slice of the given array from the start index to the end of the array, or an empty array if start
is greater than the end index."
    (array-slice array start (dec (alength array))))
  ([array start end]
    "Returns a slice of the given array from the start to the end indices as a sequence. If start > end, returns
and empty sequence. Ignores any indices outside the bounds of the array."
    (map #(aget array %) (range (max 0 start) (min (inc end) (alength array))))))

(defn map-subcommand-options [args accepted-options]
  "Processes the arguments given as a subcommand with the sequence of accepted options. The args parameter is expected
to be an array of command arguments where subcommand arguments start at index 1. Returns a map of the option name to
the value found. Any arguments that are additional are mapped from the :_remain keyword."
  (let [num-subcommand-args (dec (alength args))
        start-idx 1]
    (if
      (< num-subcommand-args start-idx) {}
      (let [subcommand-args (array-slice args start-idx)
            matched-options (zipmap (take num-subcommand-args accepted-options) subcommand-args)
            num-accepted-options (count accepted-options)]
        (if (> num-subcommand-args num-accepted-options)
          (assoc matched-options :_remain (take-last (- num-subcommand-args num-accepted-options) subcommand-args))
          matched-options)))))

(defn create-subcommand [args accepted-options]
  "Given a set of command arguments and accepted options, retrieves the subcommand if one exists, and provides a
Subcommand record containing the subcommand as a keyword and its options. accepted-options is a map of each recognised
subcommand to a sequence of options that it accepts. If no subcommand is found, returns :no-args. If a subcommand is
found but not recognised, returns a record with the :unknown subcommand and a sequence containing the remaining
arguments."
  (let [args-length (alength args)]
    (if (= args-length 0) :no-args
      (let [subcommand (keyword (aget args 0))]
        (if (contains? (keys accepted-options) subcommand)
          (Subcommand. subcommand (map-subcommand-options args (get accepted-options subcommand)))
          (Subcommand. :unknown
                      (if (= args-length 1)
                        []
                        (array-slice args 1))))))))
