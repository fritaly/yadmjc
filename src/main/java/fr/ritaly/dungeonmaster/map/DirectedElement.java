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
import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Position;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public abstract class DirectedElement extends Element {

	private final Direction direction;

	protected DirectedElement(Type type, Direction direction) {
		super(type);

		Validate.isTrue(direction != null, "The given direction is null");

		this.direction = direction;
	}

	public Direction getDirection() {
		return direction;
	}

	/**
	 * Retourne les {@link Element}s qui entourent celui-ci. Pour une porte, les
	 * éléments sont les deux murs situés de part et d'autre de la porte.
	 * 
	 * @return une List&lt;Element&gt;.
	 */
	public List<Element> getSurroundingElements() {
		final Position position1;
		final Position position2;

		if (Orientation.NORTH_SOUTH.equals(getDirection().getOrientation())) {
			position1 = getPosition().getWesternPosition();
			position2 = getPosition().getEasternPosition();
		} else {
			position1 = getPosition().getNorthernPosition();
			position2 = getPosition().getSouthernPosition();
		}

		final Level level = getLevel();

		return Arrays.asList(level.getElement(position1),
				level.getElement(position2));
	}
}
