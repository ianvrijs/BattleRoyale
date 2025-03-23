package org.Foxraft.battleRoyale.commands;

import org.Foxraft.battleRoyale.events.TeamLeaveEvent;
import org.Foxraft.battleRoyale.managers.*;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.Foxraft.battleRoyale.BattleRoyale;
import org.Foxraft.battleRoyale.states.game.GameState;


public class CommandHandler implements CommandExecutor {
    private final BattleRoyale plugin;
    private final TeamManager teamManager;
    private final SetupManager setupManager;
    private final InviteManager inviteManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final StatsManager statsManager;
    private final CooldownManager cooldownManager = new CooldownManager();
    private final JoinManager joinManager;


    public CommandHandler(BattleRoyale plugin, TeamManager teamManager, SetupManager setupManager, InviteManager inviteManager, GameManager gameManager, PlayerManager playerManager, StatsManager statsManager, JoinManager joinManager)  {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.setupManager = setupManager;
        this.inviteManager = inviteManager;
        this.gameManager = gameManager;
        this.playerManager = playerManager;
        this.statsManager = statsManager;
        this.joinManager = joinManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /br <subcommand> [args]");
            return true;
        }
        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("setup") || subCommand.equals("start") || subCommand.equals("stop")) {
            if (!sender.hasPermission("br.admin")) {
                sender.sendMessage(ChatColor.RED + "nope.");
                return true;
            }
        }
        switch (subCommand) {
            case "setup":
                handleSetupCommand(sender, args);
                break;
            case "team":
                handleTeamCommand(sender, args);
                break;
            case "start":
                gameManager.startGame(sender);
                break;
            case "stop":
                gameManager.stopGame();
                break;
            case "clearstats":
                if (!sender.hasPermission("br.admin")) {
                    sender.sendMessage(ChatColor.RED + "nope.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /br clearstats <player|all>");
                    return true;
                }
                if (args[1].equalsIgnoreCase("all")) {
                    statsManager.clearAllStats();
                    sender.sendMessage(ChatColor.GREEN + "All stats have been cleared.");
                } else {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        statsManager.resetStats(targetPlayer);
                        sender.sendMessage(ChatColor.GREEN + "Stats cleared for " + targetPlayer.getName() + ".");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                }
                break;
            case "exempt":
                if (!sender.hasPermission("br.admin")) {
                    sender.sendMessage(ChatColor.RED + "nope.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /br exempt <player>");
                    return true;
                }
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                playerManager.toggleExemption(targetPlayer);
                boolean isExempted = playerManager.isExempted(targetPlayer);
                sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " is " +
                        (isExempted ? "now" : "no longer") + " exempted from matchmaking.");
                targetPlayer.sendMessage(ChatColor.YELLOW + "You are " +
                        (isExempted ? "now" : "no longer") + " exempted from matchmaking.");
                break;
            case "join":
                if (sender instanceof Player player) {
                    if(!player.hasPermission("br.team")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                        return true;
                    }
                    joinManager.handleJoin(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                }
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
        if(!sender.hasPermission("br.admin")) {
            sender.sendMessage(ChatColor.RED + "Nuh uh.");
            return;
        }

        String action = args[1].toLowerCase();
        if (sender instanceof Player player) {
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
            sender.sendMessage(ChatColor.RED + "Usage: /br team <invite|accept|list|leave> [args]");
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "invite":
            case "accept":
            case "leave":
                if (gameManager.getCurrentState() != GameState.LOBBY) {
                    sender.sendMessage(ChatColor.RED + "You can't do this right now.");
                    return;
                }
                break;
        }
        switch (action) {
            case "invite", "accept", "list", "leave" -> {
                if (!sender.hasPermission("br.team")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return;
                }
            }
            case "create", "add", "remove" -> {
                if (!sender.hasPermission("br.admin")) {
                    sender.sendMessage(ChatColor.RED + "Nuh-uh.");
                    return;
                }
            }
        }
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
                        Bukkit.getPluginManager().callEvent(new TeamLeaveEvent(player));
                        sender.sendMessage(ChatColor.GREEN + "Player " + player.getName() + " removed from their team.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /br team remove {player}");
                }
                break;
            case "leave":
                if (sender instanceof Player player) {
                    if (teamManager.isPlayerInAnyTeam(player)) {

                        teamManager.removePlayerFromTeam(player);
                        Bukkit.getPluginManager().callEvent(new TeamLeaveEvent(player));
                        sender.sendMessage(ChatColor.GREEN + "You have left your team.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You are not in any team.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                }
                break;
            case "invite":
                if (args.length == 3 && sender instanceof Player inviter) {
                    Player invitee = plugin.getServer().getPlayer(args[2]);
                    if (invitee != null) {
                        if (cooldownManager.hasCooldown((Player) sender, "invite")) {
                            break;
                        }
                        inviteManager.invitePlayer(inviter, invitee);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /br team invite {player}");
                }
                break;
            case "accept":
                if (args.length == 3 && sender instanceof Player invitee) {
                    Player inviter = plugin.getServer().getPlayer(args[2]);
                    if (inviter != null) {
                        inviteManager.acceptInvitation(invitee, inviter);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /br team accept {player}");
                }
                break;
            case "list":
                if (args.length == 3) {
                    if (cooldownManager.hasCooldown((Player) sender, "list")) {
                        break;
                    }
                    int page = Integer.parseInt(args[2]);
                    teamManager.listTeams(sender, page);
                } else {
                    teamManager.listTeams(sender, 1);
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown team action: " + action);
                break;
        }
    }
}