package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final GulagManager gulagManager;
    private final PlayerManager playerManager;
    private final GameManager gameManager;

    public PlayerQuitListener(GulagManager gulagManager, PlayerManager playerManager, GameManager gameManager) {
        this.gulagManager = gulagManager;
        this.playerManager = playerManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameState gameState = gameManager.getState();

        gulagManager.handlePlayerLeave(player);

        if ((gameState == GameState.STORM || gameState == GameState.DEATHMATCH)
                && playerManager.getPlayerState(player) == PlayerState.ALIVE) {
            playerManager.setPlayerState(player, PlayerState.DEAD);
            gameManager.checkForWinningTeam();
        }
    }
}