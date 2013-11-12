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

public class WallSlotTest extends TestCase {

	public WallSlotTest() {
	}

	public WallSlotTest(String name) {
		super(name);
	}

	public void testActuatorTriggeredWhenUsingMultiWallSlot() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | S |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+


		final TestActuator actuator = new TestActuator();

		final WallSlot wallSlot = new WallSlot(Direction.EAST,
				Item.Type.GOLD_COIN, 2);
		wallSlot.setActuator(actuator);

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(2, 2, wallSlot);

		// --- Initial state
		assertFalse(actuator.isTriggered());
		assertFalse(wallSlot.isUsed());

		// --- Try with an invalid item type
		assertFalse(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.KEY_OF_B)));
		Clock.getInstance().tick(1);
		assertFalse(actuator.isTriggered());
		assertFalse(wallSlot.isUsed());

		// --- Try with the proper item type (first try)
		assertTrue(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.GOLD_COIN)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered()); // <-- Triggering
		assertFalse(wallSlot.isUsed()); // <-- Not yet used

		// --- Try with the proper item type (second try)
		assertTrue(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.GOLD_COIN)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered()); // <-- Triggering again
		assertTrue(wallSlot.isUsed()); // <-- Used

		// --- The slot has been consumed and can't be used again
		assertFalse(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.GOLD_COIN)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered());
		assertTrue(wallSlot.isUsed());
	}

	public void testActuatorTriggeredWhenUsingWallSlot() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | S |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final TestActuator actuator = new TestActuator();

		final Level level1 = dungeon.createLevel(1, 5, 5);

		final WallSlot wallSlot = new WallSlot(Direction.EAST,
				Item.Type.GOLD_COIN);
		level1.setElement(2, 2, wallSlot);

		wallSlot.setActuator(actuator);

		// --- Initial state
		assertFalse(actuator.isTriggered());
		assertFalse(wallSlot.isUsed());

		// --- Try with an invalid item type
		assertFalse(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.KEY_OF_B)));
		Clock.getInstance().tick(1);
		assertFalse(actuator.isTriggered());
		assertFalse(wallSlot.isUsed());

		// --- Try with the proper item type (first try)
		assertTrue(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.GOLD_COIN)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered());
		assertTrue(wallSlot.isUsed());

		// --- The slot has been consumed and can't be used again
		assertFalse(wallSlot.unlock(ItemFactory.getFactory().newItem(Item.Type.GOLD_COIN)));
		Clock.getInstance().tick(1);
		assertTrue(actuator.isTriggered());
		assertTrue(wallSlot.isUsed());
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}