package org.faudroids.distributedmemory.core;


import com.google.common.base.Objects;

/**
 * Describes one remote / local device by including information such
 * as an unique id, number of pairs that fit on the screen etc.
 */
public final class Device {

	private final int id;
	private final String name;
	private final int pairsCount;

	public Device(int id, String name, int pairsCount) {
		this.id = id;
		this.name = name;
		this.pairsCount = pairsCount;
	}


	public int getId() {
		return id;
	}


	public String getName() {
		return name;
	}


	public int getPairsCount() {
		return pairsCount;
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Device)) return false;
		if (other == this) return true;
		Device device = (Device) other;
		return Objects.equal(id, device.id)
				&& Objects.equal(name, device.name)
				&& Objects.equal(pairsCount, device.pairsCount);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, pairsCount);
	}

}
