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
 * A transform turning a direction into another.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum DirectionTransform {
	/**
	 * The identify transform turns a direction into itself.
	 */
	IDENTITY,

	/**
	 * Transform to turn a direction into the next clock-wise direction.
	 * Example: {@link Direction#NORTH} will be turned into
	 * {@link Direction#EAST}.
	 */
	NEXT_CLOCKWISE,

	/**
	 * Transform to turn a direction into the next anti clock-wise direction.
	 * Example: {@link Direction#EAST} will be turned into
	 * {@link Direction#NORTH}.
	 */
	NEXT_ANTI_CLOCKWISE,

	/**
	 * Transform to turn a direction into the opposite direction. Example:
	 * {@link Direction#NORTH} will be turned into {@link Direction#SOUTH}.
	 */
	OPPOSITE,

	/**
	 * Transform to turn a direction into another random direction.
	 */
	RANDOM,

	/**
	 * Transform to turn a direction into the {@link Direction#NORTH} direction.
	 */
	NORTH,

	/**
	 * Transform to turn a direction into the {@link Direction#SOUTH} direction.
	 */
	SOUTH,

	/**
	 * Transform to turn a direction into the {@link Direction#EAST} direction.
	 */
	EAST,

	/**
	 * Transform to turn a direction into the {@link Direction#WEST} direction.
	 */
	WEST;

	/**
	 * Transforms the given direction into another one and returns it.
	 *
	 * @param direction
	 *            the direction to transform. Can't be null.
	 * @return the transformed direction. Never returns null.
	 */
	public Direction transform(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		switch (this) {
		case IDENTITY:
			return direction;
		case NEXT_CLOCKWISE:
			return direction.getClockwiseDirection();
		case NEXT_ANTI_CLOCKWISE:
			return direction.getAntiClockwiseDirection();
		case OPPOSITE:
			return direction.getOpposite();
		case RANDOM:
			return Direction.random();
		case NORTH:
			return Direction.NORTH;
		case EAST:
			return Direction.EAST;
		case SOUTH:
			return Direction.SOUTH;
		case WEST:
			return Direction.WEST;
		default:
			throw new UnsupportedOperationException();
		}
	}
}