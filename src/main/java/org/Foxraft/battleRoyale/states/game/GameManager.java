package org.Foxraft.battleRoyale.states.game;

import org.Foxraft.battleRoyale.events.StormReachedFinalDestinationEvent;
import org.Foxraft.battleRoyale.listeners.GracePeriodListener;
import org.Foxraft.battleRoyale.listeners.TeamDamageListener;
import org.Foxraft.battleRoyale.managers.*;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.gulag.GulagState;
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
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
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
    private final TeamDamageListener teamDamageListener;
    private final GulagManager gulagManager;
    private GameState currentState = GameState.LOBBY;
    private final StormManager stormManager;
    private final TimerManager timerManager;
    private final TabManager tabManager;
    private int gracePeriodTaskId = -1;

    public GameManager(JavaPlugin plugin, PlayerManager playerManager, TeamManager teamManager, StartUtils startUtils, TeamDamageListener teamDamageListener, StormManager stormManager, GulagManager gulagManager, TimerManager timerManager, TabManager tabManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.teamManager = teamManager;
        this.startUtils = startUtils;
        this.teamDamageListener = teamDamageListener;
        this.stormManager = stormManager;
        this.gulagManager = gulagManager;
        this.timerManager = timerManager;
        this.tabManager = tabManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onStormReachedFinalDestination(StormReachedFinalDestinationEvent event) {
        Bukkit.getLogger().info("StormReachedFinalDestinationEvent received");
        startDeathmatchState();
    }
    public void startGame(CommandSender sender) {
        if (currentState != GameState.LOBBY) {
            sender.sendMessage(ChatColor.RED + "Game has already been started.");
            return;
        }

        List<Player> playersWithoutTeam = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!teamManager.isPlayerInAnyTeam(player)) {
                playersWithoutTeam.add(player);
            }
        }

        for (int i = 0; i < playersWithoutTeam.size(); i += 2) {
            if (i + 1 < playersWithoutTeam.size()) {
                Player player1 = playersWithoutTeam.get(i);
                Player player2 = playersWithoutTeam.get(i + 1);
                teamManager.createTeam(player1, player2);
                player1.sendMessage(ChatColor.GREEN + "You have been put into a new team with " + player2.getName());
                player2.sendMessage(ChatColor.GREEN + "You have been put into a new team with " + player1.getName());
            } else {
                teamManager.createSoloTeam(playersWithoutTeam.get(i));
            }
        }

        // Count active teams
        int activeTeams = (int) teamManager.getTeams().values().stream()
                .filter(team -> team.getPlayers().stream()
                        .map(Bukkit::getPlayer).anyMatch(Objects::nonNull))
                .count();

        if (activeTeams < 2) {
            sender.sendMessage(ChatColor.RED + "At least 2 teams are required to start (current: " + activeTeams + ")");
            return;
        }

        setState(GameState.STARTING);
        sender.sendMessage(ChatColor.GREEN + "Generating spawn locations... this might take a minute.");
        startUtils.generateSpawnLocationsAndTeleportTeams();
        startUtils.broadcastCountdown(5);

        // Set player states
        for (Team team : teamManager.getTeams().values()) {
            for (String playerName : team.getPlayers()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    playerManager.setPlayerState(player, PlayerState.ALIVE);
                }
            }
        }

        // Set grace
        int graceTimeMinutes = plugin.getConfig().getInt("graceTime", 5);
        final long graceTimeTicks = graceTimeMinutes * 60L * 20L;

        // TP after countdown
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                startUtils.teleportTeamsToSpawnLocations();
                startGraceState(graceTimeTicks);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error during teleportation: " + e.getMessage());
                stopGame();
            }
        }, 5 * 20);
    }
    public void stopGame() {
        if (currentState == GameState.LOBBY) {
            Bukkit.broadcastMessage(ChatColor.RED + "There's no active game to stop..");
            return;
        }
        setState(GameState.LOBBY);
        stormManager.stopStorm();
        timerManager.stop();
        gulagManager.clearGulag();

        Location lobbyLocation = getLobbyLocation();
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
        // Cancel grace period task if running
        if (gracePeriodTaskId != -1) {
            Bukkit.getScheduler().cancelTask(gracePeriodTaskId);
            gracePeriodTaskId = -1;
        }
    }

    private void startGraceState(long graceTimeTicks) {
        setState(GameState.GRACE);
        PluginManager pluginManager = Bukkit.getPluginManager();

        // Disable PvP and enable keep inventory
        pluginManager.registerEvents(gracePeriodListener, plugin);
        Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.KEEP_INVENTORY, true);

        gracePeriodTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Logic once grace period ends
            HandlerList.unregisterAll(gracePeriodListener);
            Objects.requireNonNull(Bukkit.getWorld("world")).setGameRule(GameRule.KEEP_INVENTORY, false);
            startStormState();
        }, graceTimeTicks).getTaskId();
    }

    private void startStormState() {
        if (currentState == GameState.STORM) {
            Bukkit.getLogger().info("Storm state is already active. Ignoring startStormState() call.");
            return;
        }
        setState(GameState.STORM);
        stormManager.startStorm();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(teamDamageListener, plugin);
    }

    //TODO implement deathmatch utils
    private void startDeathmatchState() {
        setState(GameState.DEATHMATCH);
        gulagManager.clearGulag();
        // TODO: Implement border color flicker logic
    }

    private void setState(GameState newState) {
        this.currentState = newState;
        Bukkit.getLogger().info("Game state changed to: " + newState);

        // Start timer for new state
        timerManager.startTimer(getCurrentState());

        tabManager.updateHeaderFooter(newState);
        for (Player player : Bukkit.getOnlinePlayers()) {
            tabManager.updatePlayerTab(player, playerManager.getPlayerState(player));
        }
        String message = switch (newState) {
            case STARTING -> ChatColor.GREEN + "⚔ Prepare for battle! The game is starting...";
            case GRACE -> ChatColor.YELLOW + "☮ Grace period has begun! Gather resources and prepare your strategy.";
            case STORM -> ChatColor.RED + "⚡ The storm is approaching! PvP has been enabled!";
            case DEATHMATCH -> ChatColor.GOLD + "☠ Final showdown! May the best team win!";
            case LOBBY -> ChatColor.AQUA + "✦ Game ended - returning to lobby.";
            default -> ChatColor.GREEN + "Game state: " + newState;
        };

        Bukkit.broadcastMessage(message);
        if (newState == GameState.STORM) {
            Bukkit.getPluginManager().registerEvents(teamDamageListener, plugin);
        } else if (newState == GameState.LOBBY) {
            HandlerList.unregisterAll(teamDamageListener);
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
    public void checkForWinningTeam() {
        // Wait 1 tick to ensure the gulag queue is updated
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<Team> teamsWithPotential = new ArrayList<>();

            // Check each team for alive or gulag players
            for (Team team : teamManager.getTeams().values()) {
                boolean hasAliveOrGulagPlayer = false;
                for (String playerName : team.getPlayers()) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        PlayerState state = playerManager.getPlayerState(player);
                        if (state == PlayerState.ALIVE || state == PlayerState.GULAG) {
                            hasAliveOrGulagPlayer = true;
                            break;
                        }
                    }
                }
                if (hasAliveOrGulagPlayer) {
                    teamsWithPotential.add(team);
                }
            }

            // End game conditions
            if (teamsWithPotential.size() == 1) {
                Team lastTeam = teamsWithPotential.get(0);

                switch (currentState) {
                    case STORM:
                        if ((gulagManager.getGulagQueue().size() <= 1) &&
                                gulagManager.getGulagState() == GulagState.IDLE) {
                            endGame(lastTeam);
                        }
                        break;
                    case DEATHMATCH:
                        endGame(lastTeam);
                        break;
                }
            }
        }, 1L);
    }

    private void endGame(Team winningTeam) {
        stormManager.stopStorm();
        timerManager.stop();
        gulagManager.clearGulag();

        StringBuilder winners = new StringBuilder();
        for (String playerName : winningTeam.getPlayers()) {
            if (!winners.isEmpty()) {
                winners.append(" and ");
            }
            winners.append(playerName);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================");
        Bukkit.broadcastMessage(ChatColor.GOLD + "           VICTORY ROYALE!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Winners: " + ChatColor.GREEN + winners);
        Bukkit.broadcastMessage(ChatColor.GOLD + "=============================");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "entity.ender_dragon.death", 1.0f, 1.0f);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location lobbyLocation = getLobbyLocation();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(lobbyLocation);
                player.getInventory().clear();
                player.setHealth(20);
                player.setFoodLevel(20);
                playerManager.setPlayerState(player, PlayerState.LOBBY);
            }
            setState(GameState.LOBBY);
        }, 100L); // 5-second delay
    }

    public GameState getState() {
        return currentState;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }
}