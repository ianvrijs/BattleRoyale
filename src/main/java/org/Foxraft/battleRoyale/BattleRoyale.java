package org.Foxraft.battleRoyale;

import org.Foxraft.battleRoyale.listeners.*;
import org.Foxraft.battleRoyale.managers.*;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.utils.StartUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.Foxraft.battleRoyale.commands.CommandHandler;
import org.Foxraft.battleRoyale.commands.CommandTabCompleter;

import java.util.Objects;

public final class BattleRoyale extends JavaPlugin {

    @Override
    public void onEnable() {
        //enable pvp :oops:
        String worldName = getConfig().getString("lobby.world", "world");
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().severe("World '" + worldName + "' not found!");
            return;
        }
        world.setPVP(true);
        DeathMessageManager deathMessageManager = new DeathMessageManager();
        PlayerManager playerManager = new PlayerManager();
        TeamManager teamManager = new TeamManager(this, playerManager);
        TabManager tabManager = new TabManager(teamManager);
        getServer().getPluginManager().registerEvents(tabManager, this);
        TeamDamageListener teamDamageListener = new TeamDamageListener(teamManager, playerManager);
        StartUtils startUtils = new StartUtils(this, teamManager);
        GulagManager gulagManager = new GulagManager(playerManager, this, teamManager);
        InviteManager inviteManager = new InviteManager(this, teamManager);
        SetupManager setupManager = new SetupManager(this);
        StormManager stormManager = new StormManager(this);
        TimerManager timerManager = new TimerManager(this, stormManager);
        GameManager gameManager = new GameManager(this, playerManager, teamManager, startUtils, teamDamageListener, stormManager, gulagManager, timerManager, tabManager);
        gulagManager.setGameManager(gameManager);
        CommandHandler commandHandler = new CommandHandler(this, teamManager, setupManager, inviteManager, gameManager, playerManager);
        Objects.requireNonNull(getCommand("br")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("br")).setTabCompleter(new CommandTabCompleter());

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(gulagManager, playerManager, gameManager, this, deathMessageManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameManager, teamManager, playerManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(gulagManager, playerManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListener(deathMessageManager), this);
        stormManager.resetBorder();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}