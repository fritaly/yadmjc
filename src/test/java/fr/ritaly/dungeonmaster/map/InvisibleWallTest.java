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
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;

public class InvisibleWallTest extends TestCase {

	public InvisibleWallTest() {
	}

	public InvisibleWallTest(String name) {
		super(name);
	}

	public void testFakeWallCanBeTraversedByChampions() {
		// +---+---+---+---+
		// | W | W | W | W |
		// +---+---+---+---+
		// | W | P | I | W |
		// +---+---+---+---+
		// | W | W | W | W |
		// +---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level = dungeon.createLevel(1, 3, 4);
		level.setElement(2, 1, new InvisibleWall());

		final Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		final Position partyPosition = new Position(1, 1, 1);

		dungeon.setParty(partyPosition, party);

		// --- Le groupe ne peut PAS se déplacer "dans" le mur invisible
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());
		assertFalse(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(partyPosition, party.getPosition());
	}
}