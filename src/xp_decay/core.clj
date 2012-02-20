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

(defn jp-init []
  "Initialises the player list"
  (def state {:players (ref (hash-map))}))

(defn jp-onEnable [] nil)

; Timer
(defn add-timer [plugin]
  (let [scheduler (-> plugin .getServer .getScheduler)]
    nil))