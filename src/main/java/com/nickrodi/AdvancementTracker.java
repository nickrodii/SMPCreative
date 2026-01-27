package com.nickrodi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementTracker implements Listener {

    private final SmpCreative plugin;
    private final File file;
    private final FileConfiguration config;

    public AdvancementTracker(SmpCreative plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "advancements.yml");
        
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create advancements.yml!", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    // 1. LISTEN: Record any advancement earned in Creative
    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        // Only track if they are in the Creative World
        if (!player.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
            return;
        }

        // Ignore recipe unlocks (too much spam)
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) {
            return;
        }

        // Add to the "Hit List"
        String key = event.getAdvancement().getKey().toString();
        List<String> hitList = config.getStringList(player.getUniqueId().toString());
        
        if (!hitList.contains(key)) {
            hitList.add(key);
            config.set(player.getUniqueId().toString(), hitList);
            saveFile();
        }
    }

    // 2. CLEAN: Revoke everything on the Hit List
    public void revokeCreativeAdvancements(Player player) {
        String uuid = player.getUniqueId().toString();
        List<String> hitList = config.getStringList(uuid);

        if (hitList.isEmpty()) return;

        int count = 0;
        for (String key : hitList) {
            // FIXED: Added null check for NamespacedKey to satisfy the linter
            NamespacedKey nsKey = NamespacedKey.fromString(key);
            if (nsKey == null) continue;

            Advancement adv = Bukkit.getAdvancement(nsKey);
            if (adv != null) {
                AdvancementProgress progress = player.getAdvancementProgress(adv);
                // Revoke every criteria (this effectively removes the advancement)
                for (String criteria : progress.getAwardedCriteria()) {
                    progress.revokeCriteria(criteria);
                }
                count++;
            }
        }

        // Clear the list now that they are revoked
        config.set(uuid, null); 
        saveFile();
        
        if (count > 0) {
            // FIXED: Using log placeholders instead of string concatenation
            plugin.getLogger().log(Level.INFO, "Revoked {0} creative advancements from {1}", 
                new Object[]{count, player.getName()});
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save advancements.yml!");
        }
    }
}
