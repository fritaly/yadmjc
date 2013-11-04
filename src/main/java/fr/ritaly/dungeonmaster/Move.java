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

/**
 * Enumerates the possible moves. A move can be absolute (for instance
 * "Move north") or relative (for instance "Move forward). This enumeration
 * defines some relative moves (forward, right, backward, left) and some
 * absolute moves (up, down).<br>
 * <br>
 * A relative move (for instance "Move forward") and a direction (say "North")
 * can be combined into an absolute move (in this case "Move North").
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Move {
	/** The (relative) move of stepping forward. */
	FORWARD,

	/** The (relative) move of stepping right. */
	RIGHT,

	/** The (relative) move of stepping backwards. */
	BACKWARD,

	/** The (relative) move of stepping left. */
	LEFT,

	/** The (absolute) move of going up (climbing). */
	UP,

	/** The (absolute) move of going down (climbing down). */
	DOWN,

	/** The special move of turning leftward. */
	TURN_LEFT,

	/** The special move of turning rightwrad. */
	TURN_RIGHT;

	/**
	 * TODO Document this method
	 *
	 * @param lookDirection
	 * @return
	 */
	public Direction changeDirection(Direction lookDirection) {
		Validate.notNull(lookDirection, "The given look direction is null");

		switch (this) {
		case BACKWARD:
		case DOWN:
		case FORWARD:
		case LEFT:
		case RIGHT:
		case UP:
			return lookDirection;
		case TURN_LEFT:
			return lookDirection.getAntiClockwiseDirection();
		case TURN_RIGHT:
			return lookDirection.getClockwiseDirection();
		default:
			throw new UnsupportedOperationException("Unsupported move " + this);
		}
	}

	/**
	 * TODO Document this method
	 *
	 * @param lookDirection
	 * @return
	 */
	public Direction getMoveDirection(Direction lookDirection) {
		Validate.notNull(lookDirection, "The given look direction is null");

		switch (this) {
		case FORWARD:
			return lookDirection;
		case BACKWARD:
			return lookDirection.getOpposite();
		case LEFT:
			return lookDirection.getAntiClockwiseDirection();
		case RIGHT:
			return lookDirection.getClockwiseDirection();
		case DOWN:
			return Direction.DOWN;
		case UP:
			return Direction.UP;
		case TURN_LEFT:
		case TURN_RIGHT:
			return lookDirection;
		default:
			throw new UnsupportedOperationException("Unsupported move " + this);
		}
	}

	/**
	 * Tells whether this move changes the position. Returns false for
	 * {@link #TURN_LEFT} and {@link #TURN_RIGHT} otherwise returns true.
	 *
	 * @return whether this move changes the position.
	 */
	public boolean changesPosition() {
		switch (this) {
		case BACKWARD:
		case DOWN:
		case FORWARD:
		case LEFT:
		case RIGHT:
		case UP:
			return true;
		case TURN_LEFT:
		case TURN_RIGHT:
			return false;
		default:
			throw new UnsupportedOperationException("Unsupported move " + this);
		}
	}

	/**
	 * Tells whether this move changes the direction. Returns true for
	 * {@link #TURN_LEFT} and {@link #TURN_RIGHT} otherwise returns false.
	 *
	 * @return whether this move changes the direction.
	 */
	public boolean changesDirection() {
		switch (this) {
		case BACKWARD:
		case DOWN:
		case FORWARD:
		case LEFT:
		case RIGHT:
		case UP:
			return false;
		case TURN_LEFT:
		case TURN_RIGHT:
			return true;
		default:
			throw new UnsupportedOperationException("Unsupported move " + this);
		}
	}
}
