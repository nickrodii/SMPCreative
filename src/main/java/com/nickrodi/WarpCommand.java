package com.nickrodi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.TextComponent;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Hello console or command block... This command is for player use only.", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("smpcreative.warp")) {
            player.sendMessage(Component.text("You don't have permission to use warps.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /warp <name> | add <name> | remove <name> | list", NamedTextColor.RED));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            Set<String> warps = warpManager.listWarps(player.getUniqueId());
            if (warps.isEmpty()) {
                player.sendMessage(Component.text("No warps set.", NamedTextColor.YELLOW));
            } else {
                TextComponent.Builder builder = Component.text();
                boolean first = true;
                for (String warp : warps) {
                    if (!first) builder.append(Component.newline());
                    first = false;
                    builder.append(
                        Component.text(warp, NamedTextColor.AQUA)
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/warp " + warp))
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                    Component.text("Click to warp to " + warp, NamedTextColor.GREEN)))
                    );
                }
                player.sendMessage(builder.build());
            }
            return true;
        }

        if (sub.equals("add") && args.length == 2) {
            if (!player.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
                player.sendMessage(Component.text("You can only set warps in creative!", NamedTextColor.RED));
                return true;
            }
            String name = args[1];
            Location loc = player.getLocation();
            warpManager.saveWarp(player.getUniqueId(), name, loc);
            player.sendMessage(Component.text("Warp \"" + name + "\" saved.", NamedTextColor.GREEN));
            return true;
        }

        if ((sub.equals("remove") || sub.equals("del")) && args.length == 2) {
            String name = args[1];
            if (warpManager.removeWarp(player.getUniqueId(), name)) {
                player.sendMessage(Component.text("Warp \"" + name + "\" removed.", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Warp \"" + name + "\" was not found.", NamedTextColor.RED));
            }
            return true;
        }

        // Teleport to warp: /warp <name>
        String warpName = args[0];
        Location warpLoc = warpManager.getWarp(player.getUniqueId(), warpName);
        if (warpLoc == null) {
            player.sendMessage(Component.text("Warp \"" + warpName + "\" does not exist.", NamedTextColor.RED));
            return true;
        }

        World playerWorld = player.getWorld();
        if (!playerWorld.getName().equals(SmpCreative.WORLD_NAME)) {
            player.sendMessage(Component.text("You can only use warps while in creative!", NamedTextColor.RED));
            return true;
        }

        player.teleport(warpLoc);
        player.sendMessage(Component.text("Teleported to warp \"" + warpName + "\".", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;
        if (!player.hasPermission("smpcreative.warp")) return null;

        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("list");
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("del");
            suggestions.addAll(warpManager.listWarps(player.getUniqueId()));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("del"))) {
            suggestions.addAll(warpManager.listWarps(player.getUniqueId()));
        }
        return suggestions;
    }
}
