package org.Foxraft.battleRoyale.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("setup", "team");
        }

        if (args[0].equalsIgnoreCase("setup")) {
            if (args.length == 2) {
                return Arrays.asList("setlobby", "setmapradius", "setstormspeed", "setgulag", "setgracetime", "setgulagheight");
            }
        }

        if (args[0].equalsIgnoreCase("team")) {
            return Arrays.asList("create", "add", "remove");
        }
        return new ArrayList<>();
    }
}