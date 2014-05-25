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

import java.util.Set;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.stat.Stat;

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
			if (Item.Type.getWeaponTypes().contains(type)) {
				assertTrue(type.getDamage() >= 0);
			} else {
				assertTrue(type.getDamage() == -1);
			}
		}
	}

//	public void testItemCategories() {
//		// Ensure each item belongs to a category
//		for (Item.Type type : Item.Type.values()) {
//			assertNotNull(type.getCategory());
//		}
//
//		// Check the number of items in each category
//		assertEquals(20, Item.Category.POTION.getTypes().size());
//		assertEquals(56, Item.Category.WEAPON.getTypes().size());
//		assertEquals(73, Item.Category.CLOTH.getTypes().size());
//		assertEquals(59, Item.Category.MISCELLANEOUS.getTypes().size());
//	}

	public void testFoodItemsAreConsumable() {
		final Set<Item.Type> foodTypes = Item.Type.getFoodTypes();

		// All food items can be consumed
		for (Item.Type type : Item.Type.values()) {
			if (foodTypes.contains(type)) {
				final boolean consumable = type.getCarryLocations().contains(CarryLocation.CONSUMABLE);

				assertTrue("Item type " + type + " isn't consumable", consumable);
			}
		}
	}

	public void testEffectOfItemOnChampion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// The cloak of night increases the champion's dexterity
		final Item cloak = ItemFactory.getFactory().newItem(Item.Type.CLOAK_OF_NIGHT);

		final int initialDexterity = tiggy.getStats().getDexterity().value();

		// --- The cloak has no effect when grabbed
		assertNull(tiggy.getBody().getWeaponHand().putOn(cloak));
		assertEquals(initialDexterity, tiggy.getStats().getDexterity().value());

		// --- Removing the cloak has no effect
		assertEquals(cloak, tiggy.getBody().getWeaponHand().takeOff());
		assertEquals(initialDexterity, tiggy.getStats().getDexterity()
				.value());

		// --- Putting on the cloak increases the dexterity
		assertNull(tiggy.getBody().getNeck().putOn(cloak));
		assertEquals(initialDexterity + 8, tiggy.getStats().getDexterity().value());

		// --- Taking off the cloak cancels the effect
		assertEquals(cloak, tiggy.getBody().getNeck().takeOff());
		assertEquals(initialDexterity, tiggy.getStats().getDexterity().value());
	}

	public void testInfluenceOfElvenBootsOnMaxLoad() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Elven boots increases the champion's max load
		final Item elvenBoots = ItemFactory.getFactory().newItem(Item.Type.ELVEN_BOOTS);

		final Stat maxLoadBoost = tiggy.getStats().getMaxLoadBoost();
		final float maxLoad1 = tiggy.getStats().getActualMaxLoad();
		final float maxLoad2 = tiggy.getMaxLoad();

		// --- The boost is initially zero
		assertEquals(0, maxLoadBoost.value());

		// --- When put on, the boots boost the max load by 14 points
		tiggy.getBody().getFeet().putOn(elvenBoots);

		assertEquals(+14, maxLoadBoost.value());
		assertEquals(maxLoad1 + 14, tiggy.getStats().getActualMaxLoad(), 0.00001f);
		assertEquals(maxLoad2 + 14, tiggy.getMaxLoad(), 0.00001f);

		// --- Taking off the boots cancel the boots' effect
		final Item item = tiggy.getBody().getFeet().takeOff(true);

		assertNotNull(item);
		assertEquals(elvenBoots, item);

		assertEquals(0, maxLoadBoost.value());
		assertEquals(maxLoad1, tiggy.getStats().getActualMaxLoad(), 0.00001f);
		assertEquals(maxLoad2, tiggy.getMaxLoad(), 0.00001f);
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}