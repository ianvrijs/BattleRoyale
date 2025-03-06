package org.Foxraft.battleRoyale;

import org.Foxraft.battleRoyale.commands.CommandTabCompleter;
import org.Foxraft.battleRoyale.teams.TeamHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.Foxraft.battleRoyale.commands.CommandHandler;

import java.util.Objects;

public final class BattleRoyale extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        CommandHandler commandHandler = new CommandHandler(this);
        Objects.requireNonNull(getCommand("br")).setExecutor(commandHandler);
        Objects.requireNonNull(getCommand("br")).setTabCompleter(new CommandTabCompleter());

    }

    @Override
    public void onDisable() {

    }
}