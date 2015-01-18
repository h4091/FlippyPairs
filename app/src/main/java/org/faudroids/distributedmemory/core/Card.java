package org.faudroids.distributedmemory.core;


import com.google.common.base.Objects;

public final class Card implements Comparable<Card> {

	private final int id;
    private final int value;

    Card(int id, int value) {
        this.id = id;
        this.value = value;
    }


	public int getId() {
		return id;
	}


    public int getValue() {
        return value;
    }


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Card)) return false;
		if (other == this) return true;
		Card card = (Card) other;
		return Objects.equal(id, card.id)
				&& Objects.equal(value, card.value);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(id, value);
	}


    @Override
    public int compareTo(Card another) {
        return ((Integer) this.id).compareTo(another.getId());
    }

}
