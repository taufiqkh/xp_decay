(ns xp-decay.core
  "Xp decay plugin for Bukkit"
  (:gen-class
    :name com.quiptiq.xpdecay
    :extends org.bukkit.plugin.java.JavaPlugin
    :prefix jp-
    :state state
    :init init
  (:import org.bukkit.plugin.java.JavaPlugin
           java.util.concurrent.TimeUnit)))

(def ^{:doc "Default decrement of XP"} ^:const DEFAULT_XP_DECREMENT 10)

(def ^{:doc "Strength of the explosion that occurs when XP is zero"} DEFAULT_EXPLOSION_STRENGTH 0xFFFF)

(def ^{:doc "Whether or not a fire should be set when the explosion occurs"} DEFAULT_SET_FIRE false)

(defrecord Plugin-state [players trigger])

(defn on-zero-xp! [plugin player]
  (.createExplosion (.getWorld plugin) (.getLocation player) DEFAULT_EXPLOSION_STRENGTH DEFAULT_SET_FIRE))

(defn reduce-xp! [plugin zero-xp-trigger player]
  "Reduces xp on the specified player and calls zero-xp-trigger if that
player's experience has fallen to zero or below. The zero-xp-trigger function
may have side effects."
  (let [new-xp (max 0 (- (.getExperience player) DEFAULT_XP_DECREMENT))]
    (.setExperience player new-xp)
    (if (= new-xp 0)
      (zero-xp-trigger player))))

(defn action-players [plugin]
  "Actions the players in the given plugin."
  (let [state @(.state plugin)
        action! (partial reduce-xp! plugin (:trigger state))
        players (:players state)]
    (loop [player-names (keys players)]
      (let [[first-player-name & rem-players] player-names
            first-player (first-player-name players)]
        (if-not (nil? (first-player))
          (action! first-player))
        (if-not (empty? rem-players) (recur rem-players))))))

(defn add-timer [plugin]
  "Adds a timer to the given plugin, which when called will reduce XP on that
player and perform any additional triggered actions."
  (let [scheduler (-> plugin .getServer .getScheduler)]
    (.scheduleSyncDelayedTask scheduler plugin #(action-players plugin))))

(defn jp-init [this]
  "Initialises the player list"
  (def state (Plugin-state. (ref (hash-map)) on-zero-xp!)))

(defn jp-onEnable [this] nil)
