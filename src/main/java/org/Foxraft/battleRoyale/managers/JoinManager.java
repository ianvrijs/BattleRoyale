package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class JoinManager {
    private final GameManager gameManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;

    public JoinManager(GameManager gameManager, TeamManager teamManager, PlayerManager playerManager) {
        this.gameManager = gameManager;
        this.teamManager = teamManager;
        this.playerManager = playerManager;
    }

    public void handleJoin(Player player) {
        if (gameManager.getCurrentState() != GameState.GRACE) {
            player.sendMessage(ChatColor.RED + "You can only join during the grace period.");
            return;
        }

        if (teamManager.isPlayerInAnyTeam(player)) {
            player.sendMessage(ChatColor.RED + "You are already in a team.");
            return;
        }

        // Try to find a team with one player
        boolean soloTeamFound = false;
        for (Team team : teamManager.getTeams().values()) {
            if (team.getPlayers().size() == 1) {
                teamManager.addPlayerToTeam(player, team.getId());
                Player teammate = Bukkit.getPlayer(team.getPlayers().get(0));
                if (teammate != null && teammate.isOnline()) {
                    player.teleport(teammate.getLocation());
                    player.sendMessage(ChatColor.GREEN + "You joined team " + ChatColor.GOLD + team.getId() + ChatColor.GREEN + ". Your teammate is " + ChatColor.GOLD + teammate.getName());
                    teammate.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has joined your team!");
                } else {
                    teleportToDefaultLocation(player);
                }
                soloTeamFound = true;
                break;
            }
        }

        if (!soloTeamFound) {
            teamManager.createSoloTeam(player);
            teleportToDefaultLocation(player);
        }

        playerManager.setPlayerState(player, PlayerState.ALIVE);
    }

    private void teleportToDefaultLocation(Player player) {
        String worldName = gameManager.getPlugin().getConfig().getString("lobby.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Error: Could not find world '" + worldName);
            return;
        }

        Location defaultLocation = new Location(world, 0,
                world.getHighestBlockYAt(0, 0) + 2, 0);
        player.teleport(defaultLocation);
    }
}
