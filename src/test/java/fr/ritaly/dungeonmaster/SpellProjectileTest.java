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
package fr.ritaly.dungeonmaster;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Wall;

public class SpellProjectileTest extends TestCase {

	public SpellProjectileTest(String name) {
		super(name);
	}

	public void testSpellProjectile() throws Throwable {
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
		// | W | . | . | . | . | P | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(5, 5, 1), party);

		tiggy.cast(PowerRune.LO, Spell.Type.FIREBALL);
		tiggy.castSpell();

		assertTrue(dungeon.getElement(5, 5, 1).hasProjectiles());

		// Changement de position
		Clock.getInstance().tick(3);

		assertFalse(dungeon.getElement(5, 5, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 4, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 4, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 3, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 3, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 2, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 2, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile va exploser
		Clock.getInstance().tick(6);

		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile explose
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 1, 1).hasProjectiles());
	}
	
	public void testSpellProjectileExplodingInDoor() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | W | D | W | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | P | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);
		dungeon.setElement(4, 1, 1, new Wall());
		dungeon.setElement(5, 1, 1, new Door(Door.Style.WOODEN,
				Orientation.NORTH_SOUTH));
		dungeon.setElement(6, 1, 1, new Wall());

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(5, 5, 1), party);

		tiggy.cast(PowerRune.LO, Spell.Type.FIREBALL);
		tiggy.castSpell();

		assertTrue(dungeon.getElement(5, 5, 1).hasProjectiles());

		// Changement de position
		Clock.getInstance().tick(3);

		assertFalse(dungeon.getElement(5, 5, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 4, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 4, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 3, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 3, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 2, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 2, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile explose dans la porte
		Clock.getInstance().tick(6);

		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile explose
		Clock.getInstance().tick(6);

		// FIXME Le projectile doit détruire la porte
		assertFalse(dungeon.getElement(5, 1, 1).hasProjectiles());
	}
}