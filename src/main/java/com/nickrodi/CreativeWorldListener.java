package com.nickrodi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class CreativeWorldListener implements Listener {

    private final SmpCreative plugin;
    private final Map<UUID, Location> lastSafeLocations = new HashMap<>();
    private final Map<UUID, String> lastDeathWorlds = new HashMap<>();

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
            trackSafeLocation(player, player.getLocation());
            if (plugin.getWorldEditManager().isAvailable()) {
                plugin.getWorldEditManager().enableFor(player);
            }
        } 
        
        // on creative exit
        else if (fromWorld.equals(SmpCreative.WORLD_NAME)) {
            dataManager.savePlayerData(player, "creative");
            lastSafeLocations.remove(player.getUniqueId());

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
            trackSafeLocation(player, player.getLocation());
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
        lastSafeLocations.remove(player.getUniqueId());
        lastDeathWorlds.remove(player.getUniqueId());
        if (plugin.getWorldEditManager().isAvailable()) {
            plugin.getWorldEditManager().disableFor(player);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isInCreativeWorld(player)) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        trackSafeLocation(player, to);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!isInCreativeWorld(player)) {
            return;
        }
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        trackSafeLocation(player, to);
    }

    @EventHandler
    public void onCreativeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!isInCreativeWorld(player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            teleportOutOfVoid(player);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        if (world != null) {
            lastDeathWorlds.put(player.getUniqueId(), world.getName());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String deathWorld = lastDeathWorlds.remove(player.getUniqueId());
        if (deathWorld == null || deathWorld.equals(SmpCreative.WORLD_NAME)) {
            return;
        }

        Location respawn = event.getRespawnLocation();
        if (respawn == null || respawn.getWorld() == null) {
            return;
        }

        if (respawn.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
            World targetWorld = plugin.getServer().getWorld(deathWorld);
            if (targetWorld != null) {
                Location fallback = targetWorld.getSpawnLocation();
                fallback.setYaw(respawn.getYaw());
                fallback.setPitch(respawn.getPitch());
                event.setRespawnLocation(fallback);
            }
        }
    }

    private boolean isInCreativeWorld(Player player) {
        World world = player.getWorld();
        return world != null && world.getName().equals(SmpCreative.WORLD_NAME);
    }

    private void trackSafeLocation(Player player, Location location) {
        if (!isInCreativeWorld(player)) {
            return;
        }
        if (location == null) {
            return;
        }
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (location.getY() <= world.getMinHeight() + 2) {
            return;
        }
        if (player.getFallDistance() > 0 && !player.isFlying()) {
            return;
        }

        lastSafeLocations.put(player.getUniqueId(), location.clone());
    }

    private void teleportOutOfVoid(Player player) {
        Location safe = lastSafeLocations.get(player.getUniqueId());
        if (safe == null || safe.getWorld() == null || !safe.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
            safe = getFallbackSafeLocation(player);
        }

        if (safe == null) {
            return;
        }

        Location target = safe.clone();
        Location current = player.getLocation();
        target.setYaw(current.getYaw());
        target.setPitch(current.getPitch());

        player.teleport(target);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
        player.setHealth(player.getMaxHealth());
    }

    private Location getFallbackSafeLocation(Player player) {
        World world = player.getWorld();
        if (world == null) {
            return null;
        }
        Location current = player.getLocation();
        int highestY = world.getHighestBlockYAt(current.getBlockX(), current.getBlockZ());
        return new Location(world, current.getX(), highestY + 1, current.getZ(), current.getYaw(), current.getPitch());
    }
}
