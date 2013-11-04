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

/**
 * Inside a party, champions are located at 4 different locations. The location
 * can be (front or rear) and (left or right).
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Location {
	FRONT_LEFT,
	FRONT_RIGHT,
	REAR_LEFT,
	REAR_RIGHT;

	/**
	 * Tells whether this location is at the front of the party.
	 *
	 * @return whether this location is at the front of the party.
	 */
	public boolean isFront() {
		return equals(FRONT_LEFT) || equals(FRONT_RIGHT);
	}

	/**
	 * Tells whether this location is at the rear of the party.
	 *
	 * @return whether this location is at the rear of the party.
	 */
	public boolean isRear() {
		return equals(REAR_LEFT) || equals(REAR_RIGHT);
	}

	/**
	 * Tells whether this location is on the left of the party.
	 *
	 * @return whether this location is on the left of the party.
	 */
	public boolean isLeft() {
		return equals(FRONT_LEFT) || equals(REAR_LEFT);
	}

	/**
	 * Tells whether this location is on the right of the party.
	 *
	 * @return whether this location is on the right of the party.
	 */
	public boolean isRight() {
		return equals(FRONT_RIGHT) || equals(REAR_RIGHT);
	}

	/**
	 * TODO Document this method
	 *
	 * @param lookDirection
	 * @return
	 */
	public boolean isNorth(Direction lookDirection) {
		Validate.notNull(lookDirection, "The given look direction is null");

		switch (lookDirection) {
		case EAST:
			return isLeft();
		case NORTH:
			return isFront();
		case SOUTH:
			return isRear();
		case WEST:
			return isRight();
		default:
			throw new UnsupportedOperationException("Unsupported location " + this);
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @param lookDirection
	 * @return
	 */
	public boolean isSouth(Direction lookDirection) {
		Validate.notNull(lookDirection, "The given look direction is null");

		switch (lookDirection) {
		case EAST:
			return isRight();
		case NORTH:
			return isRear();
		case SOUTH:
			return isFront();
		case WEST:
			return isLeft();
		default:
			throw new UnsupportedOperationException("Unsupported location " + this);
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @param lookDirection
	 * @return
	 */
	public boolean isEast(Direction lookDirection) {
		Validate.notNull(lookDirection, "The given look direction is null");

		switch (lookDirection) {
		case EAST:
			return isFront();
		case NORTH:
			return isRight();
		case SOUTH:
			return isLeft();
		case WEST:
			return isRear();
		default:
			throw new UnsupportedOperationException("Unsupported location " + this);
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @param lookDirection
	 * @return
	 */
	public boolean isWest(Direction lookDirection) {
		Validate.notNull(lookDirection, "The given look direction is null");

		switch (lookDirection) {
		case EAST:
			return isRear();
		case NORTH:
			return isLeft();
		case SOUTH:
			return isRight();
		case WEST:
			return isFront();
		default:
			throw new UnsupportedOperationException("Unsupported location " + this);
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @param direction
	 * @return
	 */
	public SubCell toSubCell(Direction direction) {
		return SubCell.fromLocation(direction, this);
	}

	/**
	 * TODO Document this method
	 *
	 * @param direction
	 * @param subCell
	 * @return
	 */
	public static Location fromSubCell(final Direction direction, final SubCell subCell) {
		Validate.notNull(direction, "The given direction is null");
		Validate.notNull(subCell, "The given sub-cell is null");

		switch (direction) {
		case NORTH:
			switch (subCell) {
			case NORTH_EAST:
				return FRONT_RIGHT;
			case NORTH_WEST:
				return FRONT_LEFT;
			case SOUTH_EAST:
				return REAR_RIGHT;
			case SOUTH_WEST:
				return REAR_LEFT;
			}
		case EAST:
			switch (subCell) {
			case NORTH_EAST:
				return FRONT_LEFT;
			case NORTH_WEST:
				return REAR_LEFT;
			case SOUTH_EAST:
				return FRONT_RIGHT;
			case SOUTH_WEST:
				return REAR_LEFT;
			}
		case SOUTH:
			switch (subCell) {
			case NORTH_EAST:
				return REAR_LEFT;
			case NORTH_WEST:
				return REAR_RIGHT;
			case SOUTH_EAST:
				return FRONT_LEFT;
			case SOUTH_WEST:
				return FRONT_RIGHT;
			}
		case WEST:
			switch (subCell) {
			case NORTH_EAST:
				return REAR_RIGHT;
			case NORTH_WEST:
				return FRONT_RIGHT;
			case SOUTH_EAST:
				return REAR_LEFT;
			case SOUTH_WEST:
				return FRONT_LEFT;
			}
		}

		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the locations at the front of the party.
	 *
	 * @return a list of locations. Never returns null.
	 */
	public static List<Location> getFront() {
		return Arrays.asList(FRONT_LEFT, FRONT_RIGHT);
	}

	/**
	 * Returns the locations at the rear of the party.
	 *
	 * @return a list of locations. Never returns null.
	 */
	public static List<Location> getRear() {
		return Arrays.asList(REAR_LEFT, REAR_RIGHT);
	}

	/**
	 * Returns the locations on the left of the party.
	 *
	 * @return a list of locations. Never returns null.
	 */
	public static List<Location> getLeft() {
		return Arrays.asList(FRONT_LEFT, REAR_LEFT);
	}

	/**
	 * Returns the locations on the right of the party.
	 *
	 * @return a list of locations. Never returns null.
	 */
	public static List<Location> getRight() {
		return Arrays.asList(FRONT_RIGHT, REAR_RIGHT);
	}
}