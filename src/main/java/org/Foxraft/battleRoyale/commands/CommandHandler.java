package org.Foxraft.battleRoyale.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.Foxraft.battleRoyale.BattleRoyale;
import org.Foxraft.battleRoyale.managers.SetupManager;
import org.Foxraft.battleRoyale.managers.TeamManager;

public class CommandHandler implements CommandExecutor {
    private final BattleRoyale plugin;
    private final TeamManager teamManager;
    private final SetupManager setupManager;

    public CommandHandler(BattleRoyale plugin) {
        this.plugin = plugin;
        this.teamManager = new TeamManager(plugin);
        this.setupManager = new SetupManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /br <subcommand> [args]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "setup":
                handleSetupCommand(sender, args);
                break;
            case "team":
                handleTeamCommand(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                break;
        }
        return true;
    }

    private void handleSetupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /br setup <setlobby|setmapradius|setstormspeed|setgulag|setgracetime|setgulagheight>");
            return;
        }

        String action = args[1].toLowerCase();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
                switch (action) {
                    case "setlobby":
                        setupManager.setLobby(player);
                        break;
                    case "setmapradius":
                        if (args.length >= 3) {
                            setupManager.setMapRadius(player, Integer.parseInt(args[2]));
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /br setup setmapradius <int>");
                        }
                        break;
                    case "setstormspeed":
                        if (args.length >= 3) {
                            setupManager.setStormSpeed(player, Float.parseFloat(args[2]));
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /br setup setstormspeed <float>");
                        }
                        break;
                    case "setgulag":
                        if (args.length >= 3) {
                            setupManager.setGulag(player, Integer.parseInt(args[2]));
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /br setup setgulag <1|2>");
                        }
                        break;
                    case "setgracetime":
                        if (args.length >= 3) {
                            setupManager.setGraceTime(player, Integer.parseInt(args[2]));
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /br setup setgracetime <minutes>");
                        }
                        break;
                    case "setgulagheight":
                        if (args.length >= 3) {
                            setupManager.setGulagHeight(player, Integer.parseInt(args[2]));
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /br setup setgulagheight <int>");
                        }
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown setup action: " + action);
                        break;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number format.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
        }
    }

    private void handleTeamCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /br team <create|add|remove> [args]");
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "create":
                if (args.length == 4 && sender instanceof Player) {
                    Player player1 = plugin.getServer().getPlayer(args[2]);
                    Player player2 = plugin.getServer().getPlayer(args[3]);
                    if (player1 != null && player2 != null) {
                        teamManager.createTeam(player1, player2);
                        sender.sendMessage(ChatColor.GREEN + "Team created with players " + player1.getName() + " and " + player2.getName());
                    } else {
                        sender.sendMessage(ChatColor.RED + "One or both players not found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /br team create {player1} {player2}");
                }
                break;
            case "add":
                if (args.length == 4) {
                    Player player = plugin.getServer().getPlayer(args[2]);
                    String teamId = args[3];
                    if (player != null) {
                        teamManager.addPlayerToTeam(player, teamId);
                        sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " added to team " + teamId);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /br team add {player} {teamId}");
                }
                break;
            case "remove":
                if (args.length == 3) {
                    Player player = plugin.getServer().getPlayer(args[2]);
                    if (player != null) {
                        teamManager.removePlayerFromTeam(player);
                        sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " removed from their team.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /br team remove {player}");
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown team action: " + action);
                break;
        }
    }
}