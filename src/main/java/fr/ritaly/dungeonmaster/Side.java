package fr.ritaly.dungeonmaster;

import java.util.EnumSet;
import java.util.Set;

public enum Side {
	FRONT,
	LEFT,
	RIGHT,
	REAR;

	public Set<Location> getLocations() {
		switch (this) {
		case FRONT:
			return EnumSet.of(Location.FRONT_LEFT, Location.FRONT_RIGHT);
		case LEFT:
			return EnumSet.of(Location.FRONT_LEFT, Location.REAR_LEFT);
		case RIGHT:
			return EnumSet.of(Location.FRONT_RIGHT, Location.REAR_RIGHT);
		case REAR:
			return EnumSet.of(Location.REAR_LEFT, Location.REAR_RIGHT);
		default:
			throw new UnsupportedOperationException("Unsupported method for "
					+ this);
		}
	}
}