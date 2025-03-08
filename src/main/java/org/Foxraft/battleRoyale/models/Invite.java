package org.Foxraft.battleRoyale.models;

public class Invite {
    private final String inviter;
    private final String invitee;
    private final long timestamp;

    public Invite(String inviter, String invitee) {
        this.inviter = inviter;
        this.invitee = invitee;
        this.timestamp = System.currentTimeMillis();
    }

    public String getInviter() {
        return inviter;
    }

    public String getInvitee() {
        return invitee;
    }

    public long getTimestamp() {
        return timestamp;
    }
}