package org.Foxraft.battleRoyale.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.Foxraft.battleRoyale.config.GameManagerConfig;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;

public class PlayerDeathListener implements Listener {
    private final GameManagerConfig config;
    private final GulagManager gulagManager;
    private final PlayerManager playerManager;

    public PlayerDeathListener(GameManagerConfig config) {
        this.config = config;
        this.gulagManager = config.getGulagManager();
        this.playerManager = config.getPlayerManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.setKeepInventory(false);
        event.setKeepLevel(true);

        Bukkit.getScheduler().runTaskLater(config.getPlugin(), () -> {
            GameState gameState = config.getCurrentGameState();
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
                        Bukkit.getLogger().info("Enlisting player " + player.getName() + " in Gulag during STORM period.");
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
            playerManager.updatePlayerState(player, gameState);
        }, 1L); // 1 tick delay for force respawn
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
        Location lobbyLocation = config.getGameManager().getLobbyLocation();
        player.spigot().respawn();
        player.teleport(lobbyLocation);
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
    }
}