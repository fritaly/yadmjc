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
package fr.ritaly.dungeonmaster.item;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.body.WeaponHand;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.stat.Stat;

public class CurseTest extends TestCase {

	public CurseTest() {
	}

	public CurseTest(String name) {
		super(name);
	}

	public void testCurseDetection() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Create a cursed torch
		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		// --- The torch is initially cursed but the curse is undetected
		assertTrue(torch.isCursed());
		assertFalse(torch.isCurseDetected());

		// --- Let Tiggy grab the torch
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));

		// --- The torch is cursed and still undetected
		assertTrue(torch.isCursed());
		assertFalse(torch.isCurseDetected());

		// --- Try dropping the torch -> the cursed is detected
		assertNull(tiggy.getBody().getWeaponHand().takeOff());
		assertEquals(torch, tiggy.getBody().getWeaponHand().getItem());

		assertTrue(torch.isCursed());
		assertTrue(torch.isCurseDetected());

		// --- Conjure the curse
		torch.conjure(PowerRune.MON);

		// --- Dropping the torch must now succeed
		final Item item = tiggy.getBody().getWeaponHand().takeOff();

		assertEquals(torch, item);

		assertFalse(torch.isCursed());
		assertFalse(torch.isCurseDetected());
	}

	public void testInfluenceOfCurseOnLuck() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Create a cursed torch
		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		final Stat luck = tiggy.getStats().getLuck();
		luck.baseValue(10);

		// --- Check the initial luck
		assertEquals(10, luck.value());

		// --- Grabbing a cursed item decreases the luck by 3 points
		tiggy.getBody().getWeaponHand().putOn(torch);

		assertEquals(7, luck.value());

		// --- Dropping the cursed item increases the luck by 3 points
		final Item item = tiggy.getBody().getWeaponHand().takeOff(true);

		assertNotNull(item);
		assertEquals(torch, item);

		// --- Tiggy grabs the torch again, the luck decreases again
		tiggy.getBody().getWeaponHand().putOn(torch);

		assertEquals(7, luck.value());

		// --- Conjure the torch, while in hand, the luck increases
		torch.conjure(PowerRune.EE);

		assertEquals(10, luck.value());

		// --- Cursing the torch, while in again, decreases the luck again
		torch.curse(PowerRune.LO);

		assertEquals(7, luck.value());
	}

	public void testRemovalOfCursedItem() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		final WeaponHand hand = tiggy.getBody().getWeaponHand();
		hand.putOn(torch);

		// --- A cursed item can't be removed
		assertNull(hand.takeOff());

		// --- The same test should succeed if the removal is forced
		final Item item = hand.takeOff(true);

		assertNotNull(item);
		assertEquals(torch, item);

		// --- Put the torch in the champion's hand again
		hand.putOn(torch);

		// --- Conjure the item and drop it (should succeed now)
		torch.conjure(PowerRune.EE);

		final Item item2 = hand.takeOff();

		assertNotNull(item2);
		assertEquals(torch, item2);
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}