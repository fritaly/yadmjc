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
import fr.ritaly.dungeonmaster.actuator.SimpleActuator;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;

public class WallSwitchTest extends TestCase {

	public WallSwitchTest() {
	}

	public WallSwitchTest(String name) {
		super(name);
	}

	public void testWallSwitchTriggeringPit() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | I | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | S |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Pit pit = new Pit(false, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(2, 1, pit);

		final WallSwitch wallSwitch = new WallSwitch(Direction.EAST);
		level1.setElement(4, 2, wallSwitch);

		wallSwitch
				.setActuator(new SimpleActuator(2, TriggerAction.TOGGLE, pit));

		// --- Situation initiale
		assertFalse(wallSwitch.isPressed());
		assertTrue(pit.isClosed());

		// --- On presse le bouton - l'oubliette doit s'ouvrir
		wallSwitch.toggle();

		// Attendre que l'oubliette s'ouvre
		Clock.getInstance().tick(2);

		assertTrue(wallSwitch.isPressed());
		assertTrue(pit.isOpen());

		// --- On represse le bouton - l'oubliette doit se fermer
		wallSwitch.toggle();

		// Attendre que l'oubliette s'ouvre
		Clock.getInstance().tick(2);

		assertFalse(wallSwitch.isPressed());
		assertFalse(pit.isOpen());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}