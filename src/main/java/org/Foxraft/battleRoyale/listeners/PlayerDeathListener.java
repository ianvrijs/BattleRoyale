package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.DeathMessageManager;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.World;

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
        World deathWorld = player.getWorld();

        event.setKeepInventory(false);
        event.setKeepLevel(true);

        // First ensure the player respawns
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.spigot().respawn();

            // Handle game logic after respawn
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                GameState gameState = gameManager.getCurrentState();
                PlayerState playerState = playerManager.getPlayerState(player);

                switch (gameState) {
                    case GRACE -> {
                        Location zeroZeroLocation = new Location(deathWorld, 0,
                                deathWorld.getHighestBlockAt(0, 0).getY() + 2, 0);
                        player.teleport(zeroZeroLocation);
                        resetPlayerState(player);
                    }
                    case STORM -> {
                        if (playerState == PlayerState.ALIVE) {
                            gulagManager.enlistInGulag(player);
                        } else if (playerState == PlayerState.RESURRECTED) {
                            eliminatePlayer(player);
                            player.sendMessage(ChatColor.RED + "You've been eliminated!");
                        } else {
                            eliminatePlayer(player);
                        }
                    }
                    default -> eliminatePlayer(player);
                }


                if (team != null && !gameState.equals(GameState.LOBBY) && gameManager.getTeamManager().isTeamEliminated(team.getId())) {
                    Bukkit.broadcastMessage(deathMessageManager.getTeamEliminatedMessage(team.getName()));
                }
//                playerManager.updatePlayerState(player, gameState);
                gameManager.checkForWinningTeam();
            }, 1L);
        }, 1L);
    }

    private void resetPlayerState(Player player) {
        if (gameManager.getCurrentState() != GameState.GRACE){
            player.getInventory().clear();
        }
        player.setHealth(20);
        player.setFoodLevel(20);
    }

    private void eliminatePlayer(Player player) {
        playerManager.setPlayerState(player, PlayerState.DEAD);
        Location lobbyLocation = gameManager.getLobbyLocation();
        if (lobbyLocation != null && lobbyLocation.getWorld() != null) {
            player.spigot().respawn();
            player.teleport(lobbyLocation);
            resetPlayerState(player);
        }
    }
}