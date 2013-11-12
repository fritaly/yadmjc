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
package fr.ritaly.dungeonmaster.projectile;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.map.Dungeon;

public class ItemProjectile extends AbstractProjectile {

	private final Item item;

	public ItemProjectile(Item item, Dungeon dungeon, Position position, Direction direction, Sector sector, int range) {
		super(dungeon, position, direction, sector, range);

		Validate.notNull(item);

		this.item = item;
	}

	@Override
	protected void projectileDied() {
		// TODO Play the sound of an item falling on the floor (depends on the item type)
		// SoundSystem.getInstance().play(clip);

		// Drop the item on the floor (TODO Arrows need to point towards the relevant direction !)
		dungeon.getElement(getPosition()).addItem(item, getSector());
	}

	// TODO The range of a projectile depends on the item type (its weight)
}