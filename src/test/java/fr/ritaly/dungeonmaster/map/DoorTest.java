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
import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Door.Motion;
import fr.ritaly.dungeonmaster.map.Door.State;

public class DoorTest extends TestCase {

	public DoorTest() {
	}

	public DoorTest(String name) {
		super(name);
	}

	public void testBreakableDoor() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH);

		final Position doorPosition = initialPosition.towards(Direction.NORTH);

		dungeon.setElement(doorPosition, door);

		// --- La porte doit pouvoir être cassée
		assertFalse(door.isTraversable(party));
		assertFalse(door.isBroken());
		assertTrue(door.isBreakable());

		door.destroy();

		assertTrue(door.isTraversable(party));
		assertTrue(door.isBroken());
		assertTrue(door.isBreakable());

		// --- Echec la 2nde fois
		try {
			door.destroy();
			fail();
		} catch (IllegalStateException e) {
			// OK
		}
	}
	
	/**
	 * Test d'animation de porte.
	 */
	public void testDoorAnimation() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH);
		final Position doorPosition = initialPosition.towards(Direction.NORTH);

		dungeon.setElement(doorPosition, door);

		// --- La porte doit être fermée et statique initialement
		assertEquals(Motion.IDLE, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		// --- Ouvrir la porte et tester les états intermédiaires
		door.open();

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.ONE_FOURTH_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.HALF_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.THREE_FOURTH_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.IDLE, door.getMotion());
		assertEquals(State.OPEN, door.getState());

		// --- Fermer la porte et tester les états intermédiaires
		door.close();

		assertEquals(Motion.CLOSING, door.getMotion());
		assertEquals(State.OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.CLOSING, door.getMotion());
		assertEquals(State.THREE_FOURTH_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.CLOSING, door.getMotion());
		assertEquals(State.HALF_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.CLOSING, door.getMotion());
		assertEquals(State.ONE_FOURTH_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.IDLE, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		// --- Ouvrir et refermer la porte avant qu'elle ne soit complètement
		// ouverte
		door.open();

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.ONE_FOURTH_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.OPENING, door.getMotion());
		assertEquals(State.HALF_OPEN, door.getState());

		door.toggle(); // Inverser le sens de la porte

		assertEquals(Motion.CLOSING, door.getMotion());
		assertEquals(State.HALF_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.CLOSING, door.getMotion());
		assertEquals(State.ONE_FOURTH_OPEN, door.getState());

		Clock.getInstance().tick(4);

		assertEquals(Motion.IDLE, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		// --- Tester que toggle() ne change rien si porte statique
		assertEquals(Motion.IDLE, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		door.toggle();

		assertEquals(Motion.IDLE, door.getMotion());
		assertEquals(State.CLOSED, door.getState());

		// --- Tester quand la porte est traversable
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());
		assertFalse(door.isTraversable(party));

		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(initialPosition, party.getPosition());

		door.open();

		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(initialPosition, party.getPosition());
		assertFalse(door.isTraversable(party));

		Clock.getInstance().tick(4); // 1/4 ouverte

		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(initialPosition, party.getPosition());
		assertFalse(door.isTraversable(party));

		Clock.getInstance().tick(4); // 1/2 ouverte

		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(initialPosition, party.getPosition());
		assertFalse(door.isTraversable(party));

		Clock.getInstance().tick(4); // 3/4 ouverte

		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(doorPosition, party.getPosition());
		assertTrue(door.isTraversable(party));

		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));

		Clock.getInstance().tick(4);

		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(doorPosition, party.getPosition());
		assertTrue(door.isTraversable(party));
	}
	
	public void testValidateDoorConfiguration() throws ValidationException {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | W | . | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | W | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Door door = new Door(Door.Style.WOODEN, Orientation.WEST_EAST);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(2, 2, door);

		// --- Configuration invalide
		try {
			dungeon.validate();
			fail();
		} catch (ValidationException e) {
			// OK
		}

		// --- On place un mur à gauche de la porte
		level1.setElement(2, 1, new Wall());

		try {
			dungeon.validate();
			fail();
		} catch (ValidationException e) {
			// OK
		}

		// --- On place un mur à droite de la porte
		level1.setElement(2, 3, new Wall());

		dungeon.validate();
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}