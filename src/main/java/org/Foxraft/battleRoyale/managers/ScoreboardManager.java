package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.game.GameManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Objects;

public class ScoreboardManager {
    private final org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager;
    private final StatsManager statsManager;
    private final TeamManager teamManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final Scoreboard gameScoreboard;

    public ScoreboardManager(StatsManager statsManager, TeamManager teamManager, GameManager gameManager, PlayerManager playerManager) {
        this.bukkitScoreboardManager = Bukkit.getScoreboardManager();
        assert bukkitScoreboardManager != null;
        this.gameScoreboard = bukkitScoreboardManager.getNewScoreboard();
        this.statsManager = statsManager;
        this.teamManager = teamManager;
        this.gameManager = gameManager;
        this.playerManager = playerManager;
        setupTeams();
    }

    private void setupTeams() {
        for (org.bukkit.scoreboard.Team scoreboardTeam : gameScoreboard.getTeams()) {
            scoreboardTeam.unregister();
        }

        for (Team gameTeam : teamManager.getTeams().values()) {
            org.bukkit.scoreboard.Team scoreboardTeam = gameScoreboard.registerNewTeam("team" + gameTeam.getId());

            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
//            scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
            scoreboardTeam.setCanSeeFriendlyInvisibles(true);

            scoreboardTeam.setPrefix(ChatColor.GOLD + "[Team " + gameTeam.getId() + "] ");
            scoreboardTeam.setColor(ChatColor.GRAY);

            for (String playerName : gameTeam.getPlayers()) {
                scoreboardTeam.addEntry(playerName);
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.setScoreboard(gameScoreboard);
                }
            }
        }
    }

    public void updatePlayerTeam(Player player) {
        player.setScoreboard(gameScoreboard);

        Team gameTeam = teamManager.getTeam(player);
        String teamName = gameTeam != null ? "team" + gameTeam.getId() : null;

        for (org.bukkit.scoreboard.Team scoreboardTeam : gameScoreboard.getTeams()) {
            if (scoreboardTeam.hasEntry(player.getName())) {
                scoreboardTeam.removeEntry(player.getName());
            }
        }

        if (teamName != null) {
            org.bukkit.scoreboard.Team scoreboardTeam = gameScoreboard.getTeam(teamName);
            if (scoreboardTeam == null) {
                scoreboardTeam = gameScoreboard.registerNewTeam(teamName);
                scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
                scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
                scoreboardTeam.setCanSeeFriendlyInvisibles(true);
                scoreboardTeam.setColor(ChatColor.GRAY);
                scoreboardTeam.setPrefix(ChatColor.GOLD + "[Team " + gameTeam.getId() + "] ");
            }
            scoreboardTeam.addEntry(player.getName());
        }

        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Objective objective = gameScoreboard.getObjective("battleRoyale");
        if (objective == null) {
            objective = gameScoreboard.registerNewObjective("battleRoyale", "dummy", ChatColor.GOLD + "" + ChatColor.BOLD + "Battle Royale");
        } else {
            for (String entry : gameScoreboard.getEntries()) {
                Objects.requireNonNull(objective.getScoreboard()).resetScores(entry);
            }
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore(ChatColor.WHITE + "Game: " + ChatColor.GREEN + formatGameState(gameManager.getCurrentState())).setScore(7);
        objective.getScore(ChatColor.WHITE + "Status: " + ChatColor.YELLOW + formatPlayerState(playerManager.getPlayerState(player))).setScore(6);
        objective.getScore("").setScore(5);
        objective.getScore(ChatColor.WHITE + "Player: " + ChatColor.AQUA + player.getName()).setScore(4);
        objective.getScore(ChatColor.WHITE + "Kills: " + ChatColor.RED + statsManager.getKills(player)).setScore(3);
        objective.getScore(" ").setScore(2);
        objective.getScore(ChatColor.WHITE + "Team: " + ChatColor.GOLD + (teamManager.getTeam(player) != null ? teamManager.getTeam(player).getId() : "None")).setScore(1);

        player.setScoreboard(gameScoreboard);
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

    public Scoreboard getGameScoreboard() {
        return gameScoreboard;
    }
}