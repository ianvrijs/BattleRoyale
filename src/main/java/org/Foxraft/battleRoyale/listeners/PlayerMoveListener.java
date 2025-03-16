package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.states.gulag.GulagState;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerMoveListener implements Listener {
    private final Player player1;
    private final Player player2;
    private final GulagManager gulagManager;
    private boolean isActive = true;

    public PlayerMoveListener(Player player1, Player player2, GulagManager gulagManager) {
        this.player1 = player1;
        this.player2 = player2;
        this.gulagManager = gulagManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive) {
            return;
        }

        Player player = event.getPlayer();
        if (!isGulagParticipant(player)) {
            return;
        }

        if (shouldPreventMovement(player)) {
            preventMovement(event);
            return;
        }

        checkForElimination(player);
    }

    private boolean isGulagParticipant(Player player) {
        return player.equals(player1) || player.equals(player2);
    }

    private boolean shouldPreventMovement(Player player) {
        return gulagManager.isCountdownActive() || gulagManager.getGulagState() != GulagState.ONGOING;
    }

    private void preventMovement(PlayerMoveEvent event) {
        if (hasPlayerMoved(event)) {
            event.setCancelled(true);
        }
    }

    private boolean hasPlayerMoved(PlayerMoveEvent event) {
        return event.getFrom().getX() != Objects.requireNonNull(event.getTo()).getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ();
    }

    private void checkForElimination(Player player) {
        if (player.getLocation().getY() < gulagManager.getEliminationYLevel()) {
            endMatch(player);
        }
    }

    private void endMatch(Player loser) {
        if (!isActive) {
            return;
        }
        isActive = false;

        if (loser != null && loser.isOnline()) {
            Player winner = loser.equals(player1) ? player2 : player1;
            if (winner != null && winner.isOnline()) {
                Bukkit.getScheduler().runTask(gulagManager.getPlugin(), () -> {
                    HandlerList.unregisterAll(this);
                    gulagManager.processGulagResult(winner, loser);
                });
            }
        }
    }
}