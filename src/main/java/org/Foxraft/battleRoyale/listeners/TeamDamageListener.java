package org.Foxraft.battleRoyale.listeners;

import org.Foxraft.battleRoyale.states.player.PlayerManager;
import org.Foxraft.battleRoyale.managers.TeamManager;
import org.Foxraft.battleRoyale.states.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamDamageListener implements Listener {
    private final TeamManager teamManager;
    private final PlayerManager playerManager;

    public TeamDamageListener(TeamManager teamManager, PlayerManager playerManager) {
        this.teamManager = teamManager;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player damagee) {
            if (teamManager.isPlayerInAnyTeam(damager) && teamManager.isPlayerInAnyTeam(damagee)) {
                String damagerTeam = teamManager.getPlayerTeam(damager);
                String damageeTeam = teamManager.getPlayerTeam(damagee);

                if (damagerTeam != null && damagerTeam.equals(damageeTeam)) {
                    if (playerManager.getPlayerState(damager) == PlayerState.GULAG && playerManager.getPlayerState(damagee) == PlayerState.GULAG) {
                        return;
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
}