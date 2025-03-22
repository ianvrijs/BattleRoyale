package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.events.PlayerStateChangeEvent;
import org.Foxraft.battleRoyale.events.TeamLeaveEvent;
import org.Foxraft.battleRoyale.models.Team;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TabManager implements Listener {
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private String header;
    private String footer;

    public TabManager(TeamManager teamManager, PlayerManager playerManager) {
        this.teamManager = teamManager;
        this.playerManager = playerManager;
        setDefaultHeaderFooter();
    }

    @EventHandler
    public void onTeamLeave(TeamLeaveEvent event) {
        updatePlayerTab(event.getPlayer(), playerManager.getPlayerState(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerStateChange(PlayerStateChangeEvent event) {
        updatePlayerTab(event.getPlayer(), event.getNewState());
    }

    public void initializePlayer(Player player, PlayerState state) {
        updatePlayerTab(player, state);
    }

    public void updatePlayerTab(Player player, PlayerState playerState) {
        Team team = teamManager.getTeam(player);
        String tabPrefix = team != null ? ChatColor.GOLD + team.getId() + " " + ChatColor.RESET : "";
        String tabName = tabPrefix + formatPlayerState(playerState) + " " + player.getName();
        player.setPlayerListName(tabName);
    }

    private String formatPlayerState(PlayerState state) {
        return switch (state) {
            case ALIVE -> ChatColor.GREEN + "●";
            case DEAD -> ChatColor.RED + "●";
            case LOBBY -> ChatColor.GRAY + "●";
            case GULAG -> ChatColor.YELLOW + "●";
            case RESURRECTED -> ChatColor.AQUA + "●";
        };
    }

    private void setDefaultHeaderFooter() {
        this.header = ChatColor.GOLD + "=== Battle Royale ===" +
                "\n" + ChatColor.GRAY + "%players% players";
        this.footer = "\n" + ChatColor.GRAY + "Current State: %state%" +
                "\n" + ChatColor.GOLD + "=== Foxcraft ===";
    }

    public void updateHeaderFooter(GameState gameState) {
        String formattedHeader = header.replace("%players%",
                String.valueOf(Bukkit.getOnlinePlayers().size()));
        String formattedFooter = footer.replace("%state%",
                ChatColor.GREEN + gameState.toString());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListHeaderFooter(formattedHeader, formattedFooter);
        }
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}