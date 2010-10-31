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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.item.CarryLocation;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Pouch extends AbstractItemContainer {

	public Pouch(Champion champion) {
		super(champion, 2);
	}
	
	@Override
	protected String getName() {
		return "Pouch";
	}

	@Override
	protected boolean accepts(int index, Item item) {
		checkIndex(index);
		Validate.notNull(item, "The given item is null");
		
		// Pas d'influence de l'index ici
		return item.getCarryLocations().contains(CarryLocation.POUCH);
	}
}
