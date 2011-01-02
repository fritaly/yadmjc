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

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Level;
import fr.ritaly.dungeonmaster.map.Pit;
import junit.framework.TestCase;

public class RopeTest extends TestCase {

	public RopeTest() {
	}

	public RopeTest(String name) {
		super(name);
	}

	public void testClimbDownThroughRealOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, new Pit());

		dungeon.createLevel(2, 5, 5);

		final MiscItem rope = new MiscItem(Item.Type.ROPE);
		
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(rope);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		
		// --- Le groupe ne peut descendre que s'il fait face à l'oubliette !
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.WEST, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		// --- Le groupe descend à travers l'oubliette à l'aide de la corde
		final int health = tiggy.getStats().getHealth().value();
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.EAST, party.getLookDirection());
		assertTrue(rope.perform(Action.CLIMB_DOWN));
		
		assertEquals(new Position(3, 2, 2), dungeon.getParty().getPosition());
		
		// La santé du champion ne doit pas avoir diminué pour bien différencier
		// du cas de la chute !!
		assertEquals(health, tiggy.getStats().getHealth().value().intValue());
	}
	
	public void testCantClimbDownThroughFakeOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, new Pit(true, true));

		dungeon.createLevel(2, 5, 5);

		final MiscItem rope = new MiscItem(Item.Type.ROPE);
		
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(rope);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		
		// --- Le groupe ne peut pas descendre à travers une fausse oubliette !
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.WEST, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.EAST, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
	}
	
	public void testCantClimbDownThroughRealClosedPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, new Pit(false, false));

		dungeon.createLevel(2, 5, 5);

		final MiscItem rope = new MiscItem(Item.Type.ROPE);
		
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(rope);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		
		// --- Le groupe ne peut pas descendre à travers une oubliette fermée !
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.WEST, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.EAST, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));
		
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
	}
	
	public void testClimbDownThroughSeveralStackedRealOpenPits() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		
		// Level3:
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

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, new Pit());

		final Level level2 = dungeon.createLevel(2, 5, 5);
		level2.setElement(3, 2, new Pit());
		
		dungeon.createLevel(3, 5, 5);

		final MiscItem rope = new MiscItem(Item.Type.ROPE);
		
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(rope);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		
		// --- Le groupe descend à travers l'oubliette à l'aide de la corde
		final int health = tiggy.getStats().getHealth().value();
		
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));
		assertEquals(Direction.EAST, party.getLookDirection());
		assertTrue(rope.perform(Action.CLIMB_DOWN));
		
		// Le groupe atterrit au troisième niveau
		assertEquals(new Position(3, 2, 3), dungeon.getParty().getPosition());
		
		// La santé du champion ne doit pas avoir diminué pour bien différencier
		// du cas de la chute !!
		assertEquals(health, tiggy.getStats().getHealth().value().intValue());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}