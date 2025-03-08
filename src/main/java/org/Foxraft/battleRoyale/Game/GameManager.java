package org.Foxraft.battleRoyale.Game;

import org.Foxraft.battleRoyale.managers.InviteManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class GameManager {
    private final JavaPlugin plugin;
    private GameState currentState = GameState.LOBBY;
    private final TeamManager teamManager;
    private final InviteManager inviteManager;

    public GameManager(JavaPlugin plugin, TeamManager teamManager, InviteManager inviteManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.inviteManager = inviteManager;
    }

    public void startGame() {
        setState(GameState.STARTING);
        // Start ctountdown
        Bukkit.getScheduler().runTaskLater(plugin, this::startGraceState, 20 * 10); // 10 seconds countdown
    }

    public void stopGame() {
        setState(GameState.LOBBY);
        // Return all players to the lobby
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Teleport player to lobby location
        }
    }

    private void startGraceState() {
        setState(GameState.GRACE);
        // Disable PvP
        // Pause storm
        Bukkit.getScheduler().runTaskLater(plugin, this::startStormState, 20 * 60); // Grace period duration
    }

    private void startStormState() {
        setState(GameState.STORM);
        // Start storm
        // Enable PvP
        // Disable friendly fire between duos
        Bukkit.getScheduler().runTaskLater(plugin, this::startDeathmatchState, 20 * 300); // Storm duration
    }

    private void startDeathmatchState() {
        setState(GameState.DEATHMATCH);
        // Storm idles around 50x50 area
        // Play epic music
        // Check for the winning team
    }

    private void setState(GameState newState) {
        this.currentState = newState;
        Bukkit.getLogger().info("Game state changed to: " + newState);
    }

    public GameState getCurrentState() {
        return currentState;
    }
}