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

import junit.framework.TestCase;

public class ChestTest extends TestCase {

	public ChestTest() {
	}

	public ChestTest(String name) {
		super(name);
	}

	public void testGetCapacity() {
		final Chest chest = new Chest();

		assertEquals("The capacity of a chest must be 8", 8, chest.getCapacity());
	}

	public void testIsFull() {
		final Chest chest = new Chest();

		// Fill the chest
		for (int i = 1; i <= 8; i++) {
			assertFalse(chest.isFull());
			assertTrue(chest.add(new Torch()) >= 0);
			assertEquals((i == 8), chest.isFull()); // <-- Full on the 8th item
		}

		// Empty the chest
		for (int i = 8; i >= 1; i--) {
			assertEquals((i == 8), chest.isFull());
			assertNotNull(chest.remove(i - 1));
			assertFalse(chest.isFull());
		}
	}

	public void testIsEmpty() {
		final Chest chest = new Chest();

		// Fill the chest
		for (int i = 1; i <= 8; i++) {
			assertEquals((i == 1), chest.isEmpty());
			assertTrue(chest.add(new Torch()) >= 0);
			assertFalse(chest.isEmpty());
		}

		// Empty the chest
		for (int i = 8; i >= 8; i--) {
			assertFalse(chest.isEmpty());
			assertNotNull(chest.remove(i - 1));
			assertEquals((i == 1), chest.isEmpty());
		}
	}

	public void testGetItemCount() {
		final Chest chest = new Chest();

		for (int i = 0; i < 8; i++) {
			assertEquals(i, chest.getItemCount());
			assertTrue(chest.add(new Torch()) >= 0);
			assertEquals(i + 1, chest.getItemCount());
		}
	}

	public void testChestCannotContainSomeItems() {
		final Chest chest = new Chest();

		// The following items can't fit into a chest
		assertEquals(-1, chest.add(new Chest()));
		assertEquals(-1, chest.add(ItemFactory.getFactory().newItem(Item.Type.STAFF_OF_CLAWS)));
		assertEquals(-1, chest.add(ItemFactory.getFactory().newItem(Item.Type.FALCHION)));
	}

	public void testAdd() {
		final Chest chest = new Chest();

		// The following items can fit in a chest
		assertTrue(chest.add(new Potion(Item.Type.HEALTH_POTION)) >= 0);
		assertTrue(chest.add(new Torch()) >= 0);
		assertTrue(chest.add(ItemFactory.getFactory().newItem(Item.Type.EYE_OF_TIME)) >= 0);
		assertTrue(chest.add(ItemFactory.getFactory().newItem(Item.Type.DAGGER)) >= 0);
	}

	public void testAdd_Null() {
		final Chest chest = new Chest();

		try {
			chest.add(null);

			fail("Can't add a null to a chest");
		} catch (IllegalArgumentException e) {
			// Expected error
		}
	}

	public void testAdd_SameItemTwice() {
		final Chest chest = new Chest();
		final Torch torch = new Torch();

		assertTrue(chest.add(torch) >= 0);
		assertFalse("Adding an item twice to a chest should fail", chest.add(torch) >= 0);
	}

	public void testContains() {
		final Chest chest = new Chest();
		final Torch torch = new Torch();

		assertFalse(chest.contains(torch));
		assertTrue(chest.add(torch) >= 0);
		assertTrue(chest.contains(torch));
		assertTrue(chest.remove(torch));
		assertFalse(chest.contains(torch));
	}

	public void testGetRandom() {
		final Chest chest = new Chest();
		final Torch torch = new Torch();

		assertNull(chest.getRandom());

		assertTrue(chest.add(torch) >= 0);
		assertEquals(torch, chest.getRandom());

	}
}