/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.ritaly.dungeonmaster;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;

import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Element;

/**
 * A sector is similar to a {@link Location} except that a sector is an absolute
 * coordinate (that is, it doesn't depend on the looking direction).
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Sector implements Place {
	NORTH_WEST,
	NORTH_EAST,
	SOUTH_WEST,
	SOUTH_EAST;

	/**
	 * The total number of existing sectors.
	 */
	private static final int COUNT = values().length;

	/**
	 * Returns a random sector.
	 *
	 * @return a sector. Never returns null.
	 */
	public static Sector random() {
		return Sector.values()[RandomUtils.nextInt(COUNT)];
	}

	/**
	 * TODO Translate this javadoc to english
	 * Tire au hasard parmi les deux {@link Sector}s visibles de la
	 * {@link Direction} de regard donn�e et le retourne.
	 *
	 * @param direction
	 *            une {@link Direction} de regard.
	 * @return une instance de {@link Sector}.
	 */
	public static Sector randomVisible(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return getVisibleSectors(direction).get(RandomUtils.nextInt(2));
	}

	/**
	 * TODO Translate this javadoc to english
	 * Compte tenu de la {@link Direction} de regard donn�e, retourne les deux
	 * instances de {@link Sector} visibles de celle-ci. Permet de savoir quand
	 * la {@link Party} est situ�e sur un {@link Element} quelles sont les deux
	 * {@link Sector}s visibles.
	 *
	 * @param direction
	 *            une {@link Direction} de regard.
	 * @return une {@link List} contenant les deux {@link Sector}s visibles.
	 */
	public static List<Sector> getVisibleSectors(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		switch (direction) {
		case EAST:
			return getEasternSectors();
		case NORTH:
			return getNorthernSectors();
		case WEST:
			return getWesternSectors();
		case SOUTH:
			return getSouthernSectors();
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns the sectors towards south as a list.
	 *
	 * @return a list of sectors. Never returns null.
	 */
	public static List<Sector> getSouthernSectors() {
		return Arrays.asList(SOUTH_EAST, SOUTH_WEST);
	}

	/**
	 * Returns the sectors towards west as a list.
	 *
	 * @return a list of sectors. Never returns null.
	 */
	public static List<Sector> getWesternSectors() {
		return Arrays.asList(NORTH_WEST, SOUTH_WEST);
	}

	/**
	 * Returns the sectors towards north as a list.
	 *
	 * @return a list of sectors. Never returns null.
	 */
	public static List<Sector> getNorthernSectors() {
		return Arrays.asList(NORTH_EAST, NORTH_WEST);
	}

	/**
	 * Returns the sectors towards east as a list.
	 *
	 * @return a list of sectors. Never returns null.
	 */
	public static List<Sector> getEasternSectors() {
		return Arrays.asList(NORTH_EAST, SOUTH_EAST);
	}

	/**
	 * TODO Document this method
	 *
	 * @param direction
	 * @param location
	 * @return
	 */
	public static Sector fromLocation(final Direction direction, final Location location) {
		Validate.notNull(direction, "The given direction is null");
		Validate.notNull(location, "The given location is null");

		switch (direction) {
		case NORTH:
			switch (location) {
			case FRONT_LEFT:
				return NORTH_WEST;
			case FRONT_RIGHT:
				return NORTH_EAST;
			case REAR_LEFT:
				return SOUTH_WEST;
			case REAR_RIGHT:
				return NORTH_EAST;
			}
		case SOUTH:
			switch (location) {
			case FRONT_LEFT:
				return SOUTH_EAST;
			case FRONT_RIGHT:
				return SOUTH_WEST;
			case REAR_LEFT:
				return NORTH_EAST;
			case REAR_RIGHT:
				return NORTH_WEST;
			}
		case EAST:
			switch (location) {
			case FRONT_LEFT:
				return NORTH_EAST;
			case FRONT_RIGHT:
				return SOUTH_EAST;
			case REAR_LEFT:
				return NORTH_WEST;
			case REAR_RIGHT:
				return SOUTH_WEST;
			}
		case WEST:
			switch (location) {
			case FRONT_LEFT:
				return SOUTH_WEST;
			case FRONT_RIGHT:
				return NORTH_WEST;
			case REAR_LEFT:
				return SOUTH_EAST;
			case REAR_RIGHT:
				return NORTH_EAST;
			}
		}

		throw new UnsupportedOperationException("Unsupported direction " + direction);
	}

	/**
	 * TODO Document this method
	 *
	 * @param direction
	 * @return
	 */
	public Location toLocation(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return Location.fromSector(direction, this);
	}

	/**
	 * Tells whether this sector is towards east.
	 *
	 * @return whether this sector is towards east.
	 */
	public boolean isEastern() {
		return equals(NORTH_EAST) || equals(SOUTH_EAST);
	}

	/**
	 * Tells whether this sector is towards west.
	 *
	 * @return whether this sector is towards west.
	 */
	public boolean isWestern() {
		return equals(NORTH_WEST) || equals(SOUTH_WEST);
	}

	/**
	 * Tells whether this sector is towards north.
	 *
	 * @return whether this sector is towards north.
	 */
	public boolean isNorthern() {
		return equals(NORTH_EAST) || equals(NORTH_WEST);
	}

	/**
	 * Tells whether this sector is towards south.
	 *
	 * @return whether this sector is towards south.
	 */
	public boolean isSouthern() {
		return equals(SOUTH_WEST) || equals(SOUTH_EAST);
	}

	/**
	 * TODO Translate this javadoc to english
	 *
	 * Indique si la {@link Sector} donn�e est voisine de cette {@link Sector}
	 * . Pour �tre voisine, les deux {@link Sector}s doivent partager une ar�te
	 * commune.
	 *
	 * @param sector
	 *            une instance de {@link Sector}.
	 * @return si cette {@link Sector} est voisine de celle donn�e en
	 *         param�tre.
	 */
	public boolean isNeighbourOf(Sector sector) {
		Validate.notNull(sector);

		switch (this) {
		case NORTH_EAST:
			return isNorthern() || isEastern();
		case NORTH_WEST:
			return isNorthern() || isWestern();
		case SOUTH_EAST:
			return isSouthern() || isEastern();
		case SOUTH_WEST:
			return isSouthern() || isWestern();
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * TODO Translate this javadoc to english
	 *
	 * Indique si lorsqu'on avance dans la {@link Direction} donn�e, on quitte
	 * la position actuelle en se d�pla�ant sur une position voisine ou si on
	 * reste sur la m�me position.
	 *
	 * @param direction
	 *            une instance de {@link Direction}.
	 * @return si lorsqu'on avance dans la {@link Direction} donn�e, on quitte
	 *         la position actuelle.
	 */
	public boolean changesPosition(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		switch (this) {
		case NORTH_EAST:
			return Direction.NORTH.equals(direction) || Direction.EAST.equals(direction);
		case NORTH_WEST:
			return Direction.NORTH.equals(direction) || Direction.WEST.equals(direction);
		case SOUTH_EAST:
			return Direction.SOUTH.equals(direction) || Direction.EAST.equals(direction);
		case SOUTH_WEST:
			return Direction.SOUTH.equals(direction) || Direction.WEST.equals(direction);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @return
	 */
	public Sector towardsNorth() {
		switch (this) {
		case NORTH_EAST:
			return SOUTH_EAST;
		case NORTH_WEST:
			return SOUTH_WEST;
		case SOUTH_EAST:
			return NORTH_EAST;
		case SOUTH_WEST:
			return NORTH_WEST;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @return
	 */
	public Sector towardsSouth() {
		switch (this) {
		case NORTH_EAST:
			return SOUTH_EAST;
		case NORTH_WEST:
			return SOUTH_WEST;
		case SOUTH_EAST:
			return NORTH_EAST;
		case SOUTH_WEST:
			return NORTH_WEST;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @return
	 */
	public Sector towardsWest() {
		switch (this) {
		case NORTH_EAST:
			return NORTH_WEST;
		case NORTH_WEST:
			return NORTH_EAST;
		case SOUTH_EAST:
			return SOUTH_WEST;
		case SOUTH_WEST:
			return SOUTH_EAST;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @return
	 */
	public Sector towardsEast() {
		switch (this) {
		case NORTH_EAST:
			return NORTH_WEST;
		case NORTH_WEST:
			return NORTH_EAST;
		case SOUTH_EAST:
			return SOUTH_WEST;
		case SOUTH_WEST:
			return SOUTH_EAST;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @param direction
	 * @return
	 */
	public Sector towards(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		switch (direction) {
		case EAST:
			return towardsEast();
		case NORTH:
			return towardsNorth();
		case SOUTH:
			return towardsSouth();
		case WEST:
			return towardsWest();
		default:
			throw new UnsupportedOperationException();
		}
	}
}
