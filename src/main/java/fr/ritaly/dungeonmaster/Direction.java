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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;

/**
 * Enumerates the possible move directions.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Direction implements Place {
	NORTH,
	EAST,
	SOUTH,
	WEST,
	UP,
	DOWN;

	/**
	 * The total number of directions.
	 */
	private static final int COUNT = values().length;

	/**
	 * Returns the direction opposed to this one. Example: if this is NORTH then
	 * returns SOUTH.
	 *
	 * @return a direction. Never returns null.
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
	 * Returns the next clock-wise direction after this one. Example: if this is
	 * NORTH then returns EAST.
	 *
	 * @return a direction. Never returns null.
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
	 * Returns the next anti clock-wise direction after this one. Example: if
	 * this is NORTH then returns WEST.
	 *
	 * @return a direction. Never returns null.
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
	 * Returns the target position reached when moving from the given position
	 * in this direction. Example: if this direction is NORTH and the position
	 * is (0,0,0) then the position reached by moving NORTH is (0,-1,0).
	 *
	 * @param position
	 *            a position representing the start position before moving.
	 *            Can't be null.
	 * @return a position representing the target position reached when moving
	 *         in this direction.
	 */
	public Position change(final Position position) {
		Validate.notNull(position, "The given position is null");

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
	 * Tells whether this direction is opposed to the given one.
	 *
	 * @param direction
	 *            the direction to test. Can't be null.
	 * @return whether this direction is opposed to the given one.
	 */
	public boolean isOpposite(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return direction.equals(getOpposite());
	}

	/**
	 * Tells whether this direction is the next clock-wise direction for the
	 * given direction. Example: Returns true if this direction is NORTH and the
	 * given direction is WEST.
	 *
	 * @param direction
	 *            the direction to test. Can't be null.
	 * @return whether this direction is the next clock-wise direction for the
	 *         given direction.
	 */
	public boolean isClockWiseDirection(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return direction.equals(getClockwiseDirection());
	}

	/**
	 * Tells whether this direction is the next anti clock-wise direction for
	 * the given direction. Example: Returns true if this direction is NORTH and
	 * the given direction is EAST.
	 *
	 * @param direction
	 *            the direction to test. Can't be null.
	 * @return whether this direction is the next anti clock-wise direction for
	 *         the given direction.
	 */
	public boolean isAntiClockWiseDirection(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return direction.equals(getAntiClockwiseDirection());
	}

	/**
	 * Returns a random direction.
	 *
	 * @return a direction. Never returns null.
	 */
	public static Direction random() {
		return values()[RandomUtils.nextInt(COUNT)];
	}

	/**
	 * Returns the orientation associated to this direction. Examples: Returns
	 * {@link Orientation#NORTH_SOUTH} for the directions NORTH and SOUTH
	 * otherwise returns {@link Orientation#WEST_EAST}
	 *
	 * @return an orientation. Never returns null.
	 */
	public Orientation getOrientation() {
		return (equals(NORTH) || equals(SOUTH)) ? Orientation.NORTH_SOUTH : Orientation.WEST_EAST;
	}
}
