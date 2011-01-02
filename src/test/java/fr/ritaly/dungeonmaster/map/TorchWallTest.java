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
import fr.ritaly.dungeonmaster.actuator.TestActuator;
import fr.ritaly.dungeonmaster.item.Torch;

public class TorchWallTest extends TestCase {

	public TorchWallTest() {
	}

	public TorchWallTest(String name) {
		super(name);
	}

	public void testActuatorTriggeredWhenTorchPickedUp() {
		final TestActuator actuator = new TestActuator();

		final TorchWall torchWall = new TorchWall(Direction.NORTH, true);
		torchWall.addActuator(actuator);

		// --- Situation initiale
		assertFalse(actuator.isTriggered());

		// --- Prendre la torche
		assertNotNull(torchWall.takeTorch());
		Clock.getInstance().tick();
		assertTrue(actuator.isTriggered());
	}
	
	public void testActuatorTriggeredWhenTorchInstalled() {
		final TestActuator actuator = new TestActuator();

		final TorchWall torchWall = new TorchWall(Direction.NORTH, false);
		torchWall.addActuator(actuator);

		// --- Situation initiale
		assertFalse(actuator.isTriggered());

		// --- Poser une torche
		torchWall.putTorch(new Torch());
		Clock.getInstance().tick();
		assertTrue(actuator.isTriggered());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}