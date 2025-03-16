package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.DeathMessageManager;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class listens for player death events and handles player respawning/ elimination.
 * It depends on the GulagManager, PlayerManager, GameManager, and JavaPlugin classes.
 */
public class PlayerDeathListener implements Listener {
    private final GulagManager gulagManager;
    private final PlayerManager playerManager;
    private final GameManager gameManager;
    private final JavaPlugin plugin;
    private final DeathMessageManager deathMessageManager;


    public PlayerDeathListener(GulagManager gulagManager, PlayerManager playerManager, GameManager gameManager, JavaPlugin plugin, DeathMessageManager deathMessageManager) {
        this.gulagManager = gulagManager;
        this.playerManager = playerManager;
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.deathMessageManager = deathMessageManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Team team = gameManager.getTeamManager().getTeam(player);

        event.setKeepInventory(false);
        event.setKeepLevel(true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            GameState gameState = gameManager.getCurrentState();
            PlayerState playerState = playerManager.getPlayerState(player);
            Bukkit.getLogger().info("Player " + player.getName() + " died. Current state: " + playerState + ", Game state: " + gameState);

            switch (gameState) {
                case GRACE:
                    Bukkit.getLogger().info("Respawning player " + player.getName() + " at 0,0 during GRACE period.");
                    respawnAtZeroZero(player);
                    break;
                case STORM:
                    if (playerState == PlayerState.ALIVE) {
                        player.spigot().respawn();
                        gulagManager.enlistInGulag(player);
                    } else {
                        Bukkit.getLogger().info("Eliminating player " + player.getName() + " during STORM period.");
                        eliminatePlayer(player);
                    }
                    break;
                default:
                    Bukkit.getLogger().info("Eliminating player " + player.getName() + " during " + gameState + " period.");
                    eliminatePlayer(player); // deathmatch or lobby
                    break;
            }
            if (team != null && gameManager.getTeamManager().isTeamEliminated(team.getId())) {
                Bukkit.broadcastMessage(deathMessageManager.getTeamEliminatedMessage(team.getName()));
            }
            playerManager.updatePlayerState(player, gameState);
            gameManager.checkForWinningTeam();
        }, 1L);
    }

    private void respawnAtZeroZero(Player player) {
        Location zeroZeroLocation = new Location(player.getWorld(), 0, player.getWorld().getHighestBlockYAt(0, 0) + 2, 0);
        player.spigot().respawn();
        player.teleport(zeroZeroLocation);
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
    }

    private void eliminatePlayer(Player player) {
        playerManager.setPlayerState(player, PlayerState.DEAD);
        Bukkit.getLogger().info("Player state set to: " + playerManager.getPlayerState(player));
        Location lobbyLocation = gameManager.getLobbyLocation();
        player.spigot().respawn();
        player.teleport(lobbyLocation);
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
    }
}