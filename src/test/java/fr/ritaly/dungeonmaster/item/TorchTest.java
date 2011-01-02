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
import fr.ritaly.dungeonmaster.Constants;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.body.Body;

public class TorchTest extends TestCase {

	public TorchTest() {
	}

	public TorchTest(String name) {
		super(name);
	}

	public void testTorchBurning() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Torch torch = new Torch();

		// --- La torche est initialement éteinte et neuve
		assertFalse(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT, torch.getLight());

		// --- Le champion prend la torche en main, elle s'allume
		// automatiquement
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));
		assertTrue(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT, torch.getLight());

		// --- Laisser la torche se consumer (1 fois)
		Clock.getInstance().tick(Torch.TICK_COUNT);

		assertTrue(torch.isBurning());
		assertTrue(torch.getLight() < Constants.MAX_LIGHT);
		assertEquals(Constants.MAX_LIGHT - 1, torch.getLight());

		// --- Le champion lâche la torche, elle s'éteint automatiquement
		final Item removed = tiggy.getBody().getWeaponHand().takeOff();

		assertNotNull(removed);
		assertEquals(torch, removed);
		assertFalse(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT - 1, torch.getLight());
	}

	public void testTorchCanOnlyBeWornByRelevantBodyParts() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Body body = tiggy.getBody();
		final Torch torch = new Torch();

		// --- Mettre la torche sur la tête (doit échouer)
		assertEquals(torch, body.getHead().putOn(torch));

		// --- Mettre la torche sur le cou (doit échouer)
		assertEquals(torch, body.getNeck().putOn(torch));

		// --- Mettre la torche sur le torse (doit échouer)
		assertEquals(torch, body.getTorso().putOn(torch));

		// --- Mettre la torche sur les jambes (doit échouer)
		assertEquals(torch, body.getLegs().putOn(torch));

		// --- Mettre la torche sur les pieds (doit échouer)
		assertEquals(torch, body.getFeet().putOn(torch));

		// --- Mettre la torche dans une main (doit réussir)
		assertNull(body.getShieldHand().putOn(torch));

		// --- Retirer la torche de la main qui le tient (doit réussir)
		assertEquals(torch, body.getShieldHand().takeOff());

		// --- Mettre la torche dans l'autre main (doit réussir)
		assertNull(body.getWeaponHand().putOn(torch));

		// --- On ne peut mettre dans l'autre main la torche si elle est déjà
		// portée
		try {
			body.getShieldHand().putOn(torch);
			fail();
		} catch (RuntimeException e) {
			// OK
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}