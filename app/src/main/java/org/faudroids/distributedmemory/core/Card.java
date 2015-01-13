package org.faudroids.distributedmemory.core;

/**
 * Created by sim0n on 13.01.15.
 */
public class Card implements Comparable<Card> {
    private int value;
    private int id;
    private boolean closed;

    Card(int id, int value)
    {
        this.id = id;
        this.value = value;
        this.closed = false;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Card another) {
        return ((Integer) this.id).compareTo(another.getId());
    }
}
