package com.nickrodi;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeleportRequestManager {

    private static final long EXPIRY_SECONDS = 30;

    private final Map<UUID, PendingRequest> requestsByTarget = new HashMap<>();

    public TeleportRequestManager() {}

    public void createRequest(Player requester, Player target) {
        requestsByTarget.put(target.getUniqueId(),
                new PendingRequest(requester.getUniqueId(), target.getUniqueId(),
                        Instant.now().getEpochSecond() + EXPIRY_SECONDS));
    }

    public PendingRequest getRequest(UUID targetId) {
        PendingRequest request = requestsByTarget.get(targetId);
        if (request == null) return null;
        if (request.isExpired()) {
            requestsByTarget.remove(targetId);
            Player target = Bukkit.getPlayer(targetId);
            if (target != null) {
                target.sendMessage(net.kyori.adventure.text.Component.text("Teleport request expired!", net.kyori.adventure.text.format.NamedTextColor.RED));
            }
            return null;
        }
        return request;
    }

    public PendingRequest removeRequest(UUID targetId) {
        return requestsByTarget.remove(targetId);
    }

    public static class PendingRequest {
        private final UUID requesterId;
        private final UUID targetId;
        private final long expiresAtSeconds;

        public PendingRequest(UUID requesterId, UUID targetId, long expiresAtSeconds) {
            this.requesterId = requesterId;
            this.targetId = targetId;
            this.expiresAtSeconds = expiresAtSeconds;
        }

        public UUID getRequesterId() {
            return requesterId;
        }

        public UUID getTargetId() {
            return targetId;
        }

        public boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresAtSeconds;
        }
    }
}
