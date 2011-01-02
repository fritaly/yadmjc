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
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Bones;

public class AltarTest extends TestCase {

	public AltarTest() {
	}

	public AltarTest(String name) {
		super(name);
	}

	public void testChampionResurrection() {
		// +---+---+---+
		// | W | A | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Altar altar = new Altar(Direction.SOUTH);

		final Dungeon dungeon = new Dungeon();
		final Level level = dungeon.createLevel(1, 3, 3);
		level.setElement(1, 0, altar);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		final Champion daroou = ChampionFactory.getFactory().newChampion(
				Name.DAROOU);

		final Party party = new Party();
		party.addChampion(tiggy);
		party.addChampion(daroou);

		dungeon.setParty(1, 1, 1, party);

		// Situation initiale
		assertTrue(tiggy.isAlive());
		assertTrue(daroou.isAlive());

		// Tiggy meurt
		assertTrue(tiggy.die());

		assertFalse(tiggy.isAlive());
		assertTrue(daroou.isAlive());

		// Récupérer les os de Tiggy
		assertNotNull(level.getElement(1, 1).getItems());
		assertEquals(1, level.getElement(1, 1).getItemCount());

		final Bones bones = (Bones) level.getElement(1, 1).getItems()
				.iterator().next();

		assertNotNull(bones.getChampion());
		assertEquals(tiggy, bones.getChampion());

		assertEquals(0, altar.getItemCount());
		
		// Placer les os dans l'autel réssuscite le champion
		altar.dropItem(bones, Direction.SOUTH);

		assertTrue(tiggy.isAlive());
		assertTrue(daroou.isAlive());
		
		// L'autel doit être vide
		assertEquals(0, altar.getItemCount());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}