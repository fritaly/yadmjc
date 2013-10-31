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

import org.apache.commons.lang.math.RandomUtils;

/**
 * Enum�ration des diff�rentes {@link Direction}s de d�placement possibles.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Direction {
	NORTH,
	EAST,
	SOUTH,
	WEST,
	UP,
	DOWN;
	
	private static final int COUNT = values().length;

	/**
	 * Retourne la {@link Direction} oppos�e � cette {@link Direction}.
	 * 
	 * @return une instance de {@link Direction}.
	 */
	public Direction getOpposite() {
		switch (this) {
		case EAST:
			return WEST;
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		case WEST:
			return EAST;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Retourne la {@link Direction} suivant cette {@link Direction} en tournant
	 * dans le sens des aiguilles d'une montre.
	 * 
	 * @return une instance de {@link Direction}.
	 */
	public Direction getClockwiseDirection() {
		switch (this) {
		case EAST:
			return SOUTH;
		case NORTH:
			return EAST;
		case SOUTH:
			return WEST;
		case WEST:
			return NORTH;
		case UP:
			return UP;
		case DOWN:
			return DOWN;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Retourne la {@link Direction} suivant cette {@link Direction} en tournant
	 * dans le sens inverse des aiguilles d'une montre.
	 * 
	 * @return une instance de {@link Direction}.
	 */
	public Direction getAntiClockwiseDirection() {
		switch (this) {
		case EAST:
			return NORTH;
		case NORTH:
			return WEST;
		case SOUTH:
			return EAST;
		case WEST:
			return SOUTH;
		case UP:
			return UP;
		case DOWN:
			return DOWN;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Retourne la {@link Position} cible atteinte � partir de la
	 * {@link Position} donn�e quand on se d�place dans cette {@link Direction}.
	 * 
	 * @param position
	 *            une {@link Position} de d�part. Ne peut �tre nul.
	 * @return une instance de {@link Position} repr�sentant la {@link Position}
	 *         cible atteinte.
	 */
	public Position change(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		switch (this) {
		case NORTH:
			return new Position(position.x, position.y - 1, position.z);
		case DOWN:
			return new Position(position.x, position.y, position.z + 1);
		case EAST:
			return new Position(position.x + 1, position.y, position.z);
		case SOUTH:
			return new Position(position.x, position.y + 1, position.z);
		case UP:
			return new Position(position.x, position.y, position.z - 1);
		case WEST:
			return new Position(position.x - 1, position.y, position.z);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Indique si cette {@link Direction} est l'oppos�e de celle donn�e.
	 * 
	 * @param direction
	 *            une instance de {@link Direction}. Ne peut �tre nul.
	 * @return si cette {@link Direction} est l'oppos�e de celle donn�e.
	 */
	public boolean isOpposite(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}

		return direction.equals(getOpposite());
	}

	public boolean isClockWiseDirection(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}

		return direction.equals(getClockwiseDirection());
	}

	public boolean isAntiClockWiseDirection(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}

		return direction.equals(getAntiClockwiseDirection());
	}

	/**
	 * Retourne une {@link Direction} au hasard.
	 * 
	 * @return une instance de {@link Direction}.
	 */
	public static Direction random() {
		return values()[RandomUtils.nextInt(COUNT)];
	}

	/**
	 * Retourne l'orientation associ�e � cette {@link Direction}.
	 * 
	 * @return une instance de {@link Orientation}.
	 */
	public Orientation getOrientation() {
		return (NORTH.equals(this) || SOUTH.equals(this)) ? Orientation.NORTH_SOUTH
				: Orientation.WEST_EAST;
	}
}
