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

		// --- The torch isn't initially burning
		assertFalse(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT, torch.getLight());

		// --- Taking the torch automatically lights it
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));
		assertTrue(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT, torch.getLight());

		// --- Let the torch burn (wait at least 4 ticks)
		Clock.getInstance().tick(4);

		// Check that the torch actually burnt
		assertTrue(torch.isBurning());
		assertTrue(torch.getLight() < Constants.MAX_LIGHT);
		assertEquals(Constants.MAX_LIGHT - 1, torch.getLight());

		// --- Releasing the torch automatically puts it off
		final Item removed = tiggy.getBody().getWeaponHand().takeOff();

		assertNotNull(removed);
		assertEquals(torch, removed);
		assertFalse(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT - 1, torch.getLight());
	}

	public void testTorchCanOnlyBeWornByRelevantBodyParts() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Body body = tiggy.getBody();
		final Torch torch = new Torch();

		// --- Can't put the torch on the head, neck, torso, legs or feets
		assertEquals(torch, body.getHead().putOn(torch));
		assertEquals(torch, body.getNeck().putOn(torch));
		assertEquals(torch, body.getTorso().putOn(torch));
		assertEquals(torch, body.getLegs().putOn(torch));
		assertEquals(torch, body.getFeet().putOn(torch));

		// --- Can take the torch in the both hands
		assertNull(body.getShieldHand().putOn(torch));
		assertEquals(torch, body.getShieldHand().takeOff());

		assertNull(body.getWeaponHand().putOn(torch));

		try {
			// --- The same torch can't be held in both hands
			body.getShieldHand().putOn(torch);
			fail();
		} catch (RuntimeException e) {
			// OK
		}
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}