package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.events.PlayerStateChangeEvent;
import org.Foxraft.battleRoyale.managers.ScoreboardManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class ScoreboardListener implements Listener {
    private final ScoreboardManager scoreboardManager;
    private final Plugin plugin;

    public ScoreboardListener(ScoreboardManager scoreboardManager, Plugin plugin) {
        this.scoreboardManager = scoreboardManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreboardManager.updateScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Update for killed player
        scoreboardManager.updateScoreboard(event.getEntity());

        // Update for killer if exists
        if (event.getEntity().getKiller() != null) {
            scoreboardManager.updateScoreboard(event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onPlayerStateChange(PlayerStateChangeEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player updatedPlayer = event.getPlayer();
            scoreboardManager.updateScoreboard(updatedPlayer);

            org.Foxraft.battleRoyale.models.Team team = scoreboardManager.getTeamManager().getTeam(updatedPlayer);
            if (team != null) {
                team.getPlayers().stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(scoreboardManager::updateScoreboard);
            }
        });
    }

    public void updateOnGameStateChange(GameState newState) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.getOnlinePlayers().forEach(scoreboardManager::updateScoreboard)
        );
    }
}