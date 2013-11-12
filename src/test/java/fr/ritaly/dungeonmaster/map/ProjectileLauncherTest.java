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
import fr.ritaly.dungeonmaster.Sector;
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

		final ProjectileLauncher launcher = new ProjectileLauncher(Direction.WEST, new SpellProjectileFactory(new Spell(
				PowerRune.MON, Spell.Type.FIREBALL)));

		final Level level1 = dungeon.createLevel(1, 10, 10);
		level1.setElement(9, 5, launcher);

		final Element neighbour = dungeon.getElement(8, 5, 1);

		// --- No projectile initially
		assertFalse(neighbour.hasProjectiles());

		// --- Trigger the launcher
		launcher.trigger();

		// --- Two projectiles appeared on the neighbour position in NE and SE
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(Sector.NORTH_EAST));
		assertNotNull(neighbour.getProjectiles().get(Sector.SOUTH_EAST));

		// Let the projectile move
		Clock.getInstance().tick(3);

		// --- Check that the 2 projectiles moved to NW and SW
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(Sector.NORTH_WEST));
		assertNotNull(neighbour.getProjectiles().get(Sector.SOUTH_WEST));
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

		final ProjectileLauncher launcher = new ProjectileLauncher(Direction.WEST, new ItemProjectileFactory(
				ItemFactory.getFactory(), Item.Type.POISON_DART));

		final Level level1 = dungeon.createLevel(1, 10, 10);
		level1.setElement(9, 5, launcher);

		final Element neighbour = dungeon.getElement(8, 5, 1);
		final Element target = level1.getElement(1, 5);

		// --- No projectile initially
		assertFalse(neighbour.hasProjectiles());
		assertFalse(target.hasProjectiles());

		// --- Trigger the launcher
		launcher.trigger();

		// --- Two projectiles appeared on the neighbour position in NE and SE
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(Sector.NORTH_EAST));
		assertNotNull(neighbour.getProjectiles().get(Sector.SOUTH_EAST));

		assertFalse(target.hasItems());

		// Let the projectile move
		Clock.getInstance().tick(3);

		// --- Check that the 2 projectiles moved to NW and SW
		assertTrue(neighbour.hasProjectiles());
		assertEquals(2, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(Sector.NORTH_WEST));
		assertNotNull(neighbour.getProjectiles().get(Sector.SOUTH_WEST));

		assertFalse(target.hasItems());

		// Let the projectile move to the opposite wall (wait long enough)
		Clock.getInstance().tick(60);

		assertFalse(neighbour.hasProjectiles());
		assertFalse(target.hasProjectiles());

		// There must be 2 items on the floor
		assertEquals(2, target.getItemCount());

		assertFalse(target.getItems(Sector.NORTH_WEST).isEmpty());
		assertEquals(1, target.getItemCount(Sector.NORTH_WEST));
		assertEquals(Item.Type.POISON_DART, target.getItems(Sector.NORTH_WEST).iterator().next().getType());

		assertFalse(target.getItems(Sector.SOUTH_WEST).isEmpty());
		assertEquals(1, target.getItemCount(Sector.SOUTH_WEST));
		assertEquals(Item.Type.POISON_DART, target.getItems(Sector.SOUTH_WEST).iterator().next().getType());
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}