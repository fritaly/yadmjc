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
import junit.framework.TestCase;

public class ChestTest extends TestCase {

	public ChestTest() {
	}

	public ChestTest(String name) {
		super(name);
	}
	
	public void testChestCapacity() {
		final Chest chest = new Chest();
		
		assertEquals(8, chest.getCapacity());
	}
	
	public void testChestIsFull() {
		final Chest chest = new Chest();
		
		// Remplir le coffre
		for (int i = 0; i < 8; i++) {
			assertFalse(chest.isFull());			
			assertTrue(chest.add(new Torch()) >= 0);
			assertEquals((i == 7), chest.isFull()); // <-- Rempli à 8ème itér.
		}
		
		// Vider le coffre
		for (int i = 0; i < 8; i++) {
			assertEquals((i == 0), chest.isFull()); // <-- Rempli à 1ère itér.			
			assertNotNull(chest.remove(i));
			assertFalse(chest.isFull());
		}
	}
	
	public void testChestIsEmpty() {
		final Chest chest = new Chest();
		
		// Remplir le coffre
		for (int i = 0; i < 8; i++) {
			assertEquals((i == 0), chest.isEmpty()); // <-- Vide à 1ère itér.
			assertTrue(chest.add(new Torch()) >= 0);
			assertFalse(chest.isEmpty());			
		}
		
		// Vider le coffre
		for (int i = 0; i < 8; i++) {
			assertFalse(chest.isEmpty());			
			assertNotNull(chest.remove(i));
			assertEquals((i == 7), chest.isEmpty()); // <-- Vide à 8ème itér.
		}
	}
	
	public void testGetItemCount() {
		final Chest chest = new Chest();

		// --- Remplir le coffre
		for (int i = 0; i < 8; i++) {
			assertEquals(i, chest.getItemCount());
			assertTrue(chest.add(new Torch()) >= 0);
			assertEquals(i + 1, chest.getItemCount());
		}
	}

	public void testMiscItemsNotFittingInChest() {
		final Chest chest = new Chest();

		// --- Tests avec des objets qui ne peuvent pas tenir dans un coffre
		assertEquals(-1, chest.add(new Chest()));
		assertEquals(-1, chest.add(new Weapon(Item.Type.STAFF_OF_CLAWS)));
		assertEquals(-1, chest.add(new Weapon(Item.Type.FALCHION)));
	}
	
	public void testMiscItemsFittingInChest() {
		final Chest chest = new Chest();

		// --- Tests avec des objets qui peuvent tenir dans un coffre
		assertTrue(chest.add(new Potion(Item.Type.HEALTH_POTION)) >= 0);
		assertTrue(chest.add(new Torch()) >= 0);
		assertTrue(chest.add(new Weapon(Item.Type.EYE_OF_TIME)) >= 0);
		assertTrue(chest.add(new Weapon(Item.Type.DAGGER)) >= 0);
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}