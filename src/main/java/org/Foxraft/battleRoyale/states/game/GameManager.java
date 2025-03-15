package org.Foxraft.battleRoyale.states.game;

import org.Foxraft.battleRoyale.events.StormReachedFinalDestinationEvent;
import org.Foxraft.battleRoyale.listeners.GracePeriodListener;
import org.Foxraft.battleRoyale.managers.InviteManager;
import org.Foxraft.battleRoyale.managers.StormManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.Foxraft.battleRoyale.utils.StartUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
/**
 * This class manages the game state and transitions between game states.
 * It depends on the PlayerManager, TeamManager, StormManager and StartUtils classes.
 */
public class GameManager implements Listener {
    private final JavaPlugin plugin;
    private final PlayerManager playerManager;
    private final StartUtils startUtils;
    private final GracePeriodListener gracePeriodListener = new GracePeriodListener();
    private final TeamManager teamManager;
    private GameState currentState = GameState.LOBBY;
    private final StormManager stormManager;

    public GameManager(JavaPlugin plugin, PlayerManager playerManager, TeamManager teamManager, StartUtils startUtils, StormManager stormManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.teamManager = teamManager;
        this.startUtils = startUtils;
        this.stormManager = stormManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onStormReachedFinalDestination(StormReachedFinalDestinationEvent event) {
        startDeathmatchState();
    }
    //TODO set all online players without a team inside a team, except for exempt players
    public void startGame(CommandSender sender) {
        if (currentState != GameState.LOBBY) {
            sender.sendMessage(ChatColor.RED + "Game has already been started.");
            return;
        }
        setState(GameState.STARTING);
        startUtils.generateSpawnLocationsAndTeleportTeams();
        startUtils.broadcastCountdown(5);

        int graceTimeMinutes;
        try {
            graceTimeMinutes = plugin.getConfig().getInt("graceTime");
            if (graceTimeMinutes <= 0) {
                sender.sendMessage(ChatColor.RED + "Grace time must be a positive integer.");
                return;
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Invalid graceTime in config: " + e.getMessage());
            return;
        }

        final long graceTimeTicks = graceTimeMinutes * 60L * 20L;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startUtils.teleportTeamsToSpawnLocations();
            startGraceState(graceTimeTicks);
        }, 5 * 20); // wait 5s for countdown to finish

        sender.sendMessage(ChatColor.GREEN + "Game started successfully with a grace period of " + graceTimeMinutes + " minutes.");

        // Set player state to ALIVE for all players in teams
        for (Team team : teamManager.getTeams().values()) {
            for (String playerName : team.getPlayers()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    playerManager.setPlayerState(player, PlayerState.ALIVE);
                }
            }
        }
    }
    public void stopGame() {
        if (currentState == GameState.LOBBY) {
            Bukkit.broadcastMessage(ChatColor.RED + "There's no active game to stop..");
            return;
        }
        setState(GameState.LOBBY);
        stormManager.stopStorm();
        String worldName = plugin.getConfig().getString("lobby.world");
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        assert worldName != null;
        Location lobbyLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
        //TODO unregister gulag listener
        for (Team team : teamManager.getTeams().values()) {
            for (String playerName : team.getPlayers()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.teleport(lobbyLocation);
                    player.getInventory().clear();
                    player.setHealth(20);
                    player.setFoodLevel(20);
                    playerManager.setPlayerState(player, PlayerState.LOBBY);
                    player.sendMessage(ChatColor.RED + "Game stopped. Teleported to lobby.");
                }
            }
        }
    }

    private void startGraceState(long graceTimeTicks) {
        setState(GameState.GRACE);
        PluginManager pluginManager = Bukkit.getPluginManager();

        //disable pvp and enable keepinv
        pluginManager.registerEvents(gracePeriodListener, plugin);
        Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.KEEP_INVENTORY, true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // logic once grace period ends
            HandlerList.unregisterAll(gracePeriodListener);
            Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.KEEP_INVENTORY, false);
            startStormState();
        }, graceTimeTicks);
    }

    private void startStormState() {
        if (currentState == GameState.STORM) {
            Bukkit.getLogger().info("Storm state is already active. Ignoring startStormState() call.");
            return;
        }
        setState(GameState.STORM);
        stormManager.startStorm();
    }

    //TODO implement deathmatch utils
    private void startDeathmatchState() {
        setState(GameState.DEATHMATCH);
        //eliminate all players in sumo queue
        //flicker border color
    }

    private void setState(GameState newState) {
        this.currentState = newState;
        Bukkit.getLogger().info("Game state changed to: " + newState);

        String message = ChatColor.GREEN + "The game state has changed to: " + newState;
        for (Team team : teamManager.getTeams().values()) {
            for (String playerName : team.getPlayers()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public Location getLobbyLocation() {
        String worldName = plugin.getConfig().getString("lobby.world", "world");
        double x = plugin.getConfig().getDouble("lobby.x", 0);
        double y = plugin.getConfig().getDouble("lobby.y", 64);
        double z = plugin.getConfig().getDouble("lobby.z", 0);
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}