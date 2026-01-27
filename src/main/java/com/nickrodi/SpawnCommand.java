package com.nickrodi;

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

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Hello console or command block... This command is for player use only.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("smpcreative.spawn")) {
            player.sendMessage(Component.text("You don't have permission to use /spawn.", NamedTextColor.RED));
            return true;
        }

        if (!player.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
            player.sendMessage(Component.text("There is only a spawn in creative.", NamedTextColor.RED));
            return true;
        }

        World creativeWorld = Bukkit.getWorld(SmpCreative.WORLD_NAME);
        if (creativeWorld == null) {
            player.sendMessage(Component.text("Creative world is not loaded.", NamedTextColor.RED));
            return true;
        }

        int highestY = creativeWorld.getHighestBlockYAt(0, 0);
        Location target = new Location(creativeWorld, 0.5, highestY + 1, 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
        player.teleport(target);
        player.sendMessage(Component.text("Teleported to the creative spawn.", NamedTextColor.GREEN));
        return true;
    }
}
