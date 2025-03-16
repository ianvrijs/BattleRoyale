package org.Foxraft.battleRoyale;

import org.Foxraft.battleRoyale.listeners.PlayerDeathListener;
import org.Foxraft.battleRoyale.listeners.PlayerQuitListener;
import org.Foxraft.battleRoyale.listeners.TeamDamageListener;
import org.Foxraft.battleRoyale.managers.*;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.utils.StartUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.Foxraft.battleRoyale.commands.CommandHandler;
import org.Foxraft.battleRoyale.commands.CommandTabCompleter;
import org.Foxraft.battleRoyale.listeners.PlayerJoinListener;

import java.util.Objects;

public final class BattleRoyale extends JavaPlugin {

    @Override
    public void onEnable() {
        PlayerManager playerManager = new PlayerManager();
        TeamManager teamManager = new TeamManager(this);
        TeamDamageListener teamDamageListener = new TeamDamageListener(teamManager, playerManager);
        StartUtils startUtils = new StartUtils(this, teamManager);
        GulagManager gulagManager = new GulagManager(playerManager, this, teamManager);
        InviteManager inviteManager = new InviteManager(this, teamManager);
        SetupManager setupManager = new SetupManager(this);
        StormManager stormManager = new StormManager(this);
        TimerManager timerManager = new TimerManager(this, stormManager);
        GameManager gameManager = new GameManager(this, playerManager, teamManager, startUtils, teamDamageListener, stormManager, gulagManager, timerManager);
        gulagManager.setGameManager(gameManager);
        CommandHandler commandHandler = new CommandHandler(this, teamManager, setupManager, inviteManager, gameManager, playerManager);
        Objects.requireNonNull(getCommand("br")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("br")).setTabCompleter(new CommandTabCompleter());

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(gulagManager, playerManager, gameManager, this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameManager, teamManager, playerManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(gulagManager), this);

        stormManager.resetBorder();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}