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
            List<String> commands = new ArrayList<>();
            if (sender.hasPermission("br.admin")) {
                commands.add("setup");
                commands.add("start");
                commands.add("stop");
            }
            if (sender.hasPermission("br.use")) {
                commands.add("team");
            }
            return commands;
        }

        if (args[0].equalsIgnoreCase("setup") && sender.hasPermission("br.admin")) {
            if (args.length == 2) {
                return Arrays.asList("setlobby", "setmapradius", "setstormspeed", "setgulag", "setgracetime", "setgulagheight");
            }
        }

        if (args[0].equalsIgnoreCase("team")) {
            List<String> teamCommands = new ArrayList<>();
            if (sender.hasPermission("br.use")) {
                teamCommands.add("invite");
                teamCommands.add("accept");
                teamCommands.add("leave");
                teamCommands.add("list");
            }
            if (sender.hasPermission("br.admin")) {
                teamCommands.addAll(Arrays.asList("create", "add", "remove"));
            }
            return teamCommands;
        }
        return new ArrayList<>();
    }
}