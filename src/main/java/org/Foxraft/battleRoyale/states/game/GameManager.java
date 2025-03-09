package org.Foxraft.battleRoyale.states.game;

import org.Foxraft.battleRoyale.config.GameManagerConfig;
import org.Foxraft.battleRoyale.listeners.GracePeriodListener;
import org.Foxraft.battleRoyale.listeners.PlayerDeathListener;
import org.Foxraft.battleRoyale.managers.InviteManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.Foxraft.battleRoyale.utils.StartUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.CommandSender;

public class GameManager {
    private final JavaPlugin plugin;
    private final GameManagerConfig config;
    private final PlayerManager playerManager;
    private GameState currentState = GameState.LOBBY;
    private final TeamManager teamManager;
    private final InviteManager inviteManager;
    private final StartUtils startUtils;
    private final GracePeriodListener gracePeriodListener = new GracePeriodListener();
    private final PlayerDeathListener playerDeathListener;

    public GameManager(GameManagerConfig config) {
        this.plugin = config.getPlugin();
        this.teamManager = config.getTeamManager();
        this.inviteManager = config.getInviteManager();
        this.playerManager = config.getPlayerManager();
        this.startUtils = config.getStartUtils();
        this.config = config;
        this.playerDeathListener = new PlayerDeathListener(config);
        config.setGameManager(this);
    }

    public void startGame(CommandSender sender) {
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

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(playerDeathListener, plugin);

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
        setState(GameState.LOBBY);
        String worldName = plugin.getConfig().getString("lobby.world");
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        assert worldName != null;
        Location lobbyLocation = new Location(Bukkit.getWorld(worldName), x, y, z);

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
        HandlerList.unregisterAll(playerDeathListener);
    }

    private void startGraceState(long graceTimeTicks) {
        setState(GameState.GRACE);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(gracePeriodListener, plugin);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            HandlerList.unregisterAll(gracePeriodListener);
            startStormState();
        }, graceTimeTicks);
    }

    //TODO implement storm duration calculation
    private void startStormState() {
        setState(GameState.STORM);
        Bukkit.getScheduler().runTaskLater(plugin, this::startDeathmatchState, 20 * 300);
    }

    //TODO implement deathmatch utils
    private void startDeathmatchState() {
        setState(GameState.DEATHMATCH);
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