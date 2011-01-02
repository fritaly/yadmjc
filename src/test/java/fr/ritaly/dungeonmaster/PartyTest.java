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

public class PartyTest extends TestCase {

	public PartyTest(String name) {
		super(name);
	}

	public void testAutomaticLeaderSelectionWhenLeaderDies() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);

		final Party party = new Party();

		// --- Vérifier l'état initial
		assertNull(party.getLeader());

		// --- Test quand le leader meurt
		party.addChampion(tiggy);
		party.addChampion(wuuf);

		assertEquals(tiggy, party.getLeader());
		assertTrue(tiggy.die());
		assertEquals(wuuf, party.getLeader());
	}
	
	public void testAutomaticLeaderSelectionWhenRemovingLeader() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);

		final Party party = new Party();

		// --- Vérifier l'état initial
		assertNull(party.getLeader());

		// --- Ajouter un champion au groupe
		party.addChampion(tiggy);

		assertEquals(tiggy, party.getLeader());

		// --- Ajouter un autre champion au groupe
		party.addChampion(wuuf);

		assertEquals(tiggy, party.getLeader());

		// --- Supprimer tiggy du groupe, wuuf devient leader
		party.removeChampion(tiggy);

		assertEquals(wuuf, party.getLeader());

		// --- Supprimer wuuf
		party.removeChampion(wuuf);

		assertNull(party.getLeader());
	}
	
	public void testChampionAddedToParty() {
		Party party = new Party();

		// --- Ajouter Tiggy
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Ajouter deux fois le même champion doit échouer
		try {
			party.addChampion(tiggy);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Ajouter daroou
		Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);

		party.addChampion(daroou);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(2, party.getSize(true));
		assertEquals(2, party.getSize(false));

		// --- Ajouter chani
		Champion chani = ChampionFactory.getFactory().newChampion(Name.CHANI);

		party.addChampion(chani);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(3, party.getSize(true));
		assertEquals(3, party.getSize(false));

		// --- Ajouter wuuf
		Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		party.addChampion(wuuf);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertTrue(party.isFull());
		assertEquals(4, party.getSize(true));
		assertEquals(4, party.getSize(false));

		// --- Ajouter un 5ème champion doit échouer
		try {
			party.addChampion(ChampionFactory.getFactory().newChampion(
					Name.ALEX));
			fail();
		} catch (IllegalStateException e) {
			// OK
		}
	}
	
	public void testChampionRemovedFromParty() {
		Party party = new Party();

		// --- Ajouter Tiggy
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Retirer un champion inconnu doit échouer
		try {
			party.removeChampion(ChampionFactory.getFactory().newChampion(
					Name.ALEX));
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Ajouter daroou, chani & wuuf
		Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);
		Champion chani = ChampionFactory.getFactory().newChampion(Name.CHANI);
		Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		party.addChampion(daroou);
		party.addChampion(chani);
		party.addChampion(wuuf);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertTrue(party.isFull());
		assertEquals(4, party.getSize(true));
		assertEquals(4, party.getSize(false));

		// --- Retirer daroou
		final Location location1 = party.removeChampion(daroou);

		assertNotNull(location1);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(3, party.getSize(true));
		assertEquals(3, party.getSize(false));

		// --- Retirer wuuf
		final Location location2 = party.removeChampion(wuuf);

		assertNotNull(location2);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(2, party.getSize(true));
		assertEquals(2, party.getSize(false));

		// --- Retirer tiggy
		final Location location3 = party.removeChampion(tiggy);

		assertNotNull(location3);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Retirer chani
		final Location location4 = party.removeChampion(chani);

		assertNotNull(location4);

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(0, party.getSize(true));
		assertEquals(0, party.getSize(false));
	}
	
	public void testPartyEmptiness() {
		Party party = new Party();

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}