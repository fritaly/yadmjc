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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;

public class StairsTest extends TestCase {
	
	private final Log log = LogFactory.getLog(this.getClass());

	public StairsTest() {
	}

	public StairsTest(String name) {
		super(name);
	}

	public void testStairs() {

		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W |S.D| P |S.U| W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Position stairsUpPosition = new Position(3, 2, 1);
		final Position stairsDownPosition = new Position(1, 2, 1);

		final Stairs stairsUp = new Stairs(Direction.EAST, true,
				stairsDownPosition);
		final Stairs stairsDown = new Stairs(Direction.WEST, false,
				stairsUpPosition);

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(1, 2, stairsDown);
		level1.setElement(3, 2, stairsUp);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// --- Vérifier la position initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.NORTH, dungeon.getParty().getLookDirection());

		// --- Pas sur la droite, le groupe est sur l'escalier mais dans la
		// mauvaise direction pour le prendre
		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(stairsUpPosition, dungeon.getParty().getPosition());

		// --- Quart de tour sur la gauche (WEST), le groupe est sur l'escalier
		// mais dans la mauvaise direction pour le prendre
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(stairsUpPosition, dungeon.getParty().getPosition());

		// --- Quart de tour sur la gauche (SOUTH), le groupe est sur l'escalier
		// mais dans la mauvaise direction pour le prendre
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(stairsUpPosition, dungeon.getParty().getPosition());

		// --- Quart de tour sur la gauche (EAST). Doit changer le groupe de
		// place car on est dans le sens de l'escalier. Sa direction est aussi
		// changée
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(stairsDownPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.EAST, dungeon.getParty().getLookDirection());

		// Réinitialisation
		log.debug("Reinit");
		dungeon.teleportParty(initialPosition, Direction.EAST, true);
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.EAST, dungeon.getParty().getLookDirection());

		// --- Pas en avant. Doit changer le groupe de place et lui faire
		// prendre l'escalier
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(stairsDownPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.EAST, dungeon.getParty().getLookDirection());

		// Réinitialisation
		log.debug("Reinit");
		dungeon.teleportParty(initialPosition, Direction.WEST, true);
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.WEST, dungeon.getParty().getLookDirection());

		// --- Pas en avant. Doit changer le groupe de place et lui faire
		// prendre l'escalier
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(stairsUpPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.WEST, dungeon.getParty().getLookDirection());

		// Réinitialisation
		log.debug("Reinit");
		dungeon.teleportParty(initialPosition, Direction.WEST, true);
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.WEST, dungeon.getParty().getLookDirection());

		// --- Pas en arrière. Doit changer le groupe de place sans lui faire
		// prendre l'escalier
		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(stairsUpPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.WEST, dungeon.getParty().getLookDirection());

		// --- Pas en arrière. Doit changer le groupe de place en lui faisant
		// prendre l'escalier
		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(stairsDownPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.EAST, dungeon.getParty().getLookDirection());

		// Réinitialisation
		log.debug("Reinit");
		dungeon.teleportParty(initialPosition, Direction.NORTH, true);
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.NORTH, dungeon.getParty().getLookDirection());

		// --- Pas sur la droite. Doit changer le groupe de place sans lui faire
		// prendre l'escalier
		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(stairsUpPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.NORTH, dungeon.getParty().getLookDirection());

		// --- Pas sur la droite. Doit changer le groupe de place en lui faisant
		// prendre l'escalier
		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(stairsDownPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.EAST, dungeon.getParty().getLookDirection());
	}
	
	public void testStairsWithNotOppositeDirections() {

		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W |S.D| P |S.U| W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Position stairsUpPosition = new Position(3, 2, 1);
		final Position stairsDownPosition = new Position(1, 2, 1);

		// Les deux escaliers ont des directions qui ne sont pas opposées afin
		// de tester que le groupe se trouve dans la bonne direction après avoir
		// pris un escalier
		final Stairs stairsUp = new Stairs(Direction.EAST, true,
				stairsDownPosition);
		final Stairs stairsDown = new Stairs(Direction.NORTH, false,
				stairsUpPosition);

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(1, 2, stairsDown);
		level1.setElement(3, 2, stairsUp);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);
		party.setDirection(Direction.EAST);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// --- Vérifier la position initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.EAST, dungeon.getParty().getLookDirection());

		// --- Pas devant, le groupe prend l'escalier
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(stairsDownPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.SOUTH, dungeon.getParty().getLookDirection());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}