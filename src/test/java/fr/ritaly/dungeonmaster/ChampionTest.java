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

import java.util.List;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Experience;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.body.Body;
import fr.ritaly.dungeonmaster.item.Bones;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.magic.AlignmentRune;
import fr.ritaly.dungeonmaster.magic.ElementRune;
import fr.ritaly.dungeonmaster.magic.FormRune;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Rune;
import fr.ritaly.dungeonmaster.magic.SkillTooLowException;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;

public class ChampionTest extends TestCase {

	public ChampionTest() {
	}

	public ChampionTest(String name) {
		super(name);
	}

	public void testSpellCasting() throws Exception {
		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getStats().getMana().baseMaxValue(200);
		tiggy.getStats().getMana().baseValue(200);

		Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(5, 5, 1), party);

		// --- Invoquer un rune du mauvais type
		try {
			tiggy.cast(ElementRune.DES);
			fail();
		} catch (IllegalStateException e) {
			// OK
		}

		try {
			tiggy.cast(FormRune.BRO);
			fail();
		} catch (IllegalStateException e) {
			// OK
		}

		try {
			tiggy.cast(AlignmentRune.NETA);
			fail();
		} catch (IllegalStateException e) {
			// OK
		}

		// --- Invoquer un power rune
		{
			final int initialMana = tiggy.getStats().getMana().value();

			final Rune rune = PowerRune.ON;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(), (int) tiggy.getStats().getMana().value());
		}

		// --- Invoquer un element rune
		{
			final int initialMana = tiggy.getStats().getMana().value();

			final Rune rune = ElementRune.ZO;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(PowerRune.ON), (int) tiggy.getStats().getMana().value());
		}

		// --- Invoquer un form rune
		{
			final int initialMana = tiggy.getStats().getMana().value();

			final Rune rune = FormRune.KATH;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(PowerRune.ON), (int) tiggy.getStats().getMana().value());
		}

		// --- Invoquer un alignment rune
		{
			final int initialMana = tiggy.getStats().getMana().value();

			final Rune rune = AlignmentRune.RA;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(PowerRune.ON), (int) tiggy.getStats().getMana().value());
		}

		// --- Lancer le sort
		final Experience experience = tiggy.getExperience(Spell.Type.ZO_KATH_RA.getSkill());
		final int initialXp = experience.getPoints();

		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		assertEquals(Spell.Type.ZO_KATH_RA, spell.getType());
		assertTrue(experience.getPoints() > initialXp);
	}

	public void testChampionDeath() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.WUUF));

		dungeon.setParty(1, 1, 1, party);

		// Equiper tiggy avec diff�rents objets (x7)
		final ItemFactory factory = ItemFactory.getFactory();

		final Item torch = factory.newItem(Item.Type.TORCH);
		final Item apple = factory.newItem(Item.Type.APPLE);
		final Item bread = factory.newItem(Item.Type.BREAD);
		final Item corn = factory.newItem(Item.Type.CORN);
		final Item dragonSteak = factory.newItem(Item.Type.DRAGON_STEAK);
		final Item flask = factory.newItem(Item.Type.EMPTY_FLASK);
		final Item ratStick = factory.newItem(Item.Type.DRUMSTICK);

		final Body body = tiggy.getBody();

		body.getShieldHand().putOn(torch);
		body.getWeaponHand().putOn(apple);
		assertTrue(tiggy.getInventory().getBackPack().add(bread) != -1);
		assertTrue(tiggy.getInventory().getBackPack().add(flask) != -1);
		assertTrue(tiggy.getInventory().getBackPack().add(ratStick) != -1);
		assertTrue(tiggy.getInventory().getPouch().add(corn) != -1);
		assertTrue(tiggy.getInventory().getQuiver().add(dragonSteak) == -1);

		// --- Il n'y a aucun objet au sol initialement
		final Element element = dungeon.getElement(1, 1, 1);

		assertNotNull(element.getItems());
		assertTrue(element.getItems().isEmpty());

		// --- Le champion est vivant, il porte des objets
		assertTrue(tiggy.isAlive());
		assertTrue(tiggy.getLoad() > 0.0f);
		assertFalse(tiggy.getItems().isEmpty());
		assertFalse(tiggy.getInventory().isEmpty());
		assertFalse(tiggy.getBody().getItems().isEmpty());

		// --- Le champion meurt, il ne doit plus rien porter
		assertTrue(tiggy.die());

		assertTrue(tiggy.isDead());
		assertEquals(0.0f, tiggy.getLoad());
		assertTrue(tiggy.getItems().isEmpty());
		assertTrue(tiggy.getInventory().isEmpty());
		assertTrue(tiggy.getBody().getItems().isEmpty());

		// --- Ses objets doivent tomber au sol
		final List<Item> items = element.getItems();

		assertNotNull(items);
		assertFalse(items.isEmpty());
		assertTrue(items.contains(torch));
		assertTrue(items.contains(apple));
		assertTrue(items.contains(bread));
		assertTrue(items.contains(flask));
		assertTrue(items.contains(ratStick));
		assertTrue(items.contains(corn));
		assertFalse(items.contains(dragonSteak));

		// --- Les objets au sol doivent aussi comporter les os du champion !
		items.remove(torch);
		items.remove(apple);
		items.remove(bread);
		items.remove(flask);
		items.remove(ratStick);
		items.remove(corn);
		items.remove(dragonSteak);

		assertEquals(1, items.size());

		final Item item = items.get(0);

		assertTrue(Item.Type.BONES.equals(item.getType()));

		final Bones bones = (Bones) item;

		assertEquals(tiggy, bones.getChampion());
	}

	public void testChampionNotSkilledEnoughToCastSpell() throws Throwable {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// --- Requiert niveau 5 de comp�tence #15
		tiggy.cast(PowerRune.LO, Spell.Type.SHIELD_POTION);

		try {
			tiggy.castSpell();
			fail();
		} catch (SkillTooLowException e) {
			// Erreur attendue
		}
	}

	public void testChampionPoisonedWhenEatingPoisonedFood() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		// Test avec pomme saine
		final Item apple = ItemFactory.getFactory().newItem(Item.Type.APPLE);

		assertFalse(tiggy.isPoisoned());
		assertNull(tiggy.consume(apple));
		assertFalse(tiggy.isPoisoned());

		// Test avec pomme empoisonn�e
		final Item poisonedApple = ItemFactory.getFactory().newItem(Item.Type.APPLE);
		poisonedApple.setPoisonStrength(PowerRune.ON);

		assertFalse(tiggy.isPoisoned());
		assertNull(tiggy.consume(poisonedApple));
		assertTrue(tiggy.isPoisoned());
	}

	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}