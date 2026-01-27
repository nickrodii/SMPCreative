package com.nickrodi;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocationManager {

    private final SmpCreative plugin;
    private final File file;
    private FileConfiguration config; 

    public LocationManager(SmpCreative plugin) {
        this.plugin = plugin;
        
        // ensures data exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        this.file = new File(plugin.getDataFolder(), "locations.yml");
        this.config = new YamlConfiguration();
    
        // if world is missing catch
        try {
            if (file.exists()) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
                for (String line : lines) {
                    if (line.trim().startsWith("world: ")) {
                        String worldName = line.trim().substring(7).replace("'", "").replace("\"", "");
                        if (Bukkit.getWorld(worldName) == null) {
                            throw new IOException("Unknown world found: " + worldName);
                        }
                    }
                }

                config.load(file);
            }
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Corrupted locations.yml, this is due to a missing world. Backing up...");
        
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
            File backupFile = new File(plugin.getDataFolder(), "locations-backup-" + timestamp + ".yml");
        
            try {
                // Move corrupted file
                java.nio.file.Files.move(file.toPath(), backupFile.toPath());
                plugin.getLogger().log(Level.WARNING, () -> "Moved corrupted file to: " + backupFile.getName());
                this.config = new YamlConfiguration(); 
                
            } catch (java.io.IOException moveError) {
                plugin.getLogger().severe("CRITICAL: Could not move the corrupted file.");
            }
        }
    }

    public void saveLocation(UUID uuid, String type, Location loc) {
        String path = uuid.toString() + "." + type;
        config.set(path, loc);
        saveFile();
    }

    public Location getLocation(UUID uuid, String type) {
        String path = uuid.toString() + "." + type;
        return config.getLocation(path);
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save locations.yml.", e);
        }
    }
}
