package com.vancouver.health;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    private final NamespacedKey recordKey = new NamespacedKey(this, "highest_level");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("HighestLevelHealth enabled for 26.1.2!");
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer data = player.getPersistentDataContainer();
        
        // Get current record, default to 0
        int record = data.getOrDefault(recordKey, PersistentDataType.INTEGER, 0);
        int newLevel = event.getNewLevel();

        // Only update if they hit a NEW peak
        if (newLevel > record) {
            data.set(recordKey, PersistentDataType.INTEGER, newLevel);
            updateHealth(player, newLevel);
        }
    }

    private void updateHealth(Player player, int level) {
        // Calculation: 20 base HP + 2 HP (1 heart) every 5 levels
        double extraHP = Math.floor(level / 5.0) * 2.0;
        double finalHP = 20.0 + extraHP;

        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(finalHP);
            // Optional: send message only on milestones
            if (level % 5 == 0 && level > 0) {
                player.sendMessage("§aMax Health increased! Your record level is now: " + level);
            }
        }
    }
}
