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

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.projectile.ItemProjectileFactory;
import fr.ritaly.dungeonmaster.projectile.SpellProjectileFactory;

public class ProjectileLauncherTest extends TestCase {

	public ProjectileLauncherTest() {
	}

	public ProjectileLauncherTest(String name) {
		super(name);
	}

	public void testSpellProjectileLauncher() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | <-+---+---| P |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final ProjectileLauncher launcher = new ProjectileLauncher(
				Direction.WEST, new SpellProjectileFactory(new Spell(
						PowerRune.MON, Spell.Type.FIREBALL)));

		final Level level1 = dungeon.createLevel(1, 10, 10);
		level1.setElement(9, 5, launcher);

		final Element neighbour = dungeon.getElement(8, 5, 1);

		// --- Pas de projectile initialement
		assertFalse(neighbour.hasProjectiles());

		// --- Déclencher le lanceur
		launcher.trigger();

		// --- Deux projectiles sont apparus sur la position voisine en NE et SE
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(SubCell.NORTH_EAST));
		assertNotNull(neighbour.getProjectiles().get(SubCell.SOUTH_EAST));

		// Laisser le projectile bouger
		Clock.getInstance().tick(3);

		// --- Vérifier le déplacement des deux projectiles en NW et SW
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(SubCell.NORTH_WEST));
		assertNotNull(neighbour.getProjectiles().get(SubCell.SOUTH_WEST));
	}

	public void testItemProjectileLauncher() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | <-+---+---| P |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final ProjectileLauncher launcher = new ProjectileLauncher(
				Direction.WEST, new ItemProjectileFactory(
						ItemFactory.getFactory(), Item.Type.POISON_DART));

		final Level level1 = dungeon.createLevel(1, 10, 10);
		level1.setElement(9, 5, launcher);

		final Element neighbour = dungeon.getElement(8, 5, 1);
		final Element target = level1.getElement(1, 5);

		// --- Pas de projectile initialement
		assertFalse(neighbour.hasProjectiles());
		assertFalse(target.hasProjectiles());

		// --- Déclencher le lanceur
		launcher.trigger();

		// --- Deux projectiles sont apparus sur la position voisine en NE et SE
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(SubCell.NORTH_EAST));
		assertNotNull(neighbour.getProjectiles().get(SubCell.SOUTH_EAST));

		assertFalse(target.hasProjectiles());

		// Laisser le projectile bouger
		Clock.getInstance().tick(3);

		// --- Vérifier le déplacement des deux projectiles en NW et SW
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(SubCell.NORTH_WEST));
		assertNotNull(neighbour.getProjectiles().get(SubCell.SOUTH_WEST));

		assertFalse(target.hasProjectiles());

		// Laisser le projectile atteindre le mur opposée (attendre suffisamment
		// longtemps)
		Clock.getInstance().tick(60);

		assertFalse(neighbour.hasProjectiles());
		assertFalse(target.hasProjectiles());

		// Il doit y avoir 2 objets au sol
		assertEquals(2, target.getItemCount());

		assertFalse(target.getItems(SubCell.NORTH_WEST).isEmpty());
		assertEquals(1, target.getItemCount(SubCell.NORTH_WEST));
		assertEquals(Item.Type.POISON_DART, target.getItems(SubCell.NORTH_WEST)
				.iterator().next().getType());

		assertFalse(target.getItems(SubCell.SOUTH_WEST).isEmpty());
		assertEquals(1, target.getItemCount(SubCell.SOUTH_WEST));
		assertEquals(Item.Type.POISON_DART, target.getItems(SubCell.SOUTH_WEST)
				.iterator().next().getType());
	}
}