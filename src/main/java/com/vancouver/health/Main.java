package com.vancouver.xphearts;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private final NamespacedKey recordKey = new NamespacedKey(this, "highest_level_reached");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("XPHearts 26.1.2 (Java 25) enabled!");
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        checkAndUpdate(event.getPlayer(), event.getNewLevel());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Sync health when they join in case something changed
        PersistentDataContainer data = event.getPlayer().getPersistentDataContainer();
        int record = data.getOrDefault(recordKey, PersistentDataType.INTEGER, 0);
        applyHealth(event.getPlayer(), record);
    }

    private void checkAndUpdate(Player player, int currentLevel) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        int record = data.getOrDefault(recordKey, PersistentDataType.INTEGER, 0);

        if (currentLevel > record) {
            data.set(recordKey, PersistentDataType.INTEGER, currentLevel);
            applyHealth(player, currentLevel);
            
            // Notification message for milestones (every 5 levels as per your config)
            if (currentLevel % 5 == 0) {
                player.sendMessage("§a§lYou are growing stronger! §7(Record Level: " + currentLevel + ")");
            }
        }
    }

    private void applyHealth(Player player, int level) {
        // MATCHING YOUR XP2HP CONFIG:
        // Level 0 = 20HP, Level 5 = 22HP, Level 10 = 24HP...
        // Formula: 20 + (level / 5) * 2
        double extraHP = Math.floor(level / 5.0) * 2.0;
        double finalHP = 20.0 + extraHP;

        // Cap it at 40HP (Level 50) as per your config
        if (finalHP > 40.0) finalHP = 40.0;

        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(finalHP);
        }
    }
}
