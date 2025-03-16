package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.events.PlayerStateChangeEvent;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Objects;

public class TabManager implements Listener {
    private final Scoreboard scoreboard;
    private final TeamManager teamManager;
    private String header;
    private String footer;

    public TabManager(TeamManager teamManager) {
        this.teamManager = teamManager;
        this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        setDefaultHeaderFooter();
    }

    private void setDefaultHeaderFooter() {
        this.header = ChatColor.GOLD + "=== Battle Royale ===" +
        "\n" + ChatColor.GRAY + "%players% players";
        this.footer = "\n" + ChatColor.GRAY + "Current State: %state%" +
                "\n" + ChatColor.GOLD + "=== Foxcraft ===";
    }

    @EventHandler
    public void onPlayerStateChange(PlayerStateChangeEvent event) {
        updatePlayerTab(event.getPlayer(), event.getNewState());
    }

    public void updatePlayerTab(Player player, PlayerState state) {
        Team gameTeam = teamManager.getTeam(player);
        if (gameTeam == null) return;

        String teamId = gameTeam.getId();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(teamId);

        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamId);
            scoreboardTeam.setPrefix(ChatColor.GRAY + "[" + teamId + "] ");
        }

        switch (state) {
            case ALIVE -> scoreboardTeam.setColor(ChatColor.GREEN);
            case GULAG -> scoreboardTeam.setColor(ChatColor.RED);
            case DEAD -> scoreboardTeam.setColor(ChatColor.DARK_GRAY);
            default -> scoreboardTeam.setColor(ChatColor.GRAY);
        }

        scoreboardTeam.addEntry(player.getName());
        player.setScoreboard(scoreboard);
    }

    public void updateHeaderFooter(GameState gameState) {
        String formattedHeader = header.replace("%players%",
                String.valueOf(Bukkit.getOnlinePlayers().size()));

        String formattedFooter = footer.replace("%state%",
                ChatColor.GREEN + gameState.toString());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeaderFooter(
                    formattedHeader,
                    formattedFooter
            );
        }
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public void removePlayer(Player player) {
        org.bukkit.scoreboard.Team team = scoreboard.getEntryTeam(player.getName());
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }
}