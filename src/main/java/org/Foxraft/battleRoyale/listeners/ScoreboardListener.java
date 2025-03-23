package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.ScoreboardManager;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.Foxraft.battleRoyale.states.player.PlayerState;
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

    public void updateOnGameStateChange(GameState newState) {
        Bukkit.getOnlinePlayers().forEach(scoreboardManager::updateScoreboard);
    }
    public void updateOnPlayerStateChange(PlayerState newState, Player player) {
        scoreboardManager.updateScoreboard(player);
    }
}