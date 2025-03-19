package org.Foxraft.battleRoyale.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private final Map<String, Map<String, Long>> cooldowns = new HashMap<>();
    private static final int INVITE_COOLDOWN = 3; // seconds
    private static final int LIST_COOLDOWN = 3; // seconds

    public boolean hasCooldown(Player player, String command) {
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getName(), k -> new HashMap<>());
        long lastUsage = playerCooldowns.getOrDefault(command, 0L);
        long currentTime = System.currentTimeMillis();

        int cooldownTime = switch (command) {
            case "invite" -> INVITE_COOLDOWN;
            case "list" -> LIST_COOLDOWN;
            default -> 0;
        };

        if (currentTime - lastUsage < cooldownTime * 1000) {
            long remainingSeconds = ((lastUsage + (cooldownTime * 1000)) - currentTime) / 1000;
            player.sendMessage(ChatColor.RED + "Please wait " + remainingSeconds + " seconds before using this command again.");
            return true;
        }

        playerCooldowns.put(command, currentTime);
        return false;
    }
}