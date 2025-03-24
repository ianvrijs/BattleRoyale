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
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> allCommands = new ArrayList<>();
            if (sender.hasPermission("br.admin")) {
                allCommands.addAll(Arrays.asList("setup", "start", "stop", "clearstats", "exempt"));
            }
            if (sender.hasPermission("br.team")) {
                allCommands.add("team");
            }
            String partialCommand = args[0].toLowerCase();
            for (String cmd : allCommands) {
                if (cmd.toLowerCase().startsWith(partialCommand)) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        //setup
        if (args[0].equalsIgnoreCase("setup") && sender.hasPermission("br.admin")) {
            if (args.length == 2) {
                List<String> setupCommands = Arrays.asList(
                        "setlobby", "setmapradius", "setstormspeed",
                        "setgulag", "setgracetime", "setgulagheight"
                );

                String partialCommand = args[1].toLowerCase();
                for (String cmd : setupCommands) {
                    if (cmd.toLowerCase().startsWith(partialCommand)) {
                        completions.add(cmd);
                    }
                }
            }
            return completions;
        }

        // team
        if (args[0].equalsIgnoreCase("team") && args.length == 2) {
            List<String> teamCommands = new ArrayList<>();

            if (sender.hasPermission("br.team")) {
                teamCommands.addAll(Arrays.asList("invite", "accept", "list"));
            }
            if (sender.hasPermission("br.admin")) {
                teamCommands.addAll(Arrays.asList("create", "add", "remove"));
            }

            String partialCommand = args[1].toLowerCase();
            for (String cmd : teamCommands) {
                if (cmd.toLowerCase().startsWith(partialCommand)) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        return completions;
    }
}