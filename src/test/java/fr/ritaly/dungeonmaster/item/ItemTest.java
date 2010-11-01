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

import java.util.EnumSet;

import fr.ritaly.dungeonmaster.item.Item.Category;
import junit.framework.TestCase;

public class ItemTest extends TestCase {

	public ItemTest() {
	}

	public ItemTest(String name) {
		super(name);
	}

	public void testAllItemsCanBeInstanciated() {
		for (Item.Type type : Item.Type.values()) {
			assertNotNull(ItemFactory.getFactory().newItem(type));
		}
	}

	public void testAllWeaponItemsHaveDamagePointsDefined() {
		for (Item.Type type : Item.Type.values()) {
			if (type.getCategory().equals(Category.WEAPON)) {
				assertTrue(type.getDamage() >= 0);
			} else {
				assertTrue(type.getDamage() == -1);
			}
		}
	}

	public void testItemCategories() {
		// Tester que chaque type est bien catégorisé
		for (Item.Type type : Item.Type.values()) {
			assertNotNull(type.getCategory());
		}

		// Tester le nombre de type dans chaque catégorie
		assertEquals(1, Item.Category.SCROLL.getTypes().size());
		assertEquals(1, Item.Category.CONTAINER.getTypes().size());
		assertEquals(21, Item.Category.POTION.getTypes().size());
		assertEquals(56, Item.Category.WEAPON.getTypes().size());
		assertEquals(73, Item.Category.CLOTH.getTypes().size());
		assertEquals(56, Item.Category.MISCELLANEOUS.getTypes().size());
	}

	public void testFoodItemsAreConsumable() {
		final EnumSet<Item.Type> foodItems = Item.Category.getFoodItems();

		// Tous les objets de type nourriture doivent être consommables
		for (Item.Type type : Item.Type.values()) {
			if (foodItems.contains(type)) {
				final boolean consumable = type.getCarryLocations()
						.getLocations().contains(CarryLocation.CONSUMABLE);

				assertTrue("Item type " + type + " isn't consumable",
						consumable);
			}
		}
	}
}