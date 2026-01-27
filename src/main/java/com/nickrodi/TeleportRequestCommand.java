package com.nickrodi;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TeleportRequestCommand implements CommandExecutor {

    private final TeleportRequestManager requestManager;

    public TeleportRequestCommand(TeleportRequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Hello console or command block... This command is for player use only.", NamedTextColor.RED));
            return true;
        }

        Player requester = (Player) sender;

        if (!requester.hasPermission("smpcreative.tp")) {
            requester.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            requester.sendMessage(Component.text("Usage: /tp <player>", NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            requester.sendMessage(Component.text("That player is not online.", NamedTextColor.RED));
            return true;
        }

        World requesterWorld = requester.getWorld();
        World targetWorld = target.getWorld();
        if (!requesterWorld.getName().equals(SmpCreative.WORLD_NAME) || !targetWorld.getName().equals(SmpCreative.WORLD_NAME)) {
            requester.sendMessage(Component.text("Both players must be in creative to be able to teleport to each other!", NamedTextColor.RED));
            return true;
        }

        if (requester.getUniqueId().equals(target.getUniqueId())) {
            requester.sendMessage(Component.text("Uhh... you cannot send a request to yourself.", NamedTextColor.RED));
            return true;
        }

        requestManager.createRequest(requester, target);

        requester.sendMessage(Component.text("Teleport request sent to " + target.getName() + ".", NamedTextColor.GREEN));

        Component message = Component.text(requester.getName() + " wants to teleport to you! ", NamedTextColor.YELLOW)
                .append(Component.text("[ACCEPT]", NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tpaccept " + requester.getUniqueId()))
                        .hoverEvent(HoverEvent.showText(Component.text("Accept teleport request", NamedTextColor.GREEN))))
                .append(Component.text(" "))
                .append(Component.text("[DECLINE]", NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/tpdecline " + requester.getUniqueId()))
                        .hoverEvent(HoverEvent.showText(Component.text("Decline teleport request", NamedTextColor.RED))));

        target.sendMessage(message);
        return true;
    }
}
