package com.nickrodi;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TeleportAcceptCommand implements CommandExecutor {

    private final TeleportRequestManager requestManager;

    public TeleportAcceptCommand(TeleportRequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Hello console or command block... This command is for player use only.", NamedTextColor.RED));
            return true;
        }

        Player target = (Player) sender;

        if (!target.hasPermission("smpcreative.tp.accept")) {
            target.sendMessage(Component.text("You don't have permission to accept teleport requests.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            target.sendMessage(Component.text("Usage: /tpaccept <player>", NamedTextColor.RED));
            target.sendMessage(Component.text("Tip: click the green accept button instead!", NamedTextColor.RED));
            return true;
        }

        TeleportRequestManager.PendingRequest request = requestManager.getRequest(target.getUniqueId());
        if (request == null || !request.getRequesterId().toString().equals(args[0])) {
            target.sendMessage(Component.text("There is no pending request from that player!", NamedTextColor.RED));
            return true;
        }

        Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester == null) {
            target.sendMessage(Component.text("Player is no longer online!", NamedTextColor.RED));
            requestManager.removeRequest(target.getUniqueId());
            return true;
        }

        if (!requester.getWorld().getName().equals(SmpCreative.WORLD_NAME) || !target.getWorld().getName().equals(SmpCreative.WORLD_NAME)) {
            target.sendMessage(Component.text("Both players must be in creative to be able to teleport to each other!", NamedTextColor.RED));
            requestManager.removeRequest(target.getUniqueId());
            return true;
        }

        requestManager.removeRequest(target.getUniqueId());
        requester.teleport(target.getLocation());

        requester.sendMessage(Component.text("Teleport accepted, now teleporting to " + target.getName() + "...", NamedTextColor.GREEN));
        target.sendMessage(Component.text("Accepted teleport from " + requester.getName() + ".", NamedTextColor.GREEN));

        return true;
    }
}
