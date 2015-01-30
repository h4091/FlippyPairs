package org.faudroids.distributedmemory.core;

public class Player implements Comparable<Player> {
    private String name;
    private int id;

    Player(int id, String name)
    {
        this.name = name;
        this.id = id;
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
