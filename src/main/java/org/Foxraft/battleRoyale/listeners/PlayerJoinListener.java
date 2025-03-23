package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.TabManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.managers.TimerManager;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PlayerManager playerManager;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    private final TabManager tabManager;
    private final TimerManager timerManager;

    public PlayerJoinListener(GameManager gameManager, TeamManager teamManager, PlayerManager playerManager, TabManager tabManager, TimerManager timerManager) {
        this.playerManager = playerManager;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
        this.tabManager = tabManager;
        this.timerManager = timerManager;
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

        if (currentState == GameState.GRACE) {
            if (teamManager.isPlayerInAnyTeam(player)) {
                timerManager.addPlayer(player);
                teleportToDefaultLocation(player);  // Add this line
            } else {
                player.sendMessage(ChatColor.GREEN + "Welcome! Use " + ChatColor.GOLD + "/br join" + ChatColor.GREEN + " to join the game and get assigned to a team.");
                teleportToLobby(player);
            }
        } else {
            teleportToLobby(player);
        }
    }

    private void teleportToDefaultLocation(Player player) {
        World world = player.getWorld();
        Location defaultLocation = new Location(world, 0,
                world.getHighestBlockYAt(0, 0) + 2, 0);
        player.teleport(defaultLocation);
    }

    private void teleportToLobby(Player player) {
        Location lobbyLoc = gameManager.getLobbyLocation();
        if (lobbyLoc != null && lobbyLoc.getWorld() != null) {
            player.teleport(lobbyLoc);
        }
    }
}