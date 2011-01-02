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
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;

public class RetractableWallTest extends TestCase {

	public RetractableWallTest() {
	}

	public RetractableWallTest(String name) {
		super(name);
	}

	public void testPartyBlockedByRetractableWall() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | R | . | W |
		// +---+---+---+---+---+
		// | W | R | P | R | W |
		// +---+---+---+---+---+
		// | W | . | R | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final RetractableWall retractableWallNorth = new RetractableWall();
		final RetractableWall retractableWallSouth = new RetractableWall();
		final RetractableWall retractableWallWest = new RetractableWall();
		final RetractableWall retractableWallEast = new RetractableWall();

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);
		dungeon.setElement(new Position(2, 1, 1), retractableWallNorth);
		dungeon.setElement(new Position(2, 3, 1), retractableWallSouth);
		dungeon.setElement(new Position(1, 2, 1), retractableWallWest);
		dungeon.setElement(new Position(3, 2, 1), retractableWallEast);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position partyPosition = new Position(2, 2, 1);

		dungeon.setParty(partyPosition, party);

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		retractableWallNorth.open();

		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(partyPosition.towards(Direction.NORTH),
				party.getPosition());

		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		retractableWallSouth.open();

		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(partyPosition.towards(Direction.SOUTH),
				party.getPosition());

		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.LEFT, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		retractableWallWest.open();

		assertTrue(dungeon.moveParty(Move.LEFT, true, AudioClip.STEP));
		assertEquals(partyPosition.towards(Direction.WEST), party.getPosition());

		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());

		retractableWallEast.open();

		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(partyPosition.towards(Direction.EAST), party.getPosition());

		assertTrue(dungeon.moveParty(Move.LEFT, true, AudioClip.STEP));
		assertEquals(partyPosition, party.getPosition());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}