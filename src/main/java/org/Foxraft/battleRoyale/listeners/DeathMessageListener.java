package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.DeathMessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageListener implements Listener {
    private final DeathMessageManager deathMessageManager;

    public DeathMessageListener(DeathMessageManager deathMessageManager) {
        this.deathMessageManager = deathMessageManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        EntityDamageEvent.DamageCause cause = victim.getLastDamageCause() != null ?
                victim.getLastDamageCause().getCause() : null;

        if (killer != null) {
            event.setDeathMessage(deathMessageManager.getPvPDeathMessage(killer, victim));
        } else if (cause == EntityDamageEvent.DamageCause.FALL) {
            event.setDeathMessage(deathMessageManager.getFallDeathMessage(victim));
        } else if (cause == EntityDamageEvent.DamageCause.CUSTOM) {
            event.setDeathMessage(deathMessageManager.getStormDeathMessage(victim));
        }
        //lava death message
        else if (cause == EntityDamageEvent.DamageCause.LAVA) {
            event.setDeathMessage(deathMessageManager.getLavaDeathMessage(victim));
        }
        else if (cause == EntityDamageEvent.DamageCause.FIRE) {
            event.setDeathMessage(deathMessageManager.getBurnedDeathMessage(victim));
        }
        else if (cause == EntityDamageEvent.DamageCause.DROWNING) {
            event.setDeathMessage(deathMessageManager.getDrownedDeathMessage(victim));
        }
        else if (cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setDeathMessage(deathMessageManager.getSuffocatedDeathMessage(victim));
        }
    }
}