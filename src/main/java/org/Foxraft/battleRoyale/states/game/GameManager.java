package org.Foxraft.battleRoyale.states.game;

import org.Foxraft.battleRoyale.events.DeathmatchTimerEndEvent;
import org.Foxraft.battleRoyale.events.StormReachedFinalDestinationEvent;
import org.Foxraft.battleRoyale.listeners.GracePeriodListener;
import org.Foxraft.battleRoyale.listeners.ScoreboardListener;
import org.Foxraft.battleRoyale.listeners.TeamDamageListener;
import org.Foxraft.battleRoyale.managers.*;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.gulag.GulagState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.Foxraft.battleRoyale.utils.StartUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

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
    private final String worldName;
    private ScoreboardListener scoreboardListener;
    private ScoreboardManager scoreboardManager;

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
        this.worldName = plugin.getConfig().getString("lobby.world", "world");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onStormReachedFinalDestination(StormReachedFinalDestinationEvent event) {
        Bukkit.getLogger().info("StormReachedFinalDestinationEvent received");
        startDeathmatchState();
    }
    @EventHandler
    public void onDeathmatchTimerEnd(DeathmatchTimerEndEvent event) {
        if (currentState == GameState.DEATHMATCH) {
            //count which team has most alive players
            Map<Team, Integer> alivePlayerCounts = new HashMap<>();

            for (Team team : teamManager.getTeams().values()) {
                int aliveCount = (int) team.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .filter(player -> playerManager.getPlayerState(player) == PlayerState.ALIVE)
                        .count();

                if (aliveCount > 0) {
                    alivePlayerCounts.put(team, aliveCount);
                }
            }

            if (!alivePlayerCounts.isEmpty()) {
                alivePlayerCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).ifPresent(this::endGame);
            } else {
                //edge case; no players
                Bukkit.broadcastMessage(ChatColor.RED + "Game ended - No winners!");
                stopGame();
            }
        }
    }
    public void setScoreboardListener(ScoreboardListener listener) {
        this.scoreboardListener = listener;
    }
    public void setScoreboardManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }
    public void startGame(CommandSender sender) {
        if (currentState != GameState.LOBBY) {
            sender.sendMessage(ChatColor.RED + "Game has already been started.");
            return;
        }

        int activeTeams = (int) teamManager.getTeams().values().stream()
                .filter(team -> team.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .anyMatch(Objects::nonNull))
                .count();

        if (activeTeams < 2) {
            sender.sendMessage(ChatColor.RED + "At least 2 teams are required to start (current: " + activeTeams + ")");
            return;
        }

        autoAssignTeams();

        setState(GameState.STARTING);
        sender.sendMessage(ChatColor.GREEN + "Preparing game start...");

        startUtils.prepareSpawnPoints();
        startUtils.broadcastCountdown(5);

        int graceTimeMinutes = plugin.getConfig().getInt("graceTime", 5);
        final long graceTimeTicks = graceTimeMinutes * 60L * 20L;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                startUtils.teleportTeams();
                setPlayersAlive();
                startGraceState(graceTimeTicks);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error during game start: " + e.getMessage());
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
        pluginManager.registerEvents(gracePeriodListener, plugin);

        gracePeriodTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            HandlerList.unregisterAll(gracePeriodListener);
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

    private void startDeathmatchState() {
        setState(GameState.DEATHMATCH);
        gulagManager.clearGulag();
    }

    private void setState(GameState newState) {
        this.currentState = newState;
        timerManager.startTimer(getCurrentState());

        tabManager.updateHeaderFooter(newState);
        scoreboardListener.updateOnGameStateChange(newState);

        String message = switch (newState) {
            case STARTING -> ChatColor.GREEN + "⚔ Prepare for battle! The game is starting...";
            case GRACE -> ChatColor.YELLOW + "☮ Grace period has begun! Gather resources and prepare your strategy.";
            case STORM -> ChatColor.RED + "⚡ The storm is approaching! PvP has been enabled!";
            case DEATHMATCH -> ChatColor.GOLD + "☠ Final showdown! May the best team win!";
            case LOBBY -> ChatColor.AQUA + "✦ Game ended - returning to lobby.";
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
                        if (state == PlayerState.ALIVE || state == PlayerState.GULAG || state == PlayerState.RESURRECTED) {
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

    public JavaPlugin getPlugin() {
        return plugin;
    }

    private void autoAssignTeams() {
        List<Player> playersWithoutTeam = Bukkit.getOnlinePlayers().stream()
                .filter(player -> !teamManager.isPlayerInAnyTeam(player) && !playerManager.isExempted(player))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        for (int i = 0; i < playersWithoutTeam.size(); i += 2) {
            if (i + 1 < playersWithoutTeam.size()) {
                Player player1 = playersWithoutTeam.get(i);
                Player player2 = playersWithoutTeam.get(i + 1);
                teamManager.createTeam(player1, player2);
                notifyTeamAssignment(player1, player2);
            } else {
                Player soloPlayer = playersWithoutTeam.get(i);
                teamManager.createSoloTeam(soloPlayer);
                scoreboardManager.updatePlayerTeam(soloPlayer);
            }
        }
    }

    private void notifyTeamAssignment(Player player1, Player player2) {
        String message = ChatColor.GREEN + "You have been put into a new team with %s";
        player1.sendMessage(String.format(message, player2.getName()));
        player2.sendMessage(String.format(message, player1.getName()));
        scoreboardManager.updatePlayerTeam(player1);
        scoreboardManager.updatePlayerTeam(player2);
    }

    private void setPlayersAlive() {
        teamManager.getTeams().values().stream()
                .flatMap(team -> team.getPlayers().stream())
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> playerManager.setPlayerState(player, PlayerState.ALIVE));
    }
}