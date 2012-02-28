(ns xpdecay.core
  "Xp decay plugin for Bukkit")

(def ^{:doc "Default decrement of XP"} ^:const DEFAULT_XP_DECREMENT 10)

(def ^{:doc "Strength of the explosion that occurs when XP is zero"} ^:const DEFAULT_EXPLOSION_STRENGTH 0xFFFF)

(def ^{:doc "Whether or not a fire should be set when the explosion occurs"} ^:const DEFAULT_SET_FIRE false)

; Set to level 51 experience - 1
(def ^{:doc "Starting experience for a player"} ^:const ^int DEFAULT_START_EXPERIENCE (+ 7 (bit-shift-right (* 50 7) 1)))

(def ^{:doc "Ticks per second on an ideal server"} ^:const SERVER_TICKS_PER_SEC 20)

(def ^{:doc "Default interval, in seconds, for reducing xp"} ^:const DEFAULT_XP_DECAY_INTERVAL 5)

(def logger (java.util.logging.Logger/getLogger "Minecraft"))

(defrecord Plugin-state [players trigger])

(defn on-zero-xp! [plugin player]
  "Creates an explosion at the player coordinates."
  (.createExplosion (.getWorld plugin) (.getLocation player) DEFAULT_EXPLOSION_STRENGTH DEFAULT_SET_FIRE))

(def state-ref (Plugin-state. (ref (hash-map)) on-zero-xp!))

(defn info [msg]
  (.info logger msg))

(defn debug [msg]
  (.debug logger msg))

(defprotocol RespawnEventHandler
  (on-respawn [respawn-plugin ^org.bukkit.event.player.PlayerRespawnEvent player-event]))

(deftype EventHandler [plugin]
  RespawnEventHandler
  org.bukkit.event.Listener
  (^{EventHandler {:priority EventPriority/NORMAL}}
    on-respawn [respawn-plugin player-event] (info "respawn event")))

(defn on-respawn! [plugin player]
  "Sets the player experience to the defined starting experience"
  (.setExperience player DEFAULT_START_EXPERIENCE))

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
  (let [state @state-ref
        action! (partial reduce-xp! plugin on-zero-xp!)
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
    (.scheduleSyncDelayedTask scheduler plugin #(action-players plugin) SERVER_TICKS_PER_SEC)))

(defn enable-plugin [clj-plugin-loader]
  (info (format "Start xp: %d" DEFAULT_START_EXPERIENCE))
  (let [plugin-manager (-> clj-plugin-loader .getServer .getPluginManager)]
    (.registerEvents plugin-manager (EventHandler. clj-plugin-loader) clj-plugin-loader)))

(defn disable-plugin [clj-plugin-loaded]
  nil)