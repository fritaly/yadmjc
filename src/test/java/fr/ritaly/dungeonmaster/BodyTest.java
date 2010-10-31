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
import fr.ritaly.dungeonmaster.champion.body.Body;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;

public class BodyTest extends TestCase {

	public BodyTest(String name) {
		super(name);
	}

	public void testIsWounded() {
		Champion champion = ChampionFactory.getFactory()
				.newChampion(Name.CHANI);

		final Body body = champion.getBody();

		// ---
		assertFalse(body.isWounded());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());

		// ---
		assertTrue(body.heal());
		assertFalse(body.isWounded());
	}

	public void testWound() {
		Champion champion = ChampionFactory.getFactory()
				.newChampion(Name.CHANI);

		final Body body = champion.getBody();

		// ---
		assertFalse(body.isWounded());

		// --- Blesser les 6 parties du corps
		for (int i = 0; i < 6; i++) {
			assertTrue(body.wound());
			assertTrue(body.isWounded());
		}

		// Toutes les parties du corps sont déjà blessées
		assertFalse(body.wound());
		assertTrue(body.isWounded());
	}

	public void testHeal() {
		Champion champion = ChampionFactory.getFactory()
				.newChampion(Name.CHANI);

		final Body body = champion.getBody();

		// ---
		assertFalse(body.isWounded());
		assertFalse(body.heal());
		assertFalse(body.isWounded());

		// ---
		for (int i = 0; i < 6; i++) {
			assertTrue(body.wound());
			assertTrue(body.isWounded());
		}

		for (int i = 0; i < 5; i++) {
			assertTrue(body.heal());
			assertTrue(body.isWounded());
		}

		assertTrue(body.heal());
		assertFalse(body.isWounded());
	}

	public void testGetWoundedParts() {
		Champion champion = ChampionFactory.getFactory()
				.newChampion(Name.CHANI);

		final Body body = champion.getBody();

		// ---
		assertFalse(body.isWounded());
		assertTrue(body.getWoundedParts().isEmpty());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(1, body.getWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(2, body.getWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(3, body.getWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(4, body.getWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(5, body.getWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(6, body.getWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(5, body.getWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(4, body.getWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(3, body.getWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(2, body.getWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getWoundedParts().isEmpty());
		assertEquals(1, body.getWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertFalse(body.isWounded());
		assertTrue(body.getWoundedParts().isEmpty());
		assertEquals(0, body.getWoundedParts().size());
	}

	public void testGetNotWoundedParts() {
		Champion champion = ChampionFactory.getFactory()
				.newChampion(Name.CHANI);

		final Body body = champion.getBody();

		// ---
		assertFalse(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(7, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(6, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(5, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(4, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(3, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(2, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(1, body.getNotWoundedParts().size());

		// ---
		assertFalse(body.wound());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(1, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(2, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(3, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(4, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(5, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertTrue(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(6, body.getNotWoundedParts().size());

		// ---
		assertTrue(body.heal());
		assertFalse(body.isWounded());
		assertFalse(body.getNotWoundedParts().isEmpty());
		assertEquals(7, body.getNotWoundedParts().size());

	}

	public void testGetTotalWeight() {
		Champion champion = ChampionFactory.getFactory()
				.newChampion(Name.CHANI);

		final Body body = champion.getBody();

		// ---
		assertEquals(0.0f, body.getTotalWeight());

		// ---
		final Item apple1 = ItemFactory.getFactory().newItem(Item.Type.APPLE);
		
		body.getWeaponHand().putOn(apple1);
		assertEquals(apple1.getWeight(), body.getTotalWeight());
	}

	public void testGetMagicResistance() {
		fail("Not yet implemented");
	}

	public void testGetDefense() {
		fail("Not yet implemented");
	}

}
