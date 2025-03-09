package org.Foxraft.battleRoyale.managers;

import org.Foxraft.battleRoyale.config.GameManagerConfig;
import org.Foxraft.battleRoyale.models.Invite;
import org.Foxraft.battleRoyale.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InviteManager {
    private final Map<String, Invite> invitations = new HashMap<>();
    private static final long INVITE_EXPIRATION_TIME = 60000; // 1 minute
    private final TeamManager teamManager;
    private final JavaPlugin plugin;

    public InviteManager(GameManagerConfig config) {
        this.teamManager = config.getTeamManager();
        this.plugin = config.getPlugin();
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
        if (invitations.containsKey(invitee.getName())) {
            inviter.sendMessage(ChatColor.RED + "Player " + invitee.getName() + " already has a pending invite.");
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
        Invite invite = new Invite(inviter.getName(), invitee.getName());
        invitations.put(invitee.getName(), invite);
        inviter.sendMessage(ChatColor.GREEN + "You have invited " + invitee.getName() + " to join your team.");
        invitee.sendMessage(ChatColor.GREEN + "You have been invited to join " + inviter.getName() + "'s team. Use " + ChatColor.GOLD + "/br team accept " + inviter.getName() + ChatColor.GREEN + " to join.");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (invitations.containsKey(invitee.getName()) && invitations.get(invitee.getName()).equals(invite)) {
                Player inviterPlayer = Bukkit.getPlayer(invite.getInviter());
                Player inviteePlayer = Bukkit.getPlayer(invite.getInvitee());
                if (inviterPlayer != null) {
                    inviterPlayer.sendMessage(ChatColor.RED + "Your invite to " + invite.getInvitee() + " has expired.");
                }
                if (inviteePlayer != null) {
                    inviteePlayer.sendMessage(ChatColor.RED + "The invite from " + invite.getInviter() + " has expired.");
                }
                invitations.remove(invitee.getName());
            }
        }, INVITE_EXPIRATION_TIME / 50);
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
        Iterator<Map.Entry<String, Invite>> iterator = invitations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Invite> entry = iterator.next();
            Invite invite = entry.getValue();
            if (currentTime - invite.getTimestamp() > INVITE_EXPIRATION_TIME) {
                Player inviter = Bukkit.getPlayer(invite.getInviter());
                Player invitee = Bukkit.getPlayer(invite.getInvitee());
                if (inviter != null) {
                    inviter.sendMessage(ChatColor.RED + "Your invite to " + invite.getInvitee() + " has expired.");
                }
                if (invitee != null) {
                    invitee.sendMessage(ChatColor.RED + "The invite from " + invite.getInviter() + " has expired.");
                }
                iterator.remove();
            }
        }
    }
}