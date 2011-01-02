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
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Floor;

public class FluxCageTest extends TestCase {

	public FluxCageTest(String name) {
		super(name);
	}

	public void testFluxCageMustDisappearAfterGivenTime() throws Exception {
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

		// --- Situation initiale
		assertFalse(floor.hasFluxCage());

		// Créer une cage en 1:2,1
		floor.createFluxCage();

		// --- Une cage doit être apparue
		assertTrue(floor.hasFluxCage());

		// --- Si on attend suffisamment longtemps, la cage va disparaître
		// d'elle-même
		Clock.getInstance().tick(60);

		assertFalse(floor.hasFluxCage());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}