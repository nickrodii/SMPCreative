package com.nickrodi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpManager {

    private final SmpCreative plugin;
    private final File file;
    private final FileConfiguration config;
    private final Map<UUID, Map<String, Location>> warps = new HashMap<>();

    public WarpManager(SmpCreative plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create warps.yml!", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadWarps();
    }

    private void loadWarps() {
        warps.clear();
        ConfigurationSection section = config.getConfigurationSection("warps");
        if (section == null) return;

        for (String ownerKey : section.getKeys(false)) {
            UUID owner;
            try {
                owner = UUID.fromString(ownerKey);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning(() -> "Skipping warp owner with invalid UUID: " + ownerKey);
                continue;
            }
            ConfigurationSection ownerSection = section.getConfigurationSection(ownerKey);
            if (ownerSection == null) continue;

            for (String name : ownerSection.getKeys(false)) {
                String base = "warps." + ownerKey + "." + name;
                String worldName = config.getString(base + ".world");
                if (worldName == null) continue;
                World world = Bukkit.getWorld(worldName);
                if (world == null || !worldName.equals(SmpCreative.WORLD_NAME)) {
                    plugin.getLogger().warning(() -> "Skipping warp \"" + name + "\" for " + ownerKey + " because world is missing or not the creative world.");
                    continue;
                }
                double x = config.getDouble(base + ".x");
                double y = config.getDouble(base + ".y");
                double z = config.getDouble(base + ".z");
                float yaw = (float) config.getDouble(base + ".yaw");
                float pitch = (float) config.getDouble(base + ".pitch");
                warps.computeIfAbsent(owner, k -> new HashMap<>())
                        .put(name.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
            }
        }
    }

    public void saveWarp(UUID owner, String name, Location loc) {
        String key = name.toLowerCase();
        String base = "warps." + owner + "." + key;
        config.set(base + ".world", loc.getWorld().getName());
        config.set(base + ".x", loc.getX());
        config.set(base + ".y", loc.getY());
        config.set(base + ".z", loc.getZ());
        config.set(base + ".yaw", loc.getYaw());
        config.set(base + ".pitch", loc.getPitch());
        warps.computeIfAbsent(owner, k -> new HashMap<>()).put(key, loc);
        saveFile();
    }

    public Location getWarp(UUID owner, String name) {
        Map<String, Location> playerWarps = warps.get(owner);
        if (playerWarps == null) return null;
        return playerWarps.get(name.toLowerCase());
    }

    public boolean removeWarp(UUID owner, String name) {
        Map<String, Location> playerWarps = warps.get(owner);
        if (playerWarps == null) return false;
        String key = name.toLowerCase();
        if (!playerWarps.containsKey(key)) return false;
        playerWarps.remove(key);
        config.set("warps." + owner + "." + key, null);
        if (playerWarps.isEmpty()) {
            warps.remove(owner);
            config.set("warps." + owner, null);
        }
        saveFile();
        return true;
    }

    public Set<String> listWarps(UUID owner) {
        Map<String, Location> playerWarps = warps.get(owner);
        if (playerWarps == null) return Collections.emptySet();
        return Collections.unmodifiableSet(playerWarps.keySet());
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save warps.yml!", e);
        }
    }
}
