package com.nickrodi;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TeleportDeclineCommand implements CommandExecutor {

    private final TeleportRequestManager requestManager;

    public TeleportDeclineCommand(TeleportRequestManager requestManager) {
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
            target.sendMessage(Component.text("You don't have permission to decline teleport requests.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            target.sendMessage(Component.text("Usage: /tpdecline <player>", NamedTextColor.RED));
            target.sendMessage(Component.text("Tip: click the red decline button instead!", NamedTextColor.RED));
            return true;
        }

        TeleportRequestManager.PendingRequest request = requestManager.getRequest(target.getUniqueId());
        if (request == null || !request.getRequesterId().toString().equals(args[0])) {
            target.sendMessage(Component.text("There is no pending request from that player!", NamedTextColor.RED));
            return true;
        }

        requestManager.removeRequest(target.getUniqueId());

        Player requester = Bukkit.getPlayer(request.getRequesterId());
        String requesterName = requester != null ? requester.getName() : "that player";

        target.sendMessage(Component.text("Declined teleport from " + requesterName + ".", NamedTextColor.YELLOW));
        if (requester != null) {
            requester.sendMessage(Component.text(target.getName() + " declined your teleport request.", NamedTextColor.RED));
        }
        return true;
    }
}
