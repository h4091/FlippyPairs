package org.faudroids.distributedmemory.core;

/**
 * Created by sim0n on 13.01.15.
 */
public class Player implements Comparable<Player> {
    private int points;
    private String name;
    private int id;

    Player(int id, String name)
    {
        this.name = name;
        this.points = 0;
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Player another) {
        return ((Integer) this.points).compareTo(another.getPoints());
    }
}
