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
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Floor;

public class PoisonCloudTest extends TestCase {

	public PoisonCloudTest(String name) {
		super(name);
	}

	public void testPoisonCloudMustDisappearAfterGivenTime() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Floor floor = (Floor) dungeon.getElement(2, 1, 1);

		// --- No poison could initially
		assertFalse(floor.hasPoisonClouds());

		// Create a poison cloud in 1:2,1
		floor.createPoisonCloud();

		// --- There must be a poison cloud
		assertTrue(floor.hasPoisonClouds());
		assertEquals(1, floor.getPoisonCloudCount());

		// --- Wait long enough to let the poison cloud disappear
		Clock.getInstance().tick(60);

		assertFalse(floor.hasPoisonClouds());
	}

	public void testPoisonCloudMustAttackChampionsWhenInsideIt() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getStats().getHealth().maxValue(500);
		tiggy.getStats().getHealth().value(500);

		final int health = tiggy.getStats().getHealth().value();

		final Party party = new Party(tiggy);

		dungeon.setParty(new Position(2, 1, 1), party);

		final Floor floor = (Floor) dungeon.getElement(2, 1, 1);

		// --- No poison cloud initially
		assertFalse(floor.hasPoisonClouds());

		// Create the poison cloud
		floor.createPoisonCloud();

		// --- There must be a poison cloud
		assertTrue(floor.hasPoisonClouds());
		assertEquals(1, floor.getPoisonCloudCount());

		// --- Wait long enough to let the poison cloud disappear
		Clock.getInstance().tick(60);

		assertFalse(floor.hasPoisonClouds());

		// --- The champion's health must be lesser than initially
		assertTrue(health > tiggy.getStats().getHealth().value().intValue());
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}