package org.faudroids.distributedmemory.core;

import com.google.common.base.Objects;

public class Player {

    private final String name;
    private final int id;

    Player(int id, String name) {
        this.name = name;
        this.id = id;
    }


	public int getId() {
		return id;
	}


    public String getName() {
        return name;
    }


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Player)) return false;
		Player player = (Player) other;
		return name.equals(player.name) && id == player.id;
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(id, name);
	}

}
