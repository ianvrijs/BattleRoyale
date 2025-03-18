package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.BattleRoyale;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {
    private final BattleRoyale plugin;
    private final String worldName;

    public WorldLoadListener(BattleRoyale plugin, String worldName) {
        this.plugin = plugin;
        this.worldName = worldName;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (event.getWorld().getName().equals(worldName)) {
            event.getWorld().setPVP(true);
            plugin.getLogger().info("PVP enabled for world: " + worldName);
        }
    }
}
