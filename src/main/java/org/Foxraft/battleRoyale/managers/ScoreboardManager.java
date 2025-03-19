package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerState;

public class ScoreboardManager {
    private final org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager;
    private final StatsManager statsManager;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;

    public ScoreboardManager(StatsManager statsManager, TeamManager teamManager, GameManager gameManager, PlayerManager playerManager) {
        this.bukkitScoreboardManager = Bukkit.getScoreboardManager();
        this.statsManager = statsManager;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
        this.playerManager = playerManager;
    }

    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = bukkitScoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("battleRoyale", "dummy", ChatColor.GOLD + "" + ChatColor.BOLD + "Battle Royale");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore(ChatColor.WHITE + "Game: " + ChatColor.GREEN + formatGameState(gameManager.getCurrentState())).setScore(7);
        PlayerState playerState = playerManager.getPlayerState(player);
        objective.getScore(ChatColor.WHITE + "Status: " + ChatColor.YELLOW + formatPlayerState(playerState)).setScore(6);
        objective.getScore("").setScore(5);
        objective.getScore(ChatColor.WHITE + "Player: " + ChatColor.AQUA + player.getName()).setScore(4);
        objective.getScore(ChatColor.WHITE + "Kills: " + ChatColor.RED + statsManager.getKills(player)).setScore(3);
        objective.getScore(" ").setScore(2);

        Integer teamId = teamManager.getTeam(player) != null ? Integer.valueOf(teamManager.getTeam(player).getId()) : null;
        if (teamId != null) {
            Player teammate = teamManager.getTeammate(player);
            String teammateName = teammate != null ? teammate.getName() : "None";
            objective.getScore(ChatColor.WHITE + "Team: " + ChatColor.GOLD + teamId).setScore(1);
        } else {
            objective.getScore(ChatColor.WHITE + "Team: " + ChatColor.GRAY + "None").setScore(1);
            objective.getScore(ChatColor.WHITE + "Teammate: " + ChatColor.GRAY + "None").setScore(0);
        }
        objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Foxcraft");

        player.setScoreboard(scoreboard);
    }

    private String formatGameState(GameState state) {
        return switch (state) {
            case LOBBY -> "Lobby";
            case STARTING -> "Starting";
            case GRACE -> "Grace";
            case STORM -> "Storm";
            case DEATHMATCH -> "Deathmatch";
        };
    }

    private String formatPlayerState(PlayerState state) {
        return switch (state) {
            case ALIVE -> "Alive";
            case DEAD -> "Dead";
            case LOBBY -> "Lobby";
            case GULAG -> "Redemption";
            case RESURRECTED -> "Resurrected";
        };
    }
}