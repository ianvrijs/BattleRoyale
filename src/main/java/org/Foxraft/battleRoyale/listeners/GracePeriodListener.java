package org.Foxraft.battleRoyale.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

/**
 * This class listens for player damage events and cancels them if the damager and the entity are both players.
 */
public class GracePeriodListener implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }
}