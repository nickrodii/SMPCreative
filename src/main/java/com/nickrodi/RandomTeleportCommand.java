package com.nickrodi;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class RandomTeleportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Hello console or command block... This command is for player use only.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("smpcreative.rtp")) {
            player.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        World world = player.getWorld();
        if (!world.getName().equals(SmpCreative.WORLD_NAME)) {
            player.sendMessage(Component.text("You can only use /rtp in creative!", NamedTextColor.RED));
            return true;
        }

        WorldBorder border = world.getWorldBorder();
        double radius = Math.max(0, (border.getSize() / 2) - 1);
        Location center = border.getCenter();

        double randomX = center.getX() + ThreadLocalRandom.current().nextDouble(-radius, radius);
        double randomZ = center.getZ() + ThreadLocalRandom.current().nextDouble(-radius, radius);

        int highestY = world.getHighestBlockYAt((int) Math.round(randomX), (int) Math.round(randomZ));
        Location target = new Location(world, randomX, highestY + 1, randomZ, player.getLocation().getYaw(), player.getLocation().getPitch());

        player.teleport(target);
        player.sendMessage(Component.text("You've been teleported to a blank slate.", NamedTextColor.GREEN));
        return true;
    }
}
