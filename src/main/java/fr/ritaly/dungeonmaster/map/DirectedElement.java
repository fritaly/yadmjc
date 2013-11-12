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
package fr.ritaly.dungeonmaster.map;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.HasDirection;
import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Position;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public abstract class DirectedElement extends Element implements HasDirection {

	private final Direction direction;

	protected DirectedElement(Type type, Direction direction) {
		super(type);

		Validate.notNull(direction, "The given direction is null");

		this.direction = direction;
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public List<Element> getSurroundingElements() {
		// For a door, the 2 elements on the left and right of the door are
		// supposed to be concrete and therefore can't be traversed so no need
		// to return them
		final Position position1;
		final Position position2;

		if (Orientation.NORTH_SOUTH.equals(getDirection().getOrientation())) {
			position1 = getPosition().towards(Direction.WEST);
			position2 = getPosition().towards(Direction.EAST);
		} else {
			position1 = getPosition().towards(Direction.NORTH);
			position2 = getPosition().towards(Direction.SOUTH);
		}

		final Level level = getLevel();

		return Arrays.asList(level.getElement(position1.x, position1.y),
				level.getElement(position2.x, position2.y));
	}

	@Override
	public void setDirection(Direction direction) {
		throw new UnsupportedOperationException("The direction can't be mutated once set");
	}
}
