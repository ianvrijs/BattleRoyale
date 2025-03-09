package org.Foxraft.battleRoyale;

import org.Foxraft.battleRoyale.config.GameManagerConfig;
import org.Foxraft.battleRoyale.managers.InviteManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
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
        GameManagerConfig config = new GameManagerConfig(this);

        PlayerManager playerManager = new PlayerManager();
        config.setPlayerManager(playerManager);

        TeamManager teamManager = new TeamManager(this);
        config.setTeamManager(teamManager);

        StartUtils startUtils = new StartUtils(config);
        config.setStartUtils(startUtils);

        GulagManager gulagManager = new GulagManager(config);
        config.setGulagManager(gulagManager);

        InviteManager inviteManager = new InviteManager(config);
        config.setInviteManager(inviteManager);

        GameManager gameManager = new GameManager(config);
        config.setGameManager(gameManager);

        CommandHandler commandHandler = new CommandHandler(this, config);
        Objects.requireNonNull(getCommand("br")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("br")).setTabCompleter(new CommandTabCompleter());

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(config, gameManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}