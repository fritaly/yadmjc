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
import fr.ritaly.dungeonmaster.DirectionTransform;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.LoopingActuator;
import fr.ritaly.dungeonmaster.actuator.SimpleActuator;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;

public class TeleporterTest extends TestCase {

	public TeleporterTest() {
	}

	public TeleporterTest(String name) {
		super(name);
	}

	public void testTeleporter() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | T | W |
		// +---+---+---+---+---+
		// | W | . | . | D | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Position destination = new Position(3, 3, 1);
		final Teleporter teleporter = new Teleporter(destination,
				DirectionTransform.OPPOSITE, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, teleporter);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// --- Vérifier la position initiale
		assertEquals(initialPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.NORTH, party.getLookDirection());

		// --- Marcher dans le téléporteur
		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(destination, dungeon.getParty().getPosition());
		assertEquals(Direction.SOUTH, party.getLookDirection());
	}
	
	public void testTeleporterTriggering() throws Throwable {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | T | W |
		// +---+---+---+---+---+
		// | W | . | . | D | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Position destination = new Position(3, 3, 1);
		final Teleporter teleporter = new Teleporter(destination,
				DirectionTransform.OPPOSITE, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, teleporter);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		final SimpleActuator actuator1 = new SimpleActuator(6,
				TriggerAction.TOGGLE, teleporter);

		final LoopingActuator actuator = new LoopingActuator(actuator1);

		Clock.getInstance().register(teleporter);
		Clock.getInstance().register(actuator);

		// --- Téléporteur actif
		assertTrue(teleporter.isEnabled());

		Clock.getInstance().tick(6);

		// --- Téléporteur inactif
		assertFalse(teleporter.isEnabled());

		Clock.getInstance().tick(6);

		// --- Téléporteur actif
		assertTrue(teleporter.isEnabled());
	}
	
	public void testTeleporterTriggeringWithAsymetricPeriods() throws Throwable {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | T | W |
		// +---+---+---+---+---+
		// | W | . | . | D | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Position destination = new Position(3, 3, 1);
		final Teleporter teleporter = new Teleporter(destination,
				DirectionTransform.OPPOSITE, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, teleporter);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		final Actuator actuator1 = new SimpleActuator(6, TriggerAction.TOGGLE,
				teleporter);
		final Actuator actuator2 = new SimpleActuator(10, TriggerAction.TOGGLE,
				teleporter);
		final Actuator actuator = new LoopingActuator(actuator1, actuator2);

		Clock.getInstance().register(teleporter);
		Clock.getInstance().register(actuator);

		// --- Téléporteur actif
		assertTrue(teleporter.isEnabled());

		Clock.getInstance().tick(5);

		assertTrue(teleporter.isEnabled());

		Clock.getInstance().tick();

		// --- Téléporteur inactif
		assertFalse(teleporter.isEnabled());

		Clock.getInstance().tick(9);

		assertFalse(teleporter.isEnabled());

		Clock.getInstance().tick();

		// --- Téléporteur actif
		assertTrue(teleporter.isEnabled());
	}
	
	public void testTeleporterWithNoDestination() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | T | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Teleporter teleporter = new Teleporter(
				DirectionTransform.OPPOSITE, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, teleporter);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// --- Vérifier la position initiale
		assertEquals(initialPosition, dungeon.getParty().getPosition());
		assertEquals(Direction.NORTH, party.getLookDirection());

		// --- Marcher dans le téléporteur
		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		assertEquals(new Position(3, 2, 1), dungeon.getParty().getPosition());
		assertEquals(Direction.SOUTH, party.getLookDirection());
	}
}