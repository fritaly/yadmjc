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

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.item.Item.Category;
import fr.ritaly.dungeonmaster.stat.Stat;
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
	
	public void testEffectOfItemOnChampion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Item cloak = new Cloth(Item.Type.CLOAK_OF_NIGHT);

		final int initialDexterity = tiggy.getStats().getDexterity()
				.actualValue();

		// --- Mettre la cape en main -> pas d'effet constaté
		assertNull(tiggy.getBody().getWeaponHand().putOn(cloak));
		assertEquals(initialDexterity, tiggy.getStats().getDexterity()
				.actualValue().intValue());

		// --- Retirer la cape -> pas d'effet constaté
		assertEquals(cloak, tiggy.getBody().getWeaponHand().takeOff());
		assertEquals(initialDexterity, tiggy.getStats().getDexterity()
				.actualValue().intValue());

		// --- Mettre la cape sur le dos -> effet constaté
		assertNull(tiggy.getBody().getNeck().putOn(cloak));
		assertEquals(initialDexterity + 8, tiggy.getStats().getDexterity()
				.actualValue().intValue());

		// --- Retirer la cape -> effet constaté
		assertEquals(cloak, tiggy.getBody().getNeck().takeOff());
		assertEquals(initialDexterity, tiggy.getStats().getDexterity()
				.actualValue().intValue());
	}
	
	public void testInfluenceOfElvenBootsOnMaxLoad() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Item elvenBoots = ItemFactory.getFactory().newItem(
				Item.Type.ELVEN_BOOTS);

		final Stat maxLoadBoost = tiggy.getStats().getMaxLoadBoost();
		final float maxLoad1 = tiggy.getStats().getActualMaxLoad();
		final float maxLoad2 = tiggy.getMaxLoad();

		// --- Le boost doit valoir initialement 0
		assertEquals(0, maxLoadBoost.actualValue().intValue());

		// --- Tiggy met les bottes, le boost augmente de +14
		tiggy.getBody().getFeet().putOn(elvenBoots);

		assertEquals(+14, maxLoadBoost.actualValue().intValue());
		assertEquals(maxLoad1 + 14, tiggy.getStats().getActualMaxLoad(), 0.00001f);
		assertEquals(maxLoad2 + 14, tiggy.getMaxLoad(), 0.00001f);

		// --- Tiggy retire les bottes, le boost diminue de +14
		final Item item = tiggy.getBody().getFeet().takeOff(true);

		assertNotNull(item);
		assertEquals(elvenBoots, item);
		
		assertEquals(0, maxLoadBoost.actualValue().intValue());
		assertEquals(maxLoad1, tiggy.getStats().getActualMaxLoad(), 0.00001f);
		assertEquals(maxLoad2, tiggy.getMaxLoad(), 0.00001f);
	}
}