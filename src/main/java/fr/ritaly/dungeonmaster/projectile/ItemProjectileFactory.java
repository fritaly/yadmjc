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
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.map.Dungeon;

public class ItemProjectileFactory implements ProjectileFactory {

	private final ItemFactory factory;

	private final Item.Type type;

	public ItemProjectileFactory(ItemFactory factory, Item.Type type) {
		Validate.notNull(factory, "The given item factory is null");
		Validate.notNull(type, "The given item type is null");

		this.factory = factory;
		this.type = type;
	}

	@Override
	public Projectile createProjectile(Dungeon dungeon, Position position,
			Direction direction, SubCell subCell) {

		// TODO Calculer la distance de jet de l'objet (fonction du champion si
		// besoin)

		// Créer un nouvel objet à chaque appel
		final Item item = factory.newItem(type);

		// Contrôles effectués par le constructeur de ItemProjectile
		return new ItemProjectile(item, dungeon, position, direction, subCell,
				30);
	}
}