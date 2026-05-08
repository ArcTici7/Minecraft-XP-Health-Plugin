package com.vancouver.xphearts;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private final NamespacedKey recordKey = new NamespacedKey(this, "highest_level_reached");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("hreset").setExecutor(this);
        getLogger().info("XPHearts 26.1.2 enabled with Reset Command!");
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        checkAndUpdate(event.getPlayer(), event.getNewLevel());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PersistentDataContainer data = event.getPlayer().getPersistentDataContainer();
        int record = data.getOrDefault(recordKey, PersistentDataType.INTEGER, 0);
        applyHealth(event.getPlayer(), record);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("hreset")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("xphearts.reset")) {
                player.sendMessage("§cYou don't have permission (xphearts.reset) to do this!");
                return true;
            }

            // The Reset Logic
            player.getPersistentDataContainer().remove(recordKey);
            
            // Set health back to default 10 hearts (20.0 HP)
            AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(20.0);
            }

            player.sendMessage("§a§l[XPHearts] §7Your health record has been wiped. Your hearts will now scale from your current level.");
            return true;
        }
        return false;
    }

    private void checkAndUpdate(Player player, int currentLevel) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        int record = data.getOrDefault(recordKey, PersistentDataType.INTEGER, 0);

        if (currentLevel > record) {
            data.set(recordKey, PersistentDataType.INTEGER, currentLevel);
            applyHealth(player, currentLevel);
            
            if (currentLevel % 5 == 0 && currentLevel > 0) {
                player.sendMessage("§a§lYou are growing stronger! §7(Record Level: " + currentLevel + ")");
            }
        }
    }

    private void applyHealth(Player player, int level) {
        double extraHP = Math.floor(level / 5.0) * 2.0;
        double finalHP = 20.0 + extraHP;

        if (finalHP > 40.0) finalHP = 40.0; // Max 20 hearts

        AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(finalHP);
        }
    }
}
