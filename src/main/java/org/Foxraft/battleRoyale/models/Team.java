package org.Foxraft.battleRoyale.models;

import java.util.List;

public class Team {
    private final String id;
    private List<String> players;
    private long score;

    public Team(String id, List<String> players) {
        this.id = id;
        this.players = players;
        this.score = 0;
    }

    public Team(String id, List<String> players, long score) {
        this.id = id;
        this.players = players;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public List<String> getPlayers() {
        return players;
    }

    public String getName() {
        return id;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
    public void setPlayers(List<String> players) {
        this.players = players;
    }
}