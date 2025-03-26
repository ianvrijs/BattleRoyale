package org.Foxraft.battleRoyale.models;

import org.bukkit.Location;

public class SpawnPoint {
    private final Location location;
    private boolean isOccupied;

    public SpawnPoint(Location location) {
        this.location = location;
        this.isOccupied = false;
    }

    public Location getLocation() { return location; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
}