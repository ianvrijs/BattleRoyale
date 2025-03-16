package org.Foxraft.battleRoyale.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DeathMessageManager {
    public String getStormDeathMessage(Player player) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY +player.getName() + ChatColor.RED + " was consumed by the storm";
    }

    public String getPvPDeathMessage(Player killer, Player victim) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + victim.getName() + ChatColor.RED + " was eliminated by " + ChatColor.GRAY + killer.getName();
    }

    public String getFallDeathMessage(Player player) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + player.getName() + ChatColor.RED +" fell to their death";
    }

    public String getQuitDeathMessage(Player player) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + player.getName() + ChatColor.RED + " has abandoned the match";
    }

    public String getGulagLossMessage(Player player) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + player.getName() + ChatColor.RED + " failed to redeem themselves";
    }

    public String getLavaDeathMessage(Player victim) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + victim.getName() + ChatColor.RED + " jumped into the lava";
    }

    public String getBurnedDeathMessage(Player victim) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + victim.getName() + ChatColor.RED + " was burned to a crisp";
    }

    public String getDrownedDeathMessage(Player victim) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + victim.getName() + ChatColor.RED + " couldn't swim";
    }

    public String getSuffocatedDeathMessage(Player victim) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + victim.getName() + ChatColor.RED + " tried to hide in a wall";
    }
    public String getTeamEliminatedMessage(String teamName) {
        return ChatColor.RED + "☠ | " + ChatColor.GRAY + "Team " + teamName + ChatColor.RED + " has been eliminated!";
    }
}