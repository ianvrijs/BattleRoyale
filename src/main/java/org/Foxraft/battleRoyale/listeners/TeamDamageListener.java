package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.managers.TeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamDamageListener implements Listener {
    private final TeamManager teamManager;

    public TeamDamageListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player damagee) {

            if (teamManager.isPlayerInAnyTeam(damager) && teamManager.isPlayerInAnyTeam(damagee)) {
                String damagerTeam = teamManager.getPlayerTeam(damager);
                String damageeTeam = teamManager.getPlayerTeam(damagee);

                if (damagerTeam != null && damagerTeam.equals(damageeTeam)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}