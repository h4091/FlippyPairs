package org.faudroids.distributedmemory.core;


import com.google.common.base.Objects;

/**
 * Describes one remote / local device by including information such
 * as name or number of pairs that fit on the screen etc.
 */
public final class Device {

	private final String name;
	private final int pairsCount;

	public Device(String name, int pairsCount) {
		this.name = name;
		this.pairsCount = pairsCount;
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
		return Objects.equal(name, device.name)
				&& Objects.equal(pairsCount, device.pairsCount);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(name, pairsCount);
	}

}
