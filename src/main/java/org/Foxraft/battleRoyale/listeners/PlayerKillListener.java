package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerKillListener implements Listener {
    private final StatsManager statsManager;

    public PlayerKillListener(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            statsManager.addKill(killer);
        }
    }
}