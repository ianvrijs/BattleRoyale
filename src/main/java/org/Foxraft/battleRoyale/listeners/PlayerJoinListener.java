package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    GameState currentState = gameManager.getCurrentState();

    if (currentState == GameState.LOBBY) {
        playerManager.setPlayerState(player, PlayerState.LOBBY);
        player.teleport(gameManager.getLobbyLocation());
    } else if (currentState != GameState.DEATHMATCH && currentState != GameState.STORM) {
        if (!teamManager.isPlayerInAnyTeam(player)) {
            boolean soloTeamFound = false;
            for (Team team : teamManager.getTeams().values()) {
                if (team.getPlayers().size() == 1) {
                    teamManager.addPlayerToTeam(player, team.getId());
                    Player teammate = Bukkit.getPlayer(team.getPlayers().get(0));
                    if (teammate != null && teammate.isOnline()) {
                        player.teleport(teammate.getLocation());
                        player.sendMessage(ChatColor.GREEN + "You have been assigned a random team. Say hi to your teammate "+ ChatColor.GOLD + teammate.getName() + ChatColor.GREEN +"!");
                    } else {
                        player.teleport(new Location(player.getWorld(), 0, player.getWorld().getHighestBlockYAt(0, 0)+2, 0));
                        player.sendMessage(ChatColor.GREEN + "You have been assigned a random team. Your new teammate is currently offline..");

                    }
                    soloTeamFound = true;
                    break;
                }
            }
            if (!soloTeamFound) {
                teamManager.createSoloTeam(player);
                player.teleport(new Location(player.getWorld(), 0, player.getWorld().getHighestBlockYAt(0, 0)+2, 0));
            }
        }
        playerManager.setPlayerState(player, PlayerState.ALIVE);
    } else {
        playerManager.setPlayerState(player, PlayerState.DEAD);
        player.teleport(gameManager.getLobbyLocation());
    }
}
}