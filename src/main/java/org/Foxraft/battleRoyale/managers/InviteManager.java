package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.models.Invite;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InviteManager {
    private final Map<String, Invite> invitations = new HashMap<>();
    private static final long INVITE_EXPIRATION_TIME = 60000; // 1 minute
    private final TeamManager teamManager;

    public InviteManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    public void invitePlayer(Player inviter, Player invitee) {
        if (inviter.equals(invitee)) {
            inviter.sendMessage("You cannot invite yourself.");
            return;
        }
        if (teamManager.isPlayerInAnyTeam(invitee)) {
            inviter.sendMessage(ChatColor.RED + "Player " + invitee.getName() + " is already part of a team.");
            return;
        }
        Team inviterTeam = teamManager.getTeams().values().stream()
                .filter(team -> team.getPlayers().contains(inviter.getName()))
                .findFirst()
                .orElse(null);
        if (inviterTeam != null && inviterTeam.getPlayers().size() >= 2) {
            inviter.sendMessage(ChatColor.RED + "Your team is already full.");
            return;
        }
        invitations.put(invitee.getName(), new Invite(inviter.getName(), invitee.getName()));
        invitee.sendMessage(ChatColor.GREEN + "You have been invited to join " + inviter.getName() + "'s team. Use " + ChatColor.GOLD + "/br team accept " + inviter.getName() + ChatColor.GREEN +" to join.");
    }

    public void acceptInvitation(Player invitee, Player inviter) {
        cleanUpExpiredInvites();
        Invite invite = invitations.get(invitee.getName());
        if (invite != null && invite.getInviter().equals(inviter.getName())) {
            teamManager.createTeam(inviter, invitee);
            invitations.remove(invitee.getName());
            invitee.sendMessage(ChatColor.GREEN + "You have joined " + inviter.getName() + "'s team.");
            inviter.sendMessage(ChatColor.GREEN + invitee.getName() + " has joined your team.");
        } else {
            invitee.sendMessage(ChatColor.RED + "No invitation found from " + inviter.getName() + ".");
        }
    }

    private void cleanUpExpiredInvites() {
        long currentTime = System.currentTimeMillis();
        invitations.entrySet().removeIf(entry -> currentTime - entry.getValue().getTimestamp() > INVITE_EXPIRATION_TIME);
    }
}