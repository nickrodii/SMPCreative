package com.nickrodi;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GameModeCommands implements CommandExecutor {

    private final SmpCreative plugin;
    
    public GameModeCommands(SmpCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Hello console or command block... This command is for player use only.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        LocationManager locManager = plugin.getLocationManager();

        // /creative command logic
        if (command.getName().equalsIgnoreCase("creative")) {
            
            // Check if already there
            if (player.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
                player.sendMessage(Component.text("You're already in creative!", NamedTextColor.RED));
                return true; 
            }

            locManager.saveLocation(uuid, "survival", player.getLocation());
            Location lastCreativeLoc = locManager.getLocation(uuid, "creative");
            
            if (lastCreativeLoc != null) {
                player.teleport(lastCreativeLoc);
                player.sendMessage(Component.text("Teleported to creative.", NamedTextColor.GREEN));
            } else {
                World creativeWorld = Bukkit.getWorld(SmpCreative.WORLD_NAME);
                
                if (creativeWorld != null) {
                    player.teleport(creativeWorld.getSpawnLocation());
                    player.sendMessage(Component.text("Teleported to creative, welcome.", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Error: World is not loaded.", NamedTextColor.RED));
                }
            }
            return true;
        }

        // /survival command logic
        if (command.getName().equalsIgnoreCase("survival")) {

            // Check if not in creative world
            if (!player.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
                player.sendMessage(Component.text("You're already in survival!", NamedTextColor.RED));
                return true;
            }

            locManager.saveLocation(uuid, "creative", player.getLocation());
            Location lastSurvivalLoc = locManager.getLocation(uuid, "survival");

            if (lastSurvivalLoc != null) {
                player.teleport(lastSurvivalLoc);
                player.sendMessage(Component.text("Returned to survival.", NamedTextColor.GREEN));
            } else {
                // Fallback
                World mainWorld = Bukkit.getWorlds().get(0); 
                player.teleport(mainWorld.getSpawnLocation());
                player.sendMessage(Component.text("No previous location found, returned to spawn.", NamedTextColor.YELLOW));
            }
            return true;
        }

        return false;
    }
}
