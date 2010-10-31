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
 * Un {@link DirectionTransform} transforme une {@link Direction} en une autre.
 * Pourrait égaler s'appeler DirectionTransform.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum DirectionTransform {
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en elle-même.
	 */
	IDENTITY,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en la {@link Direction} située après elle selon le sens des aiguilles
	 * d'une montre.
	 */
	NEXT_CLOCKWISE,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en la {@link Direction} située après elle selon le sens inverse des
	 * aiguilles d'une montre.
	 */
	NEXT_ANTI_CLOCKWISE,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en sa {@link Direction} opposée.
	 */
	OPPOSITE,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en une autre {@link Direction} tirée au hasard.
	 */
	RANDOM,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en la {@link Direction#NORTH}.
	 */
	NORTH,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en la {@link Direction#SOUTH}.
	 */
	SOUTH,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en la {@link Direction#EAST}.
	 */
	EAST,
	/**
	 * Instance de {@link DirectionTransform} transformant une {@link Direction}
	 * en la {@link Direction#WEST}.
	 */
	WEST;

	public Direction transform(Direction direction) {
		Validate.isTrue(direction != null, "The given direction is null");

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