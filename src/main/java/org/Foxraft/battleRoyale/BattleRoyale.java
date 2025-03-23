package org.Foxraft.battleRoyale;

import org.Foxraft.battleRoyale.listeners.*;
import org.Foxraft.battleRoyale.managers.*;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.utils.StartUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.Foxraft.battleRoyale.commands.CommandHandler;
import org.Foxraft.battleRoyale.commands.CommandTabCompleter;

import java.util.Objects;

public final class BattleRoyale extends JavaPlugin {

    @Override
    public void onEnable() {

        StatsManager statsManager = new StatsManager(this);
        DeathMessageManager deathMessageManager = new DeathMessageManager();
        PlayerManager playerManager = new PlayerManager();
        TeamManager teamManager = new TeamManager(this, playerManager);
        TabManager tabManager = new TabManager(teamManager, playerManager);
        getServer().getPluginManager().registerEvents(tabManager, this);
        TeamDamageListener teamDamageListener = new TeamDamageListener(teamManager, playerManager);
        StartUtils startUtils = new StartUtils(this, teamManager);
        GulagManager gulagManager = new GulagManager(playerManager, this, teamManager);
        InviteManager inviteManager = new InviteManager(this, teamManager, tabManager);
        SetupManager setupManager = new SetupManager(this);
        StormManager stormManager = new StormManager(this);
        TimerManager timerManager = new TimerManager(this, stormManager);
        GameManager gameManager = new GameManager(this, playerManager, teamManager, startUtils, teamDamageListener, stormManager, gulagManager, timerManager, tabManager);
        gulagManager.setGameManager(gameManager);
        JoinManager joinManager = new JoinManager(gameManager, teamManager, playerManager, timerManager);
        ScoreboardManager scoreboardManager = new ScoreboardManager(statsManager, teamManager, gameManager, playerManager);
        CommandHandler commandHandler = new CommandHandler(this, teamManager, setupManager, inviteManager, gameManager, playerManager, statsManager, joinManager);
        Objects.requireNonNull(getCommand("br")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("br")).setTabCompleter(new CommandTabCompleter());

        getServer().getPluginManager().registerEvents(new PlayerKillListener(statsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(gulagManager, playerManager, gameManager, this, deathMessageManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameManager, teamManager, playerManager, tabManager, timerManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(gulagManager, playerManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListener(deathMessageManager), this);

        ScoreboardListener scoreboardListener = new ScoreboardListener(scoreboardManager, this);
        getServer().getPluginManager().registerEvents(scoreboardListener, this);
        gameManager.setScoreboardListener(scoreboardListener);
        playerManager.setScoreboardListener(scoreboardListener);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                String worldName = getConfig().getString("lobby.world", "world");
                getServer().getPluginManager().registerEvents(new WorldLoadListener(this, worldName), this);
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    world.setPVP(true);
                }
                stormManager.resetBorder();
            } catch (Exception e) {
                getLogger().severe("Failed to load world: " + e.getMessage());
            }
        }, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}