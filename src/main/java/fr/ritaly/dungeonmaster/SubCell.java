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
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum SubCell {
	NORTH_WEST,
	NORTH_EAST,
	SOUTH_WEST,
	SOUTH_EAST;

	private static final int COUNT = values().length;

	public static SubCell random() {
		return SubCell.values()[RandomUtils.nextInt(COUNT)];
	}

	/**
	 * Tire au hasard parmi les deux {@link SubCell}s visibles de la
	 * {@link Direction} de regard donnée et le retourne.
	 * 
	 * @param direction
	 *            une {@link Direction} de regard.
	 * @return une instance de {@link SubCell}.
	 */
	public static SubCell randomVisible(Direction direction) {
		Validate.notNull(direction);

		return getVisibleSubCells(direction).get(RandomUtils.nextInt(2));
	}

	/**
	 * Compte tenu de la {@link Direction} de regard donnée, retourne les deux
	 * instances de {@link SubCell} visibles de celle-ci. Permet de savoir quand
	 * la {@link Party} est située sur un {@link Element} quelles sont les deux
	 * {@link SubCell}s visibles.
	 * 
	 * @param direction
	 *            une {@link Direction} de regard.
	 * @return une {@link List} contenant les deux {@link SubCell}s visibles.
	 */
	public static List<SubCell> getVisibleSubCells(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}

		switch (direction) {
		case EAST:
			return getEasternSubCells();
		case NORTH:
			return getNorthernSubCells();
		case WEST:
			return getWesternSubCells();
		case SOUTH:
			return getSouthernSubCells();
		default:
			throw new UnsupportedOperationException();
		}
	}

	public static List<SubCell> getSouthernSubCells() {
		return Arrays.asList(SOUTH_EAST, SOUTH_WEST);
	}

	public static List<SubCell> getWesternSubCells() {
		return Arrays.asList(NORTH_WEST, SOUTH_WEST);
	}

	public static List<SubCell> getNorthernSubCells() {
		return Arrays.asList(NORTH_EAST, NORTH_WEST);
	}

	public static List<SubCell> getEasternSubCells() {
		return Arrays.asList(NORTH_EAST, SOUTH_EAST);
	}

	public static SubCell fromLocation(final Direction direction,
			final Location location) {
		
		Validate.isTrue(direction != null, "The given direction is null");
		Validate.isTrue(location != null, "The given location is null");

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

		throw new UnsupportedOperationException();
	}

	public Location toLocation(Direction direction) {
		Validate.notNull(direction);

		return Location.fromSubCell(direction, this);
	}

	public boolean isTowardsEast() {
		return equals(NORTH_EAST) || equals(SOUTH_EAST);
	}

	public boolean isTowardsWest() {
		return equals(NORTH_WEST) || equals(SOUTH_WEST);
	}

	public boolean isTowardsNorth() {
		return equals(NORTH_EAST) || equals(NORTH_WEST);
	}

	public boolean isTowardsSouth() {
		return equals(SOUTH_WEST) || equals(SOUTH_EAST);
	}

	/**
	 * Indique si la {@link SubCell} donnée est voisine de cette {@link SubCell}
	 * . Pour être voisine, les deux {@link SubCell}s doivent partager une arête
	 * commune.
	 * 
	 * @param subCell
	 *            une instance de {@link SubCell}.
	 * @return si cette {@link SubCell} est voisine de celle donnée en
	 *         paramètre.
	 */
	public boolean isNeighbourOf(SubCell subCell) {
		Validate.notNull(subCell);

		switch (this) {
		case NORTH_EAST:
			return isTowardsNorth() || isTowardsEast();
		case NORTH_WEST:
			return isTowardsNorth() || isTowardsWest();
		case SOUTH_EAST:
			return isTowardsSouth() || isTowardsEast();
		case SOUTH_WEST:
			return isTowardsSouth() || isTowardsWest();
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Indique si lorsqu'on avance dans la {@link Direction} donnée, on quitte
	 * la position actuelle en se déplaçant sur une position voisine ou si on
	 * reste sur la même position.
	 * 
	 * @param direction
	 *            une instance de {@link Direction}.
	 * @return si lorsqu'on avance dans la {@link Direction} donnée, on quitte
	 *         la position actuelle.
	 */
	public boolean changesPosition(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		switch (this) {
		case NORTH_EAST:
			return Direction.NORTH.equals(direction)
					|| Direction.EAST.equals(direction);
		case NORTH_WEST:
			return Direction.NORTH.equals(direction)
					|| Direction.WEST.equals(direction);
		case SOUTH_EAST:
			return Direction.SOUTH.equals(direction)
					|| Direction.EAST.equals(direction);
		case SOUTH_WEST:
			return Direction.SOUTH.equals(direction)
					|| Direction.WEST.equals(direction);
		default:
			throw new UnsupportedOperationException();
		}
	}

	public SubCell towardsNorth() {
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

	public SubCell towardsSouth() {
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

	public SubCell towardsWest() {
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

	public SubCell towardsEast() {
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

	public SubCell towards(Direction direction) {
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
