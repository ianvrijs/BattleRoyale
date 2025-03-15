package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * This class listens for player join events and handles player state changes.
 * It depends on the PlayerManager, TeamManager, and GameManager classes.
 */
public class PlayerJoinListener implements Listener {
    private final PlayerManager playerManager;
    private final TeamManager teamManager;
    private final GameManager gameManager;

    public PlayerJoinListener(GameManager gameManager, TeamManager teamManager, PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
    }
//TODO implement actual handling instead of state handling
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getCurrentState() != GameState.LOBBY) {
            playerManager.setPlayerState(player, PlayerState.DEAD);
        } else if (!teamManager.isPlayerInAnyTeam(player)) {
            playerManager.setPlayerState(player, PlayerState.LOBBY);
        }
    }
}