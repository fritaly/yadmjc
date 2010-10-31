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
package fr.ritaly.dungeonmaster.champion.inventory;

import java.util.EnumSet;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.item.CarryLocation;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Quiver extends AbstractItemContainer {

	public Quiver(Champion champion) {
		super(champion, 4);
	}
	
	@Override
	protected String getName() {
		return "Quiver";
	}

	@Override
	protected boolean accepts(int index, Item item) {
		// Attention à l'index passé en paramètre ici !
		checkIndex(index);
		Validate.notNull(item, "The given item is null");

		final EnumSet<CarryLocation> locations = item.getCarryLocations();

		// Index = 0 -> QUIVER1
		// Index = 1, 2 ou 3 -> QUIVER2
		return ((index == 0) && locations.contains(CarryLocation.QUIVER1))
				|| ((index > 0) && locations.contains(CarryLocation.QUIVER2));
	}
}
