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

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.body.WeaponHand;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.stat.Stat;
import junit.framework.TestCase;

public class CurseTest extends TestCase {

	public CurseTest() {
	}

	public CurseTest(String name) {
		super(name);
	}

	public void testCurseDetection() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		// --- Torche envoûtée mais non détectée
		assertTrue(torch.isCursed());
		assertFalse(torch.isCurseDetected());

		// --- Prendre le torche en main
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));

		// --- Torche envoûtée mais non détectée
		assertTrue(torch.isCursed());
		assertFalse(torch.isCurseDetected());

		// --- Tenter de retirer la torche -> envoûtement détecté
		assertNull(tiggy.getBody().getWeaponHand().takeOff());
		assertEquals(torch, tiggy.getBody().getWeaponHand().getItem());

		assertTrue(torch.isCursed());
		assertTrue(torch.isCurseDetected());

		// --- Conjurer l'envoûtement
		torch.conjure(PowerRune.MON);

		// --- Tenter de retirer la torche -> doit marcher
		final Item item = tiggy.getBody().getWeaponHand().takeOff();

		assertEquals(torch, item);

		assertFalse(torch.isCursed());
		assertFalse(torch.isCurseDetected());
	}

	public void testInfluenceOfCurseOnLuck() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		final Stat luck = tiggy.getStats().getLuck();
		luck.value(10);

		// --- La chance doit valoir initialement 10 points
		assertEquals(10, luck.actualValue().intValue());

		// --- Tiggy prend la torche, sa chance diminue de +3
		tiggy.getBody().getWeaponHand().putOn(torch);

		assertEquals(7, luck.actualValue().intValue());

		// --- Tiggy lâche la torche, sa chance remonte de +3
		final Item item = tiggy.getBody().getWeaponHand().takeOff(true);

		assertNotNull(item);
		assertEquals(torch, item);

		// --- Tiggy reprend la torche, sa chance diminue de +3
		tiggy.getBody().getWeaponHand().putOn(torch);

		assertEquals(7, luck.actualValue().intValue());

		// --- Conjurer la torche fait remonter la chance de +3
		torch.conjure(PowerRune.EE);

		assertEquals(10, luck.actualValue().intValue());

		// --- Ensorceler la torche diminue la chance de +3
		torch.curse(PowerRune.LO);

		assertEquals(7, luck.actualValue().intValue());
	}

	public void testRemovalOfCursedItem() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		final WeaponHand hand = tiggy.getBody().getWeaponHand();
		hand.putOn(torch);

		// --- Tenter de retirer un objet envoûté doit échouer
		assertNull(hand.takeOff());

		// --- Le même test réussit si on force la chose
		final Item item = hand.takeOff(true);

		assertNotNull(item);
		assertEquals(torch, item);

		// --- Replacer la torche dans la main du champion
		hand.putOn(torch);

		// --- Conjurer l'objet puis retenter (doit réussir)
		torch.conjure(PowerRune.EE);

		final Item item2 = hand.takeOff();

		assertNotNull(item2);
		assertEquals(torch, item2);
	}
}