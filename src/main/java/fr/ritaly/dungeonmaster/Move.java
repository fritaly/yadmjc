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

/**
 * Enumération des différents déplacements possibles.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum Move {
	FORWARD,
	RIGHT,
	BACKWARD,
	LEFT,
	UP,
	DOWN,
	TURN_LEFT,
	TURN_RIGHT;

	public Direction changeDirection(Direction lookDirection) {
		if (lookDirection == null) {
			throw new IllegalArgumentException(
					"The given look direction is null");
		}

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
			throw new UnsupportedOperationException();
		}
	}

	public Direction getMoveDirection(Direction lookDirection) {
		if (lookDirection == null) {
			throw new IllegalArgumentException(
					"The given look direction is null");
		}

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
			throw new UnsupportedOperationException();
		}
	}

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
			throw new UnsupportedOperationException();
		}
	}

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
			throw new UnsupportedOperationException();
		}
	}
}
