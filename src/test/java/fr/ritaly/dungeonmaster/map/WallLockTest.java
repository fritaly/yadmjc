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
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.Torch;

public class WallLockTest extends TestCase {

	public WallLockTest() {
	}

	public WallLockTest(String name) {
		super(name);
	}

	public void testActuatorTriggeredWhenUnlockingWallLock() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | L |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final TestActuator actuator = new TestActuator();

		final Level level1 = dungeon.createLevel(1, 5, 5);

		final WallLock wallLock = new WallLock(Direction.EAST,
				Item.Type.IRON_KEY);
		level1.setElement(2, 2, wallLock);

		wallLock.setActuator(actuator);

		// --- Initial state
		assertFalse(actuator.isTriggered());
		assertFalse(wallLock.isUnlocked());

		// --- Try with an invalid item type
		assertFalse(wallLock.unlock(ItemFactory.getFactory().newItem(Item.Type.KEY_OF_B)));
		Clock.getInstance().tick(1);
		assertFalse(actuator.isTriggered());
		assertFalse(wallLock.isUnlocked());

		// --- Try with a non-key item
		assertFalse(wallLock.unlock(new Torch()));
		Clock.getInstance().tick(1);
		assertFalse(actuator.isTriggered());
		assertFalse(wallLock.isUnlocked());

		// --- Try with the correct key type
		assertTrue(wallLock.unlock(ItemFactory.getFactory().newItem(Item.Type.IRON_KEY)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered());
		assertTrue(wallLock.isUnlocked());

		// --- An already used lock can't be reused
		assertFalse(wallLock.unlock(ItemFactory.getFactory().newItem(Item.Type.IRON_KEY)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered());
		assertTrue(wallLock.isUnlocked());
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}