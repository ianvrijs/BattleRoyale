package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.states.gulag.GulagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final GulagManager gulagManager;

    public PlayerQuitListener(GulagManager gulagManager) {
        this.gulagManager = gulagManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        gulagManager.handlePlayerLeave(event.getPlayer());
    }
}