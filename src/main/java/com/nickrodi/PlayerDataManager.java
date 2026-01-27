package com.nickrodi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerDataManager {

    private final SmpCreative plugin;
    private final File file;
    private final FileConfiguration config;

    public PlayerDataManager(SmpCreative plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerdata.yml");
        
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml!", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    // saves all player data
    public void savePlayerData(Player player, String group) {
        String path = player.getUniqueId() + "." + group;

        config.set(path + ".inventory", Arrays.asList(player.getInventory().getContents()));
        config.set(path + ".armor", Arrays.asList(player.getInventory().getArmorContents()));
        config.set(path + ".enderchest", Arrays.asList(player.getEnderChest().getContents()));
        
        config.set(path + ".health", player.getHealth());
        config.set(path + ".food", player.getFoodLevel());
        config.set(path + ".xp_level", player.getLevel());
        config.set(path + ".xp_points", player.getExp());
        config.set(path + ".effects", player.getActivePotionEffects());
        
        saveFile();
    }

    // loads player data
    @SuppressWarnings("unchecked")
    public void loadPlayerData(Player player, String group) {
        String path = player.getUniqueId() + "." + group;

        try {
            if (file.exists()) config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().warning("Could not reload playerdata.yml from disk.");
        }

        // wipe data
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getEnderChest().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setExp(0);
        player.setLevel(0);
        player.setFoodLevel(20);
        player.setHealth(20);

        // if no data exists its just a fresh start
        if (!config.contains(path)) {
            return;
        }

        // restore player data
        try {
            List<ItemStack> inventory = (List<ItemStack>) config.getList(path + ".inventory");
            if (inventory != null && !inventory.isEmpty()) {
                player.getInventory().setContents(inventory.toArray(ItemStack[]::new));
            }

            List<ItemStack> armor = (List<ItemStack>) config.getList(path + ".armor");
            if (armor != null && !armor.isEmpty()) {
                player.getInventory().setArmorContents(armor.toArray(ItemStack[]::new));
            }

            List<ItemStack> enderChest = (List<ItemStack>) config.getList(path + ".enderchest");
            if (enderChest != null && !enderChest.isEmpty()) {
                player.getEnderChest().setContents(enderChest.toArray(ItemStack[]::new));
            }

            List<PotionEffect> effects = (List<PotionEffect>) config.getList(path + ".effects");
            if (effects != null) player.addPotionEffects(effects);

            player.setHealth(Math.min(config.getDouble(path + ".health", 20), 20));
            player.setFoodLevel(config.getInt(path + ".food", 20));
            player.setLevel(config.getInt(path + ".xp_level", 0));
            player.setExp((float) config.getDouble(path + ".xp_points", 0));

        } catch (ClassCastException | IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, e, () -> "Error loading data for " + player.getName());
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml!", e);
        }
    }
}
