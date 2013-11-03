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
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		final Party party = new Party();

		// The party has no leader initially
		assertNull(party.getLeader());

		// Add 2 champions
		party.addChampion(tiggy);
		party.addChampion(wuuf);

		// The leader is the first champion added
		assertEquals(tiggy, party.getLeader());

		// When the leader dies, the remaining champion is promoted leader
		assertTrue(tiggy.die());
		assertEquals(wuuf, party.getLeader());
	}

	public void testAutomaticLeaderSelectionWhenRemovingLeader() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		final Party party = new Party();

		// The party has no leader initially
		assertNull(party.getLeader());

		// Add one champion to the party
		party.addChampion(tiggy);

		// The leader is the first champion added
		assertEquals(tiggy, party.getLeader());

		// Add another champion
		party.addChampion(wuuf);

		// The leader is still the first champion added
		assertEquals(tiggy, party.getLeader());

		// Remove the first champion, the second becomes leader
		party.removeChampion(tiggy);

		assertEquals(wuuf, party.getLeader());

		// Remove the last champion, there's no more leader left
		party.removeChampion(wuuf);

		assertNull(party.getLeader());
	}

	public void testChampionAddedToParty() {
		final Party party = new Party();

		// Create a champion and add it to the party
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		try {
			// Adding the same champion twice must fail
			party.addChampion(tiggy);

			fail("Adding the same champion twice should fail");
		} catch (IllegalArgumentException e) {
			// OK
		}

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// Add another champion
		final Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);

		party.addChampion(daroou);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(2, party.getSize(true));
		assertEquals(2, party.getSize(false));

		// Add a third champion
		final Champion chani = ChampionFactory.getFactory().newChampion(Name.CHANI);

		party.addChampion(chani);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(3, party.getSize(true));
		assertEquals(3, party.getSize(false));

		// Add a fourth champion
		final Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		party.addChampion(wuuf);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertTrue(party.isFull());
		assertEquals(4, party.getSize(true));
		assertEquals(4, party.getSize(false));

		try {
			// Adding a fifth champion should fail
			party.addChampion(ChampionFactory.getFactory().newChampion(Name.ALEX));

			fail("Adding more than 4 champions to a party should fail");
		} catch (IllegalStateException e) {
			// OK
		}
	}

	public void testChampionRemovedFromParty() {
		final Party party = new Party();

		// Add a champion
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		try {
			party.removeChampion(ChampionFactory.getFactory().newChampion(Name.ALEX));

			fail("Removing an unknown champion from a party should fail");
		} catch (IllegalArgumentException e) {
			// OK
		}

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Add 3 other champions
		final Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);
		final Champion chani = ChampionFactory.getFactory().newChampion(Name.CHANI);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		party.addChampion(daroou);
		party.addChampion(chani);
		party.addChampion(wuuf);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertTrue(party.isFull());
		assertEquals(4, party.getSize(true));
		assertEquals(4, party.getSize(false));

		// Remove a champion
		final Location location1 = party.removeChampion(daroou);

		assertNotNull(location1);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(3, party.getSize(true));
		assertEquals(3, party.getSize(false));

		// Remove another champion
		final Location location2 = party.removeChampion(wuuf);

		assertNotNull(location2);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(2, party.getSize(true));
		assertEquals(2, party.getSize(false));

		// Remove another champion
		final Location location3 = party.removeChampion(tiggy);

		assertNotNull(location3);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// Remove the last champion
		final Location location4 = party.removeChampion(chani);

		assertNotNull(location4);

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(0, party.getSize(true));
		assertEquals(0, party.getSize(false));
	}

	public void testPartyEmptiness() {
		final Party party = new Party();

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));
	}

	// --- addChampion(Champion) --- //

	public void testAddChampion_Null() {
		final Party party = new Party();

		try {
			party.addChampion(null);

			fail("Adding a null champion should fail");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	public void testAddChampion_NewChampion() {
		final Party party = new Party();
		final Location location = party.addChampion(ChampionFactory.getFactory().newChampion(Champion.Name.ALEX));

		assertNotNull(location);
	}

	public void testAddChampion_ExistingChampion() {
		final Party party = new Party();
		final Champion champion = ChampionFactory.getFactory().newChampion(Champion.Name.ALEX);
		final Location location = party.addChampion(champion);

		assertNotNull(location);

		try {
			party.addChampion(champion);

			fail("Adding an existing champion should fail");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	public void testAddChampion_PartyFull() {
		final Party party = new Party();
		final ChampionFactory factory = ChampionFactory.getFactory();
		party.addChampion(factory.newChampion(Champion.Name.ALEX));
		party.addChampion(factory.newChampion(Champion.Name.AZIZI));
		party.addChampion(factory.newChampion(Champion.Name.BORIS));
		party.addChampion(factory.newChampion(Champion.Name.CHANI));

		assertEquals(4, party.getSize(true));

		try {
			party.addChampion(factory.newChampion(Champion.Name.ZED));

			fail("Adding a champion to a full party should fail");
		} catch (IllegalStateException e) {
			// OK
		}
	}

	// --- allChampionsDead() --- //

	public void testAllChampionsDead() {
		final ChampionFactory factory = ChampionFactory.getFactory();

		final Party party = new Party();
		assertFalse(party.allChampionsDead());

		final Champion alex = factory.newChampion(Champion.Name.ALEX);
		final Champion azizi = factory.newChampion(Champion.Name.AZIZI);
		final Champion boris = factory.newChampion(Champion.Name.BORIS);
		final Champion chani = factory.newChampion(Champion.Name.CHANI);

		party.addChampion(alex);
		assertFalse(party.allChampionsDead());

		party.addChampion(azizi);
		assertFalse(party.allChampionsDead());

		party.addChampion(boris);
		assertFalse(party.allChampionsDead());

		party.addChampion(chani);
		assertFalse(party.allChampionsDead());

		alex.die();
		assertFalse(party.allChampionsDead());

		azizi.die();
		assertFalse(party.allChampionsDead());

		boris.die();
		assertFalse(party.allChampionsDead());

		chani.die();
		assertTrue(party.allChampionsDead()); // <---
	}

	// --- awake() --- //

	public void testAwake() {
		final Party party = new Party();

		assertFalse(party.isSleeping());
		party.sleep();
		assertTrue(party.isSleeping());
		party.awake();
		assertFalse(party.isSleeping());
	}

	// --- //

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}