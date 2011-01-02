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

import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Position;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public abstract class OrientedElement extends Element {

	private final Orientation orientation;

	protected OrientedElement(Type type, Orientation orientation) {
		super(type);

		Validate.isTrue(orientation != null, "The given orientation is null");

		this.orientation = orientation;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * Retourne les {@link Element}s qui entourent celui-ci. Pour une porte, les
	 * éléments sont théoriquement deux murs situés de part et d'autre de la
	 * porte.
	 * 
	 * @return une List&lt;Element&gt;.
	 */
	public List<Element> getSurroundingElements() {
		final Position position1;
		final Position position2;

		if (Orientation.NORTH_SOUTH.equals(getOrientation())) {
			position1 = getPosition().towardsWest();
			position2 = getPosition().towardsEast();
		} else {
			position1 = getPosition().towardsNorth();
			position2 = getPosition().towardsSouth();
		}

		final Element element1 = getLevel()
				.getElement(position1.x, position1.y);
		final Element element2 = getLevel()
				.getElement(position2.x, position2.y);
		
		return Arrays.asList(element1, element2);
	}
}