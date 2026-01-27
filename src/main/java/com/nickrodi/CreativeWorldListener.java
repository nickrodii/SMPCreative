package com.nickrodi;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CreativeWorldListener implements Listener {

    private final SmpCreative plugin;

    public CreativeWorldListener(SmpCreative plugin) { 
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String toWorld = player.getWorld().getName();
        String fromWorld = event.getFrom().getName();

        PlayerDataManager dataManager = plugin.getPlayerDataManager();

        // on creative enter
        if (toWorld.equals(SmpCreative.WORLD_NAME)) {
            dataManager.savePlayerData(player, "survival");
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setGameMode(GameMode.CREATIVE);
            }
            dataManager.loadPlayerData(player, "creative");
            if (plugin.getWorldEditManager().isAvailable()) {
                plugin.getWorldEditManager().enableFor(player);
            }
        } 
        
        // on creative exit
        else if (fromWorld.equals(SmpCreative.WORLD_NAME)) {
            dataManager.savePlayerData(player, "creative");

            // revoke advancements earned while in creative (im proud of coming up with this idea)
            plugin.getAdvancementTracker().revokeCreativeAdvancements(player);
            if (player.getGameMode() != GameMode.SURVIVAL) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            dataManager.loadPlayerData(player, "survival");
            if (plugin.getWorldEditManager().isAvailable()) {
                plugin.getWorldEditManager().disableFor(player);
            }
        }
    }

    /*
    discovered gamerules again lmfao so i dont need this

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        // flatworld slime remover
        if (event.getEntityType() == EntityType.SLIME) {
            
            // check if in creative world
            if (event.getLocation().getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
                
                // allow spawn eggs, no natural gen
                if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    event.setCancelled(true);
                }
            }
        }
    }
    */
    

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // makes sure gamemode matches world they logged into
        if (player.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
            if (player.getGameMode() != GameMode.CREATIVE) player.setGameMode(GameMode.CREATIVE);
            if (plugin.getWorldEditManager().isAvailable()) {
                plugin.getWorldEditManager().enableFor(player);
            }
        } else {
            if (player.getGameMode() != GameMode.SURVIVAL) player.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getWorldEditManager().isAvailable()) {
            plugin.getWorldEditManager().disableFor(player);
        }
    }
}
