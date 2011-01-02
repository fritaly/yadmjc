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
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.map.Dungeon;

public class ItemProjectile extends AbstractProjectile {
	
	private final Item item;

	public ItemProjectile(Item item, Dungeon dungeon, Position position,
			Direction direction, SubCell subCell, int range) {
		
		super(dungeon, position, direction, subCell, range);
		
		Validate.notNull(item);

		this.item = item;
	}

	@Override
	protected void projectileDied() {
		// TODO Jouer le son de l'objet qui tombe à terre (dépend de l'objet !)
		// SoundSystem.getInstance().play(clip);

		// Déposer l'objet au sol (TODO Attention au sens pour les flèches !!)
		dungeon.getElement(getPosition()).itemDroppedDown(item, getSubCell());
	}
	
	// TODO La portée du projectile dépend du type de l'objet
}