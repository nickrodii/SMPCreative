package com.nickrodi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;



public class WorldEditManager {

    private final SmpCreative plugin;
    private final boolean worldEditPresent;
    private final int blockLimit;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public WorldEditManager(SmpCreative plugin, int blockLimit) {
        this.plugin = plugin;
        this.blockLimit = blockLimit;
        Plugin we = plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        this.worldEditPresent = we != null && we.isEnabled();
        if (!worldEditPresent) {
            plugin.getLogger().info("WorldEdit not detected, disabling WorldEdit features...");
        } else {
            plugin.getLogger().info(() -> "WorldEdit detected with block limit of " + blockLimit + ".");
        }
    }

    public boolean isAvailable() {
        return worldEditPresent;
    }

    public void enableFor(Player player) {
        if (!worldEditPresent) return;

        PermissionAttachment existing = attachments.get(player.getUniqueId());
        if (existing == null) {
            PermissionAttachment attachment = player.addAttachment(plugin);
            attachment.setPermission("worldedit.*", true);
            attachment.setPermission("worldedit.limit.unrestricted", false);
            attachment.setPermission("worldedit.limit." + blockLimit, true);
            attachments.put(player.getUniqueId(), attachment);
            player.updateCommands(); // refresh tab-completions with new permissions
        } else {
            player.updateCommands();
        }
    }

    public void disableFor(Player player) {
        if (!worldEditPresent) return;
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
            player.updateCommands(); // remove permissions, refresh commands
        }
    }

    public void disableAll() {
        if (!worldEditPresent) return;
        for (UUID uuid : attachments.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.removeAttachment(attachments.get(uuid));
                player.updateCommands();
            }
        }
        attachments.clear();
    }
}
