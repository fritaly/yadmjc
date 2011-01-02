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
package fr.ritaly.dungeonmaster.champion.body;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.magic.ElementRune;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;

public class WeaponHandTest extends TestCase {

	public WeaponHandTest() {
	}

	public WeaponHandTest(String name) {
		super(name);
	}
	
	public void testHandTimeOut() throws Exception {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// --- Initialement la main est utilisable
		final WeaponHand hand = tiggy.getBody().getWeaponHand();

		assertTrue(hand.isEnabled());

		// --- Lancer un sort afin de rendre la main indisponible
		final Spell spell = tiggy.cast(PowerRune.LO, ElementRune.FUL);

		assertNotNull(spell);
		assertTrue(spell.isValid());
		assertFalse(hand.isEnabled());

		// --- Attendre suffisamment longtemps pour que la main redevienne
		// disponible
		Clock.getInstance().tick(60);

		assertTrue(hand.isEnabled());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}