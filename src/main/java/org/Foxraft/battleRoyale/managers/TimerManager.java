package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.BattleRoyale;
import org.Foxraft.battleRoyale.states.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TimerManager {
    private final BattleRoyale plugin;
    private final StormManager stormManager;
    private BossBar timerBar;
    private BukkitTask timerTask;
    private int remainingSeconds;
    private GameState currentState;

    public TimerManager(BattleRoyale plugin, StormManager stormManager) {
        this.plugin = plugin;
        this.stormManager = stormManager;
        this.timerBar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
    }

    public void startTimer(GameState state) {
        if (timerTask != null) {
            timerTask.cancel();
        }

        this.currentState = state;
        setupTimer(state);
        startCountdown();
    }

    private void setupTimer(GameState state) {
        switch (state) {
            case GRACE:
                int graceMinutes = plugin.getConfig().getInt("graceTime", 60);
                remainingSeconds = graceMinutes * 60;
                timerBar.setColor(BarColor.GREEN);
                break;
            case STORM:
                remainingSeconds = stormManager.calculateStormDuration();
                timerBar.setColor(BarColor.RED);
                break;
            case DEATHMATCH:
                remainingSeconds = 5 * 60; // 5 minutes
                timerBar.setColor(BarColor.PURPLE);
                break;
            default:
                return;
        }

        updateBarTitle();
        timerBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(timerBar::addPlayer);
    }

    private void startCountdown() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    stop();
                    return;
                }

                remainingSeconds--;
                updateBarTitle();
                double initialSeconds = getInitialSeconds();
                if (initialSeconds > 0) {
                    timerBar.setProgress(Math.max(0.0, Math.min(1.0, (double) remainingSeconds / initialSeconds)));
                } else {
                    timerBar.setProgress(0.0);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateBarTitle() {
        String stateName = currentState.toString().charAt(0) + currentState.toString().substring(1).toLowerCase();
        String timeLeft = formatTime(remainingSeconds);
        timerBar.setTitle(ChatColor.GOLD + stateName + " ends in: " + ChatColor.WHITE + timeLeft);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private int getInitialSeconds() {
        switch (currentState) {
            case GRACE:
                return plugin.getConfig().getInt("graceTime", 5) * 60;
            case STORM:
                return stormManager.calculateStormDuration();
            case DEATHMATCH:
                return 5 * 60;
            default:
                return 0;
        }
    }

    public void stop() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timerBar != null) {
            timerBar.setProgress(0.0);
            timerBar.removeAll();
        }
        remainingSeconds = 0;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void addPlayer(Player player) {
        if (timerBar != null && remainingSeconds > 0) {
            timerBar.addPlayer(player);
            updateBarTitle();
        }
    }
}