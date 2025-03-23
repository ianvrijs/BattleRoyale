package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.TabManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PlayerManager playerManager;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    private final TabManager tabManager;

    public PlayerJoinListener(GameManager gameManager, TeamManager teamManager, PlayerManager playerManager, TabManager tabManager) {
        this.playerManager = playerManager;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
        this.tabManager = tabManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameState currentState = gameManager.getCurrentState();

        PlayerState state = (currentState == GameState.GRACE && teamManager.isPlayerInAnyTeam(player))
                ? PlayerState.ALIVE
                : PlayerState.LOBBY;

        playerManager.setPlayerState(player, state);
        tabManager.initializePlayer(player, state);
        if (currentState != GameState.GRACE || !teamManager.isPlayerInAnyTeam(player)) {
            Location lobbyLoc = gameManager.getLobbyLocation();
            if (lobbyLoc != null && lobbyLoc.getWorld() != null) {
                player.teleport(lobbyLoc);
            }
            if (currentState == GameState.GRACE) {
                player.sendMessage(ChatColor.GREEN + "Welcome! Use " + ChatColor.GOLD + "/br join" + ChatColor.GREEN + " to join the game and get assigned to a team.");
            }
        }
    }

    private ChatColor getStateColor(PlayerState state) {
        return switch (state) {
            case ALIVE -> ChatColor.GREEN;
            case DEAD -> ChatColor.RED;
            case LOBBY -> ChatColor.GRAY;
            case GULAG -> ChatColor.YELLOW;
            case RESURRECTED -> ChatColor.AQUA;
        };
    }
}