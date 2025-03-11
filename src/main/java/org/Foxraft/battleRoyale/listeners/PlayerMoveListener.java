package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.states.gulag.GulagState;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PlayerMoveListener implements Listener {
    private final Player player1;
    private final Player player2;
    private final GulagManager gulagManager;
    private boolean listenerUnregistered = false;

    public PlayerMoveListener(Player player1, Player player2, GulagManager gulagManager) {
        this.player1 = player1;
        this.player2 = player2;
        this.gulagManager = gulagManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (listenerUnregistered) {
            return;
        }

        Player player = event.getPlayer();
        if (player.equals(player1) || player.equals(player2)) {
            if (gulagManager.isCountdownActive() || gulagManager.getGulagState() != GulagState.ONGOING) {
                if (event.getFrom().getX() != Objects.requireNonNull(event.getTo()).getX() ||
                        event.getFrom().getY() != event.getTo().getY() ||
                        event.getFrom().getZ() != event.getTo().getZ()) {
                    event.setCancelled(true);
                }
            }

            if ((int) player.getLocation().getY() < gulagManager.getEliminationYLevel()) {
                Player winner;
                Player loser;
                if (player.equals(player1)) {
                    loser = player1;
                    winner = player2;
                } else {
                    loser = player2;
                    winner = player1;
                }
                gulagManager.handleGulagLoss(loser);
                gulagManager.handleGulagWin(winner);
                listenerUnregistered = true;
                gulagManager.unregisterPlayerMoveListener();
            }
        }
    }
}