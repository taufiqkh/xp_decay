(ns xpdecay.core
  "Xp decay plugin for Bukkit")
  ; TODO: Rewrite with less of an imperative style

(def ^{:doc "Default decrement of XP"} ^:const DEFAULT_XP_DECREMENT 10)

(def ^{:doc "Strength of the explosion that occurs when XP is zero"} ^:const DEFAULT_EXPLOSION_STRENGTH 0xFFFF)

(def ^{:doc "Whether or not a fire should be set when the explosion occurs"} ^:const DEFAULT_SET_FIRE false)

(defn xp-to-next-level [current-level] (+ 7 (bit-shift-right (* current-level 7) 1)))

(def ^{:doc "Starting level for a player"} ^:const ^int DEFAULT_START_LEVEL 50)

; Set to level 50 experience
(def ^{:doc "Starting experience for a player"} ^:const ^int DEFAULT_START_EXPERIENCE
  (reduce + (map xp-to-next-level (range 0 DEFAULT_START_LEVEL))))

(def ^{:doc "Ticks per second on an ideal server"} ^:const SERVER_TICKS_PER_SEC 20)

(def ^{:doc "Default interval, in seconds, for reducing xp"} ^:const DEFAULT_XP_DECAY_INTERVAL 5)

(def logger (java.util.logging.Logger/getLogger "Minecraft"))

(defrecord Plugin-state [players trigger])

(defn on-zero-xp! [plugin player]
  "Creates an explosion at the player coordinates."
  (.sendMessage player "Boom!")
  (.giveExp player DEFAULT_START_EXPERIENCE))
  ;(.createExplosion (.getWorld plugin) (.getLocation player) DEFAULT_EXPLOSION_STRENGTH DEFAULT_SET_FIRE))

(def state-ref (ref (Plugin-state. (sorted-set) on-zero-xp!)))

(defn info [msg & args]
  (.info logger (apply format msg args)))

(defn debug [msg & args]
  (.debug logger (apply format msg args)))

(defn on-death! [plugin player-death-event]
  "Sets the player experience to the defined starting experience"
  (info "respawn event")
  (doto player-death-event
    (.setNewLevel DEFAULT_START_LEVEL)
    (.setNewExp DEFAULT_START_EXPERIENCE)
    (.setNewTotalExp DEFAULT_START_EXPERIENCE)
    (.setDroppedExp 0)))

(definterface DeathEventHandler
  (ondeath [^org.bukkit.event.entity.PlayerDeathEvent player-event]))

(deftype XpDecayEventHandler [plugin]
  org.bukkit.event.Listener
  DeathEventHandler
  (^{org.bukkit.event.EventHandler {:priority org.bukkit.event.EventPriority/NORMAL}}
    ondeath [this ^org.bukkit.event.entity.PlayerDeathEvent player-event] (on-death! plugin player-event)))

(defn reduce-xp! [plugin zero-xp-trigger player]
  "Reduces xp on the specified player and calls zero-xp-trigger if that
player's experience has fallen to zero or below. The zero-xp-trigger function
may have side effects."
  (let [new-xp (max 0 (- (.getTotalExperience player) DEFAULT_XP_DECREMENT))]
    (.giveExp player (- DEFAULT_XP_DECREMENT))
    (info "Xp on %s reduced to %d" (.getName player) new-xp)
    (if (= new-xp 0)
      (zero-xp-trigger plugin player))))

(defn action-players [plugin]
  "Actions the players in the given plugin."
  (let [state @state-ref
        action! (partial reduce-xp! plugin on-zero-xp!)]
    (info "Actioning players %s" (pr-str (:players state)))
    (doseq [player-name (:players state)]
      (let [player (.getPlayer (.getServer plugin) player-name)]
        (if-not (nil? player)
          (action! player))))))

(defn add-timer! [plugin]
  "Adds a timer to the given plugin, which when called will reduce XP on that
player and perform any additional triggered actions."
  (let [scheduler (-> plugin .getServer .getScheduler)]
    (.scheduleAsyncRepeatingTask scheduler plugin #(action-players plugin) SERVER_TICKS_PER_SEC SERVER_TICKS_PER_SEC)))

(defn gen-command-executor []
  (reify org.bukkit.command.CommandExecutor
    (onCommand [this sender command label args]
               (if (.equals "add" (aget args 0)) ;(.hasPermission sender "xpdecay.player.add"))
                 (let [player-name (aget args 1)]
                   (dosync
                     (alter state-ref assoc :players (conj (:players @state-ref) player-name)))
                   (.sendMessage sender (format "Player %s added to the xp decay list." player-name))
                   (info (pr-str @state-ref))
                   (info "Sender %s added %s to the xp decay list" (.getName sender) player-name)
                   true)
                 (do (.sendMessage sender "Unknown subcommand") false)))))

(defn enable-plugin [clj-plugin-loader]
  (info (format "Start xp: %d" DEFAULT_START_EXPERIENCE))
  (.setExecutor (.getCommand clj-plugin-loader "xpd") (gen-command-executor))
  (add-timer! clj-plugin-loader)
  (let [plugin-manager (-> clj-plugin-loader .getServer .getPluginManager)]
    (.registerEvents plugin-manager (XpDecayEventHandler. clj-plugin-loader) clj-plugin-loader)))

(defn disable-plugin [clj-plugin-loaded]
  nil)