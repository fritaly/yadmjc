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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.actuator.SimpleActuator;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Experience;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.body.Body;
import fr.ritaly.dungeonmaster.champion.body.WeaponHand;
import fr.ritaly.dungeonmaster.item.Bones;
import fr.ritaly.dungeonmaster.item.Chest;
import fr.ritaly.dungeonmaster.item.Cloth;
import fr.ritaly.dungeonmaster.item.EmptyFlask;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.Item.Category;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.MiscItem;
import fr.ritaly.dungeonmaster.item.Potion;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.item.Weapon;
import fr.ritaly.dungeonmaster.item.drink.WaterFlask;
import fr.ritaly.dungeonmaster.item.drink.WaterSkin;
import fr.ritaly.dungeonmaster.magic.AlignmentRune;
import fr.ritaly.dungeonmaster.magic.ChampionMumblesNonsenseException;
import fr.ritaly.dungeonmaster.magic.ElementRune;
import fr.ritaly.dungeonmaster.magic.EmptyFlaskNeededException;
import fr.ritaly.dungeonmaster.magic.EmptyHandNeededException;
import fr.ritaly.dungeonmaster.magic.FormRune;
import fr.ritaly.dungeonmaster.magic.NotEnoughManaException;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Rune;
import fr.ritaly.dungeonmaster.magic.SkillTooLowException;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Altar;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;
import fr.ritaly.dungeonmaster.map.Lever;
import fr.ritaly.dungeonmaster.map.Pit;
import fr.ritaly.dungeonmaster.map.RetractableWall;
import fr.ritaly.dungeonmaster.map.ValidationException;
import fr.ritaly.dungeonmaster.map.Wall;
import fr.ritaly.dungeonmaster.map.WallSwitch;
import fr.ritaly.dungeonmaster.stat.IntStat;

public class DungeonMasterTest extends TestCase {

	private final Log log = LogFactory.getLog(this.getClass());

	public DungeonMasterTest() {
	}

	public DungeonMasterTest(String name) {
		super(name);
	}

	public void testValidateSimpleDungeon() throws ValidationException {
		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		// --- Le donjon créé doit être valide
		dungeon.validate();
	}

	/**
	 * Test de création d'un donjon simple.
	 */
	public void testSimpleDungeon() {
		Dungeon dungeon = new Dungeon();

		Level level1 = dungeon.createLevel(1, 10, 10);
		assertNotNull(level1);
		assertEquals(10, level1.getWidth());
		assertEquals(10, level1.getHeight());

		// Le niveau doit être entouré de murs
		for (int x = 0; x < level1.getWidth(); x++) {
			final Element element1 = level1.getElement(x, 0);
			final Element element2 = level1.getElement(x,
					level1.getHeight() - 1);

			assertNotNull(element1);
			assertEquals(Element.Type.WALL, element1.getType());

			assertNotNull(element2);
			assertEquals(Element.Type.WALL, element2.getType());
		}

		// Le niveau doit être rempli de dalles de sol
		for (int x = 1; x < level1.getWidth() - 1; x++) {
			for (int y = 1; y < level1.getHeight() - 1; y++) {
				final Element element = level1.getElement(x, y);

				assertNotNull(element);
				assertEquals(Element.Type.FLOOR, element.getType());
			}
		}
	}

	/**
	 * Test de positionnement d'un groupe de champions dans un donjon.
	 */
	public void testPartyInstalledInDungeon() {
		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);

		// ---
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));

		try {
			// Ca doit planter la seconde fois
			party.addChampion(tiggy);

			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		// ---
		assertNull(dungeon.getParty());
		assertNull(party.getPosition());

		final Position partyPosition = new Position(5, 5, 1);

		dungeon.setParty(partyPosition, party);

		assertNotNull(dungeon.getParty());
		assertNotNull(party.getPosition());
		assertEquals(partyPosition, party.getPosition());
	}

	public void testSpellProjectile() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | P | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(5, 5, 1), party);

		tiggy.cast(PowerRune.LO, Spell.Type.FIREBALL);
		tiggy.castSpell();

		assertTrue(dungeon.getElement(5, 5, 1).hasProjectiles());

		// Changement de position
		Clock.getInstance().tick(3);

		assertFalse(dungeon.getElement(5, 5, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 4, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 4, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 3, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 3, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 2, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 2, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile va exploser
		Clock.getInstance().tick(6);

		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile explose
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 1, 1).hasProjectiles());
	}

	public void testSpellProjectileExplodingInDoor() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | W | D | W | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | P | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);
		dungeon.setElement(4, 1, 1, new Wall());
		dungeon.setElement(5, 1, 1, new Door(Door.Style.WOODEN,
				Orientation.NORTH_SOUTH));
		dungeon.setElement(6, 1, 1, new Wall());

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(5, 5, 1), party);

		tiggy.cast(PowerRune.LO, Spell.Type.FIREBALL);
		tiggy.castSpell();

		assertTrue(dungeon.getElement(5, 5, 1).hasProjectiles());

		// Changement de position
		Clock.getInstance().tick(3);

		assertFalse(dungeon.getElement(5, 5, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 4, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 4, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 3, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 3, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 2, 1).hasProjectiles());

		// 2 case à traverser puis changement de position
		Clock.getInstance().tick(6);

		assertFalse(dungeon.getElement(5, 2, 1).hasProjectiles());
		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile explose dans la porte
		Clock.getInstance().tick(6);

		assertTrue(dungeon.getElement(5, 1, 1).hasProjectiles());

		// Le projectile explose
		Clock.getInstance().tick(6);

		// FIXME Le projectile doit détruire la porte
		assertFalse(dungeon.getElement(5, 1, 1).hasProjectiles());
	}

	/**
	 * Test de déplacement d'un groupe dans le donjon.
	 */
	public void testPartyMoved() {
		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		final Position initialPosition = new Position(5, 5, 1);

		dungeon.setParty(initialPosition, party);

		// --- Situation initiale
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		// --- Pas en avant
		assertTrue(dungeon.moveParty(Move.FORWARD, true));

		assertEquals(initialPosition.towards(Direction.NORTH),
				party.getPosition());

		// --- Pas en arrière
		assertTrue(dungeon.moveParty(Move.BACKWARD, true));

		assertEquals(initialPosition, party.getPosition());

		// --- Pas à gauche
		assertTrue(dungeon.moveParty(Move.LEFT, true));

		assertEquals(initialPosition.towards(Direction.WEST),
				party.getPosition());

		// --- Pas à droite
		assertTrue(dungeon.moveParty(Move.RIGHT, true));

		assertEquals(initialPosition, party.getPosition());

		// --- Quart de tour gauche (x4)
		assertEquals(Direction.NORTH, party.getLookDirection());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		// --- Quart de tour droite (x4)
		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());
	}

	/**
	 * Un mur doit empêcher le groupe de se déplacer.
	 */
	public void testPartyBlockedByWalls() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position partyPosition = new Position(1, 1, 1);

		dungeon.setParty(partyPosition, party);

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.FORWARD, true));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertFalse(dungeon.moveParty(Move.BACKWARD, true));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertFalse(dungeon.moveParty(Move.LEFT, true));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertFalse(dungeon.moveParty(Move.RIGHT, true));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());
	}

	public void testPartyEmptiness() {
		Party party = new Party();

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));
	}

	public void testChampionAddedToParty() {
		Party party = new Party();

		// --- Ajouter Tiggy
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Ajouter deux fois le même champion doit échouer
		try {
			party.addChampion(tiggy);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Ajouter daroou
		Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);

		party.addChampion(daroou);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(2, party.getSize(true));
		assertEquals(2, party.getSize(false));

		// --- Ajouter chani
		Champion chani = ChampionFactory.getFactory().newChampion(Name.CHANI);

		party.addChampion(chani);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(3, party.getSize(true));
		assertEquals(3, party.getSize(false));

		// --- Ajouter wuuf
		Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		party.addChampion(wuuf);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertTrue(party.isFull());
		assertEquals(4, party.getSize(true));
		assertEquals(4, party.getSize(false));

		// --- Ajouter un 5ème champion doit échouer
		try {
			party.addChampion(ChampionFactory.getFactory().newChampion(
					Name.ALEX));
			fail();
		} catch (IllegalStateException e) {
			// OK
		}
	}

	public void testChampionRemovedFromParty() {
		Party party = new Party();

		// --- Ajouter Tiggy
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		party.addChampion(tiggy);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Retirer un champion inconnu doit échouer
		try {
			party.removeChampion(ChampionFactory.getFactory().newChampion(
					Name.ALEX));
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Ajouter daroou, chani & wuuf
		Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);
		Champion chani = ChampionFactory.getFactory().newChampion(Name.CHANI);
		Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);

		party.addChampion(daroou);
		party.addChampion(chani);
		party.addChampion(wuuf);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertTrue(party.isFull());
		assertEquals(4, party.getSize(true));
		assertEquals(4, party.getSize(false));

		// --- Retirer daroou
		final Location location1 = party.removeChampion(daroou);

		assertNotNull(location1);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(3, party.getSize(true));
		assertEquals(3, party.getSize(false));

		// --- Retirer wuuf
		final Location location2 = party.removeChampion(wuuf);

		assertNotNull(location2);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(2, party.getSize(true));
		assertEquals(2, party.getSize(false));

		// --- Retirer tiggy
		final Location location3 = party.removeChampion(tiggy);

		assertNotNull(location3);

		assertFalse(party.isEmpty(true));
		assertFalse(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(1, party.getSize(true));
		assertEquals(1, party.getSize(false));

		// --- Retirer chani
		final Location location4 = party.removeChampion(chani);

		assertNotNull(location4);

		assertTrue(party.isEmpty(true));
		assertTrue(party.isEmpty(false));
		assertFalse(party.isFull());
		assertEquals(0, party.getSize(true));
		assertEquals(0, party.getSize(false));
	}

	public void testWaterFlaskUse() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final WaterFlask waterFlask = new WaterFlask();

		// --- Flasque remplie
		assertEquals(0.4f, waterFlask.getWeight(), 0.0001f);

		// --- Tiggy boit la fiole
		Item emptyFlask = tiggy.consume(waterFlask);

		// --- On récupère une fiole vide
		assertNotNull(emptyFlask);
		assertEquals(0.1f, emptyFlask.getWeight(), 0.0001f);
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

	public void testAllWeaponItemsHaveDamagePointsDefined() {
		for (Item.Type type : Item.Type.values()) {
			if (type.getCategory().equals(Category.WEAPON)) {
				assertTrue(type.getDamage() >= 0);
			} else {
				assertTrue(type.getDamage() == -1);
			}
		}
	}

	public void testDexterityPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getDexterity(),
				Item.Type.DEXTERITY_POTION);
	}

	public void testDexterityPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.DEXTERITY_POTION,
				Item.Type.DEXTERITY_POTION);
	}

	public void testChampionNotSkilledEnoughToCastSpell() throws Throwable {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// --- Requiert niveau 5 de compétence #15
		tiggy.cast(PowerRune.LO, Spell.Type.SHIELD_POTION);

		try {
			tiggy.castSpell();
			fail();
		} catch (SkillTooLowException e) {
			// Erreur attendue
		}

	}

	private void testPotionCasting(Spell.Type spellId, Item.Type itemType)
			throws NotEnoughManaException, ChampionMumblesNonsenseException,
			EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// Booster le niveau du champion pour pouvoir lancer tous les sorts
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		// --- Main vide -> sort doit rater
		tiggy.cast(PowerRune.LO, spellId);

		try {
			tiggy.castSpell();
			fail();
		} catch (EmptyFlaskNeededException e) {
			// Erreur attendue
		} catch (EmptyHandNeededException e) {
			fail();
		}

		// --- Main qui tient une fiole vide --> sort doit réussir
		tiggy.getBody().getWeaponHand().putOn(new EmptyFlask());

		// Les runes ont été conservés de l'invocation précédente
		tiggy.castSpell();

		// La main doit contenir une fiole remplie
		assertNotNull(tiggy.getBody().getWeaponHand().getItem());
		assertEquals(itemType, tiggy.getBody().getWeaponHand().getItem()
				.getType());
	}

	public void testStrengthPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getStrength(),
				Item.Type.STRENGTH_POTION);
	}

	public void testStrengthPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.STRENGTH_POTION, Item.Type.STRENGTH_POTION);
	}

	public void testWisdomPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getWisdom(), Item.Type.WISDOM_POTION);
	}

	public void testWisdomPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.WISDOM_POTION, Item.Type.WISDOM_POTION);
	}

	public void testVitalityPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getVitality(),
				Item.Type.VITALITY_POTION);
	}

	public void testVitalityPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.VITALITY_POTION, Item.Type.VITALITY_POTION);
	}

	public void testStaminaPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getStamina(),
				Item.Type.STAMINA_POTION);
	}

	public void testStaminaPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.STAMINA_POTION, Item.Type.STAMINA_POTION);
	}

	public void testAntiMagicPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getAntiMagic(),
				Item.Type.ANTI_MAGIC_POTION);
	}

	public void testAntiMagicPotionCasting() throws Throwable {
		fail();
	}

	public void testManaPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getMana(), Item.Type.MANA_POTION);
	}

	public void testManaPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.MANA_POTION, Item.Type.MANA_POTION);
	}

	public void testHealthPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getHealth(), Item.Type.HEALTH_POTION);
	}

	public void testHealthPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.HEALTH_POTION, Item.Type.HEALTH_POTION);
	}

	private void testPotion(Champion tiggy, IntStat statistic,
			fr.ritaly.dungeonmaster.item.Item.Type itemType) {

		final IntStat stat = statistic;

		stat.value(1);
		assertEquals(1, stat.actualValue().intValue());
		assertEquals(1, stat.value().intValue());
		assertEquals(0, stat.boostValue().intValue());

		// Consommer une potion augmente le boost de la statistique de manière
		// temporaire !
		tiggy.consume(ItemFactory.getFactory().newItem(itemType));

		assertTrue(stat.actualValue().intValue() > 1); // <--- Effet du boost
		assertEquals(1, stat.value().intValue());
		assertTrue(stat.boostValue().intValue() > 0); // <--- Boost changé
	}

	public void testBoostedStatWearsOff() {
		final IntStat stat = new IntStat("Test", "Stat", 1);

		// --- Vérifier l'état initial
		assertEquals(1, stat.actualValue().intValue());
		assertEquals(1, stat.value().intValue());
		assertEquals(0, stat.boostValue().intValue());

		// --- Augmenter le boost pour 6 tics
		stat.incBoost(10, 6);

		assertEquals(11, stat.actualValue().intValue()); // <---
		assertEquals(1, stat.value().intValue());
		assertEquals(10, stat.boostValue().intValue()); // <---

		// Attendre que l'effet du boost se dissipe
		Clock.getInstance().tick(5);

		assertEquals(11, stat.actualValue().intValue()); // <---
		assertEquals(1, stat.value().intValue());
		assertEquals(10, stat.boostValue().intValue());

		Clock.getInstance().tick();

		assertEquals(1, stat.actualValue().intValue()); // <---
		assertEquals(1, stat.value().intValue());
		assertEquals(0, stat.boostValue().intValue()); // <---
	}

	public void testCurePoisonPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		assertFalse(tiggy.isPoisoned());

		tiggy.poison(PowerRune.LO);
		assertTrue(tiggy.isPoisoned());

		tiggy.consume(ItemFactory.getFactory().newItem(
				Item.Type.ANTIDOTE_POTION));
		assertFalse(tiggy.isPoisoned());
	}

	public void testCurePoisonPotionCasting() throws Throwable {
		testPotionCasting(Spell.Type.ANTIDOTE_POTION, Item.Type.ANTIDOTE_POTION);
	}

	public void testWaterSkin() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final WaterSkin waterSkin = new WaterSkin();

		// --- Outre initialement vide
		assertEquals(0.3f * 0 + 0.3f, waterSkin.getWeight(), 0.0001f);

		// --- Remplir l'outre
		waterSkin.fill();

		// --- Outre remplie
		assertEquals(0.3f * 3 + 0.3f, waterSkin.getWeight(), 0.0001f);

		// --- Tiggy boit une gorgée
		Item item1 = tiggy.consume(waterSkin);

		assertEquals(waterSkin, item1);
		assertEquals(0.3f * 2 + 0.3f, waterSkin.getWeight(), 0.0001f);

		// --- Tiggy boit une seconde gorgée
		Item item2 = tiggy.consume(waterSkin);

		assertEquals(waterSkin, item2);
		assertEquals(0.3f * 1 + 0.3f, waterSkin.getWeight(), 0.0001f);

		// --- Tiggy boit la dernière gorgée
		Item item3 = tiggy.consume(waterSkin);

		assertEquals(waterSkin, item3);
		assertEquals(0.3f * 0 + 0.3f, waterSkin.getWeight(), 0.0001f);
	}

	public void testRabbitsFoot() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final IntStat luck = tiggy.getStats().getLuck();
		final int initialLuck = luck.actualValue();

		// Le pied de lapin marche quelque soit l'endroit où il est porté !
		final Item rabbitsFoot = new MiscItem(Item.Type.RABBIT_FOOT);

		// --- Pied de lapin (Main #1) -> Chance +10
		assertNull(tiggy.getBody().getWeaponHand().putOn(rabbitsFoot));
		assertEquals(initialLuck + 10, luck.actualValue().intValue());

		assertEquals(rabbitsFoot, tiggy.getBody().getWeaponHand().takeOff());
		assertEquals(initialLuck, luck.actualValue().intValue());

		// --- Pied de lapin (Main #2) -> Chance +10
		assertNull(tiggy.getBody().getShieldHand().putOn(rabbitsFoot));
		assertEquals(initialLuck + 10, luck.actualValue().intValue());

		assertEquals(rabbitsFoot, tiggy.getBody().getShieldHand().takeOff());
		assertEquals(initialLuck, luck.actualValue().intValue());

		// --- Le pied de lapin ne peut être placé sur les autres parties du
		// corps
		assertEquals(initialLuck, luck.actualValue().intValue());
		assertEquals(rabbitsFoot, tiggy.getBody().getHead().putOn(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());
		assertEquals(rabbitsFoot, tiggy.getBody().getNeck().putOn(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());
		assertEquals(rabbitsFoot, tiggy.getBody().getTorso().putOn(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());
		assertEquals(rabbitsFoot, tiggy.getBody().getLegs().putOn(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());
		assertEquals(rabbitsFoot, tiggy.getBody().getFeet().putOn(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());

		// --- Le pied de lapin marche même s'il est placé dans l'inventaire

		// Sac à dos
		assertTrue(tiggy.getInventory().getBackPack().add(rabbitsFoot) != -1);
		assertEquals(initialLuck + 10, luck.actualValue().intValue());
		assertTrue(tiggy.getInventory().getBackPack().remove(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());

		// Sac à dos
		assertTrue(tiggy.getInventory().getPouch().add(rabbitsFoot) != -1);
		assertEquals(initialLuck + 10, luck.actualValue().intValue());
		assertTrue(tiggy.getInventory().getPouch().remove(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());

		// Carquois (la patte de lapin n'y tient pas)
		assertEquals(-1, tiggy.getInventory().getQuiver().add(rabbitsFoot));
		assertEquals(initialLuck, luck.actualValue().intValue());

		// FIXME Si patte de lapin dans coffre porté par joueur ?
	}

	public void testItemHandling() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// --- Test avec une torche
		final Torch torch = new Torch();

		// Weapon hand OK
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));
		assertEquals(torch, tiggy.getBody().getWeaponHand().takeOff());

		// Shield hand OK
		assertNull(tiggy.getBody().getShieldHand().putOn(torch));
		assertEquals(torch, tiggy.getBody().getShieldHand().takeOff());

		// Autres parties du corps KO
		assertEquals(torch, tiggy.getBody().getHead().putOn(torch));
		assertEquals(torch, tiggy.getBody().getNeck().putOn(torch));
		assertEquals(torch, tiggy.getBody().getTorso().putOn(torch));
		assertEquals(torch, tiggy.getBody().getLegs().putOn(torch));
		assertEquals(torch, tiggy.getBody().getFeet().putOn(torch));

		// Tests avec l'inventaire
		assertTrue(tiggy.getInventory().getBackPack().add(torch) != -1);
		assertTrue(tiggy.getInventory().getBackPack().remove(torch));

		assertTrue(tiggy.getInventory().getPouch().add(torch) == -1);
		assertFalse(tiggy.getInventory().getPouch().remove(torch));

		assertTrue(tiggy.getInventory().getQuiver().add(torch) == -1);
		assertFalse(tiggy.getInventory().getQuiver().remove(torch));
	}

	public void testTorchBurning() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Torch torch = new Torch();

		// --- La torche est initialement éteinte et neuve
		assertFalse(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT, torch.getLight());

		// --- Le champion prend la torche en main, elle s'allume
		// automatiquement
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));
		assertTrue(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT, torch.getLight());

		// --- Laisser la torche se consumer (1 fois)
		Clock.getInstance().tick(Torch.TICK_COUNT);

		assertTrue(torch.isBurning());
		assertTrue(torch.getLight() < Constants.MAX_LIGHT);
		assertEquals(Constants.MAX_LIGHT - 1, torch.getLight());

		// --- Le champion lâche la torche, elle s'éteint automatiquement
		final Item removed = tiggy.getBody().getWeaponHand().takeOff();

		assertNotNull(removed);
		assertEquals(torch, removed);
		assertFalse(torch.isBurning());
		assertEquals(Constants.MAX_LIGHT - 1, torch.getLight());
	}

	public void testIllumulet() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Item illumulet = ItemFactory.getFactory().newItem(
				Item.Type.ILLUMULET);

		// --- Pas de lumière initialement
		assertEquals(0, tiggy.getLight());

		// --- L'immumulet ne s'allume pas dans la main
		assertNull(tiggy.getBody().getWeaponHand().putOn(illumulet));
		assertEquals(0, tiggy.getLight());

		// --- L'immumulet s'allume uniquement quand on la porte au cou
		assertEquals(illumulet, tiggy.getBody().getWeaponHand().takeOff());
		assertNull(tiggy.getBody().getNeck().putOn(illumulet));
		assertTrue(tiggy.getLight() > 0);

		// --- L'immumulet s'allume uniquement quand on la porte au cou
		assertEquals(illumulet, tiggy.getBody().getNeck().takeOff());
		assertEquals(0, tiggy.getLight());
	}

	public void testHandTimeOut() throws Exception {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		// --- Initialement la main est utilisable
		final WeaponHand hand = tiggy.getBody().getWeaponHand();

		assertTrue(hand.isEnabled());

		// --- Lancer un sort afin de rendre la main indisponible
		final Spell spell = tiggy.cast(PowerRune.LO, ElementRune.FUL);

		assertNotNull(spell);
		assertTrue(spell.isValid());
		assertFalse(hand.isEnabled());

		// --- Attendre suffisamment longtemps pour que la main redevienne
		// disponible
		Clock.getInstance().tick(60);

		assertTrue(hand.isEnabled());
	}

	public void testRemovalOfCursedItem() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		final WeaponHand hand = tiggy.getBody().getWeaponHand();
		hand.putOn(torch);

		// --- Tenter de retirer un objet envoûté doit échouer
		assertNull(hand.takeOff());

		// --- Le même test réussit si on force la chose
		final Item item = hand.takeOff(true);

		assertNotNull(item);
		assertEquals(torch, item);

		// --- Replacer la torche dans la main du champion
		hand.putOn(torch);

		// --- Conjurer l'objet puis retenter (doit réussir)
		torch.conjure(PowerRune.EE);

		final Item item2 = hand.takeOff();

		assertNotNull(item2);
		assertEquals(torch, item2);
	}

	public void testCurseDetection() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		// --- Torche envoûtée mais non détectée
		assertTrue(torch.isCursed());
		assertFalse(torch.isCurseDetected());

		// --- Prendre le torche en main
		assertNull(tiggy.getBody().getWeaponHand().putOn(torch));

		// --- Torche envoûtée mais non détectée
		assertTrue(torch.isCursed());
		assertFalse(torch.isCurseDetected());

		// --- Tenter de retirer la torche -> envoûtement détecté
		assertNull(tiggy.getBody().getWeaponHand().takeOff());
		assertEquals(torch, tiggy.getBody().getWeaponHand().getItem());

		assertTrue(torch.isCursed());
		assertTrue(torch.isCurseDetected());

		// --- Conjurer l'envoûtement
		torch.conjure(PowerRune.MON);

		// --- Tenter de retirer la torche -> doit marcher
		final Item item = tiggy.getBody().getWeaponHand().takeOff();

		assertEquals(torch, item);

		assertFalse(torch.isCursed());
		assertFalse(torch.isCurseDetected());
	}

	public void testInfluenceOfCurseOnLuck() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Torch torch = new Torch();
		torch.curse(PowerRune.EE);

		final IntStat luck = tiggy.getStats().getLuck();
		luck.value(10);

		// --- La chance doit valoir initialement 10 points
		assertEquals(10, luck.actualValue().intValue());

		// --- Tiggy prend la torche, sa chance diminue de +3
		tiggy.getBody().getWeaponHand().putOn(torch);

		assertEquals(7, luck.actualValue().intValue());

		// --- Tiggy lâche la torche, sa chance remonte de +3
		final Item item = tiggy.getBody().getWeaponHand().takeOff(true);

		assertNotNull(item);
		assertEquals(torch, item);

		// --- Tiggy reprend la torche, sa chance diminue de +3
		tiggy.getBody().getWeaponHand().putOn(torch);

		assertEquals(7, luck.actualValue().intValue());

		// --- Conjurer la torche fait remonter la chance de +3
		torch.conjure(PowerRune.EE);

		assertEquals(10, luck.actualValue().intValue());

		// --- Ensorceler la torche diminue la chance de +3
		torch.curse(PowerRune.LO);

		assertEquals(7, luck.actualValue().intValue());
	}

	public void testTorchPutOnBody() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		final Body body = tiggy.getBody();

		final Torch torch = new Torch();

		// --- Mettre la torche sur la tête (doit échouer)
		assertEquals(torch, body.getHead().putOn(torch));

		// --- Mettre la torche sur le cou (doit échouer)
		assertEquals(torch, body.getNeck().putOn(torch));

		// --- Mettre la torche sur le torse (doit échouer)
		assertEquals(torch, body.getTorso().putOn(torch));

		// --- Mettre la torche sur les jambes (doit échouer)
		assertEquals(torch, body.getLegs().putOn(torch));

		// --- Mettre la torche sur les pieds (doit échouer)
		assertEquals(torch, body.getFeet().putOn(torch));

		// --- Mettre la torche dans une main (doit réussir)
		assertNull(body.getShieldHand().putOn(torch));

		// --- Retirer la torche de la main qui le tient (doit réussir)
		assertEquals(torch, body.getShieldHand().takeOff());

		// --- Mettre la torche dans l'autre main (doit réussir)
		assertNull(body.getWeaponHand().putOn(torch));

		// --- On ne peut mettre dans l'autre main la torche si elle est déjà
		// portée
		try {
			body.getShieldHand().putOn(torch);
			fail();
		} catch (RuntimeException e) {
			// OK
		}
	}

	public void testSpellCasting() throws Exception {
		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

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
			final int initialMana = tiggy.getStats().getMana().actualValue();

			final Rune rune = PowerRune.ON;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(), (int) tiggy.getStats()
					.getMana().actualValue());
		}

		// --- Invoquer un element rune
		{
			final int initialMana = tiggy.getStats().getMana().actualValue();

			final Rune rune = ElementRune.ZO;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(PowerRune.ON), (int) tiggy
					.getStats().getMana().actualValue());
		}

		// --- Invoquer un form rune
		{
			final int initialMana = tiggy.getStats().getMana().actualValue();

			final Rune rune = FormRune.KATH;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(PowerRune.ON), (int) tiggy
					.getStats().getMana().actualValue());
		}

		// --- Invoquer un alignment rune
		{
			final int initialMana = tiggy.getStats().getMana().actualValue();

			final Rune rune = AlignmentRune.RA;
			tiggy.cast(rune);

			assertEquals(initialMana - rune.getCost(PowerRune.ON), (int) tiggy
					.getStats().getMana().actualValue());
		}

		// --- Lancer le sort
		final Experience experience = tiggy
				.getExperience(Spell.Type.LIGHTNING_BOLT.getSkill());
		final int initialXp = experience.getPoints();

		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		assertEquals(Spell.Type.ZO_KATH_RA, spell.getType());
		assertTrue(experience.getPoints() > initialXp);
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

	@Override
	protected void setUp() throws Exception {
		log.info("--- Running test " + getName() + " ---");
	}

	// public void testDoorTriggering() {
	// // +---+---+---+---+---+
	// // | W | W | W | W | W |
	// // +---+---+---+---+---+
	// // | W | . | D | . | W |
	// // +---+---+---+---+---+
	// // | W | . | P | . | W |
	// // +---+---+---+---+---+
	// // | W | . | . | . | W |
	// // +---+---+---+---+---+
	// // | W | W | W | W | W |
	// // +---+---+---+---+---+
	//
	// Dungeon dungeon = new Dungeon();
	// dungeon.createLevel(1, 5, 5);
	//
	// Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
	//
	// Party party = new Party();
	// party.addChampion(tiggy);
	//
	// final Position initialPosition = new Position(2, 2, 1);
	//
	// dungeon.setParty(initialPosition, party);
	//
	// // Porte face au groupe
	// final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH);
	//
	// dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
	//
	// final Actuator actuator = new SimpleActuator(2,
	// TriggeringAction.TOGGLE, door);
	//
	// Clock.getInstance().register(actuator);
	//
	// // --- Porte fermée
	// assertEquals(Door.State.CLOSED, door.getState());
	//
	// Clock.getInstance().tick(20);
	//
	// // --- Porte ouverte
	// assertEquals(Door.State.OPEN, door.getState());
	//
	// final Actuator actuator2 = new SimpleActuator(2,
	// TriggeringAction.TOGGLE, door);
	//
	// Clock.getInstance().register(actuator2);
	//
	// Clock.getInstance().tick(20);
	//
	// // --- Porte fermée
	// assertEquals(Door.State.CLOSED, door.getState());
	// }

	public void testAutomaticLeaderSelectionWhenRemovingLeader() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);

		final Party party = new Party();

		// --- Vérifier l'état initial
		assertNull(party.getLeader());

		// --- Ajouter un champion au groupe
		party.addChampion(tiggy);

		assertEquals(tiggy, party.getLeader());

		// --- Ajouter un autre champion au groupe
		party.addChampion(wuuf);

		assertEquals(tiggy, party.getLeader());

		// --- Supprimer tiggy du groupe, wuuf devient leader
		party.removeChampion(tiggy);

		assertEquals(wuuf, party.getLeader());

		// --- Supprimer wuuf
		party.removeChampion(wuuf);

		assertNull(party.getLeader());
	}

	public void testAutomaticLeaderSelectionWhenLeaderDies() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);

		final Party party = new Party();

		// --- Vérifier l'état initial
		assertNull(party.getLeader());

		// --- Test quand le leader meurt
		party.addChampion(tiggy);
		party.addChampion(wuuf);

		assertEquals(tiggy, party.getLeader());
		assertTrue(tiggy.die());
		assertEquals(wuuf, party.getLeader());
	}

	public void testAllChampionsDie() {
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

		dungeon.setParty(1, 1, 1, party);

		// --- Le champion est vivant, il porte des objets
		assertTrue(tiggy.isAlive());

		// --- Le champion meurt
		assertTrue(tiggy.die());
	}

	public void testChampionResurrection() {
		// +---+---+---+
		// | W | A | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Altar altar = new Altar(Direction.SOUTH);

		final Dungeon dungeon = new Dungeon();
		final Level level = dungeon.createLevel(1, 3, 3);
		level.setElement(1, 0, altar);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		final Champion daroou = ChampionFactory.getFactory().newChampion(
				Name.DAROOU);

		final Party party = new Party();
		party.addChampion(tiggy);
		party.addChampion(daroou);

		dungeon.setParty(1, 1, 1, party);

		// Situation initiale
		assertTrue(tiggy.isAlive());
		assertTrue(daroou.isAlive());

		// Tiggy meurt
		assertTrue(tiggy.die());

		assertFalse(tiggy.isAlive());
		assertTrue(daroou.isAlive());

		// Récupérer les os de Tiggy
		assertNotNull(level.getElement(1, 1).getItems());
		assertEquals(1, level.getElement(1, 1).getItemCount());

		final Bones bones = (Bones) level.getElement(1, 1).getItems()
				.iterator().next();

		assertNotNull(bones.getChampion());
		assertEquals(tiggy, bones.getChampion());

		assertEquals(0, altar.getItemCount());
		
		// Placer les os dans l'autel réssuscite le champion
		altar.dropItem(bones, Direction.SOUTH);

		assertTrue(tiggy.isAlive());
		assertTrue(daroou.isAlive());
		
		// L'autel doit être vide
		assertEquals(0, altar.getItemCount());
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

		// Equiper tiggy avec différents objets (x7)
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

	public void testChestItem() {
		final Chest chest = new Chest();

		// --- Coffre vide
		assertEquals(8, chest.getCapacity());
		assertEquals(0, chest.getItemCount());
		assertFalse(chest.isFull());
		assertTrue(chest.isEmpty());

		// --- Coffre rempli
		for (int i = 0; i < 8; i++) {
			assertTrue(chest.add(new Torch()) >= 0);
		}

		assertEquals(8, chest.getItemCount());
		assertTrue(chest.isFull());
		assertFalse(chest.isEmpty());

		// Vider le coffre
		chest.removeAll();

		// --- Tests avec des objets qui ne peuvent pas tenir dans un coffre
		assertEquals(-1, chest.add(new Chest()));
		assertEquals(-1, chest.add(new Weapon(Item.Type.STAFF_OF_CLAWS)));
		assertEquals(-1, chest.add(new Weapon(Item.Type.FALCHION)));

		// --- Tests avec des objets qui peuvent tenir dans un coffre
		assertTrue(chest.add(new Potion(Item.Type.HEALTH_POTION)) >= 0);
		assertTrue(chest.add(new Torch()) >= 0);
		assertTrue(chest.add(new Weapon(Item.Type.EYE_OF_TIME)) >= 0);
		assertTrue(chest.add(new Weapon(Item.Type.DAGGER)) >= 0);
	}

	public void testAllItemsCanBeInstanciated() {
		for (Item.Type type : Item.Type.values()) {
			assertNotNull(ItemFactory.getFactory().newItem(type));
		}
	}

	// FIXME testRopeWhenFacingPit

	// FIXME testFloorTriggeringWhenSpecificItemDropped
	// FIXME testFloorTriggeringWhenSpecificItemPickedUp
	// FIXME testActuatorTriggeredWhenPullingLever
	// FIXME testActuatorTriggeredWhenPushingLever
	// FIXME testActuatorTriggeredWhenPickingItemFrom4SideAlcove
	// FIXME testActuatorTriggeredWhenDroppingItemInto4SideAlcove
	// FIXME testActuatorTriggeredWhenPushingDoorButton
	// FIXME testActuatorTriggeredWhenUsingDoorLock

	public void testLeverTriggeringPit() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | I | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | L |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Pit pit = new Pit(false, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(2, 1, pit);

		final Lever lever = new Lever(Direction.EAST, true);
		level1.setElement(4, 2, lever);

		lever.setActuator(new SimpleActuator(2, TriggerAction.TOGGLE, pit));

		// --- Situation initiale
		assertTrue(lever.isLeverUp());
		assertTrue(pit.isClosed());

		// --- On baisse le levier - l'oubliette doit s'ouvrir
		lever.toggle();

		// Attendre que l'oubliette s'ouvre
		Clock.getInstance().tick(2);

		assertFalse(lever.isLeverUp());
		assertTrue(pit.isOpen());

		// --- On remonte le levier - l'oubliette doit se fermer
		lever.toggle();

		// Attendre que l'oubliette s'ouvre
		Clock.getInstance().tick(2);

		assertTrue(lever.isLeverUp());
		assertFalse(pit.isOpen());
	}

	public void testWallSwitchTriggeringPit() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | I | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | S |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Pit pit = new Pit(false, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(2, 1, pit);

		final WallSwitch wallSwitch = new WallSwitch(Direction.EAST);
		level1.setElement(4, 2, wallSwitch);

		wallSwitch
				.setActuator(new SimpleActuator(2, TriggerAction.TOGGLE, pit));

		// --- Situation initiale
		assertFalse(wallSwitch.isPressed());
		assertTrue(pit.isClosed());

		// --- On presse le bouton - l'oubliette doit s'ouvrir
		wallSwitch.toggle();

		// Attendre que l'oubliette s'ouvre
		Clock.getInstance().tick(2);

		assertTrue(wallSwitch.isPressed());
		assertTrue(pit.isOpen());

		// --- On represse le bouton - l'oubliette doit se fermer
		wallSwitch.toggle();

		// Attendre que l'oubliette s'ouvre
		Clock.getInstance().tick(2);

		assertFalse(wallSwitch.isPressed());
		assertFalse(pit.isOpen());
	}

	public void testElementIsConcrete() {
		for (Element.Type type : Element.Type.values()) {
			// La méthode doit toujours retourner un résultat
			type.isConcrete();
		}
	}

	public void testPartyBlockedByRetractableWall() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | R | . | W |
		// +---+---+---+---+---+
		// | W | R | P | R | W |
		// +---+---+---+---+---+
		// | W | . | R | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final RetractableWall retractableWallNorth = new RetractableWall();
		final RetractableWall retractableWallSouth = new RetractableWall();
		final RetractableWall retractableWallWest = new RetractableWall();
		final RetractableWall retractableWallEast = new RetractableWall();

		Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);
		dungeon.setElement(new Position(2, 1, 1), retractableWallNorth);
		dungeon.setElement(new Position(2, 3, 1), retractableWallSouth);
		dungeon.setElement(new Position(1, 2, 1), retractableWallWest);
		dungeon.setElement(new Position(3, 2, 1), retractableWallEast);

		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Position partyPosition = new Position(2, 2, 1);

		dungeon.setParty(partyPosition, party);

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.FORWARD, true));
		assertEquals(partyPosition, party.getPosition());

		retractableWallNorth.open();

		assertTrue(dungeon.moveParty(Move.FORWARD, true));
		assertEquals(partyPosition.towards(Direction.NORTH),
				party.getPosition());

		assertTrue(dungeon.moveParty(Move.BACKWARD, true));
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.BACKWARD, true));
		assertEquals(partyPosition, party.getPosition());

		retractableWallSouth.open();

		assertTrue(dungeon.moveParty(Move.BACKWARD, true));
		assertEquals(partyPosition.towards(Direction.SOUTH),
				party.getPosition());

		assertTrue(dungeon.moveParty(Move.FORWARD, true));
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.LEFT, true));
		assertEquals(partyPosition, party.getPosition());

		retractableWallWest.open();

		assertTrue(dungeon.moveParty(Move.LEFT, true));
		assertEquals(partyPosition.towards(Direction.WEST), party.getPosition());

		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertFalse(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(partyPosition, party.getPosition());

		retractableWallEast.open();

		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(partyPosition.towards(Direction.EAST), party.getPosition());

		assertTrue(dungeon.moveParty(Move.LEFT, true));
		assertEquals(partyPosition, party.getPosition());
	}

	public void testCreatureCanCastSpells() {
		for (Creature.Type type : Creature.Type.values()) {
			if (type.canCastSpell()) {
				assertFalse(type.getSpells().isEmpty());
			} else {
				assertTrue(type.getSpells().isEmpty());
			}
		}
	}

	public void testCantInstallTwoCreaturesAtSamePlace() {
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

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Element element = level1.getElement(2, 2);

		final Creature mummy = new Creature(Creature.Type.MUMMY, 10);

		// --- Situation initiale
		assertFalse(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertTrue(element.getCreatureMap().isEmpty());

		// 1. On installe la momie
		element.creatureSteppedOn(mummy, SubCell.NORTH_EAST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(mummy, element.getCreatureMap().get(SubCell.NORTH_EAST));

		// 2.
		try {
			element.creatureSteppedOn(mummy, SubCell.NORTH_EAST);

			fail();
		} catch (RuntimeException e) {
			// Erreur attendue
		}

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(mummy, element.getCreatureMap().get(SubCell.NORTH_EAST));
	}

	public void testCantInstallCreatureIfNotEnoughRoomLeft() {
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

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Element element = level1.getElement(2, 2);

		final Creature mummy = new Creature(Creature.Type.MUMMY, 10);
		final Creature trolin = new Creature(Creature.Type.TROLIN, 10);
		final Creature rockPile = new Creature(Creature.Type.ROCK_PILE, 10);
		final Creature giggler = new Creature(Creature.Type.GIGGLER, 10);

		// --- Situation initiale
		assertFalse(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(0, element.getCreatureCount());
		assertTrue(element.getCreatureMap().isEmpty());

		// 1. On installe les 4 créatures de taille 1
		element.creatureSteppedOn(mummy, SubCell.NORTH_EAST);
		element.creatureSteppedOn(trolin, SubCell.NORTH_WEST);
		element.creatureSteppedOn(rockPile, SubCell.SOUTH_EAST);
		element.creatureSteppedOn(giggler, SubCell.SOUTH_WEST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(4, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(mummy, element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertEquals(trolin, element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

		// 2. On supprime une créature de taille 1 et on tente d'installer un
		// ver (de taille 2) ou un dragon (de taille 4)
		element.creatureSteppedOff(mummy, SubCell.NORTH_EAST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(3, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertNull(element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertEquals(trolin, element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

		// On teste dans toutes les directions
		for (Direction direction : Arrays.asList(Direction.NORTH,
				Direction.EAST, Direction.SOUTH, Direction.WEST)) {

			try {
				element.creatureSteppedOn(new Creature(
						Creature.Type.MAGENTA_WORM, 10), direction);
				fail();
			} catch (IllegalArgumentException e) {
				// Erreur attendue
			}
		}

		try {
			element.creatureSteppedOn(new Creature(Creature.Type.RED_DRAGON, 10));
			fail();
		} catch (IllegalArgumentException e) {
			// Erreur attendue
		}

		// 3. On supprime une autre créature de taille 1 et on tente d'installer
		// un dragon (de taille 4) ou un ver (de taille 2)
		element.creatureSteppedOff(trolin, SubCell.NORTH_WEST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(2, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertNull(element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertNull(element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

		try {
			// Dragon trop gros
			element.creatureSteppedOn(new Creature(Creature.Type.RED_DRAGON, 10));
			fail();
		} catch (IllegalArgumentException e) {
			// Erreur attendue
		}

		// On teste dans les 3 directions qui doivent échouer (E,S,W)
		for (Direction direction : Arrays.asList(Direction.EAST,
				Direction.SOUTH, Direction.WEST)) {

			try {
				element.creatureSteppedOn(new Creature(
						Creature.Type.MAGENTA_WORM, 10), direction);
				fail();
			} catch (IllegalArgumentException e) {
				// Erreur attendue
			}
		}

		// Ca doit marcher dans la dernière direction
		final Creature worm = new Creature(Creature.Type.MAGENTA_WORM, 10);

		element.creatureSteppedOn(worm, Direction.NORTH);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(3, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(worm, element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertEquals(worm, element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

	}

	// FIXME Tester l'effet des sorts (FUL, Potions)
	
	public void testChampionsHurtWhenHittingConcreteWalls() {
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
		tiggy.getStats().getHealth().maxValue(500);
		tiggy.getStats().getHealth().value(500);
		
		final Champion daroou = ChampionFactory.getFactory().newChampion(
				Name.DAROOU);
		daroou.getStats().getHealth().maxValue(500);
		daroou.getStats().getHealth().value(500);
		
		final Champion halk = ChampionFactory.getFactory().newChampion(
				Name.HALK);
		halk.getStats().getHealth().maxValue(500);
		halk.getStats().getHealth().value(500);
		
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);
		wuuf.getStats().getHealth().maxValue(500);
		wuuf.getStats().getHealth().value(500);

		// Attention ! La position des champions dans le groupe est importante
		// pour la suite du test !
		final Party party = new Party();
		assertEquals(Location.FRONT_LEFT, party.addChampion(tiggy));
		assertEquals(Location.FRONT_RIGHT, party.addChampion(daroou));
		assertEquals(Location.REAR_LEFT, party.addChampion(halk));
		assertEquals(Location.REAR_RIGHT, party.addChampion(wuuf));

		dungeon.setParty(new Position(1, 1, 1), party);

		assertEquals(Direction.NORTH, party.getLookDirection());
		
		// --- FORWARD. Seuls deux héros sont blessés
		final int tiggyHealth1 = tiggy.getStats().getHealth().value();
		final int daroouHealth1 = daroou.getStats().getHealth().value();
		final int halkHealth1 = halk.getStats().getHealth().value();
		final int wuufHealth1 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.FORWARD, true));
		
		assertTrue(tiggy.getStats().getHealth().value() < tiggyHealth1);
		assertTrue(daroou.getStats().getHealth().value() < daroouHealth1);
		assertEquals(halkHealth1, halk.getStats().getHealth().value().intValue());
		assertEquals(wuufHealth1, wuuf.getStats().getHealth().value().intValue());
		
		// --- BACKWARD. Seuls deux héros sont blessés
		final int tiggyHealth2 = tiggy.getStats().getHealth().value();
		final int daroouHealth2 = daroou.getStats().getHealth().value();
		final int halkHealth2 = halk.getStats().getHealth().value();
		final int wuufHealth2 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.BACKWARD, true));
		
		assertEquals(tiggyHealth2, tiggy.getStats().getHealth().value().intValue());
		assertEquals(daroouHealth2, daroou.getStats().getHealth().value().intValue());
		assertTrue(halk.getStats().getHealth().value() < halkHealth2);
		assertTrue(wuuf.getStats().getHealth().value() < wuufHealth2);
		
		// --- LEFT. Seuls deux héros sont blessés
		final int tiggyHealth3 = tiggy.getStats().getHealth().value();
		final int daroouHealth3 = daroou.getStats().getHealth().value();
		final int halkHealth3 = halk.getStats().getHealth().value();
		final int wuufHealth3 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.LEFT, true));
		
		assertTrue(tiggy.getStats().getHealth().value() < tiggyHealth3);
		assertEquals(daroouHealth3, daroou.getStats().getHealth().value().intValue());
		assertTrue(halk.getStats().getHealth().value() < halkHealth3);
		assertEquals(wuufHealth3, wuuf.getStats().getHealth().value().intValue());

		// --- RIGHT. Seuls deux héros sont blessés
		final int tiggyHealth4 = tiggy.getStats().getHealth().value();
		final int daroouHealth4 = daroou.getStats().getHealth().value();
		final int halkHealth4 = halk.getStats().getHealth().value();
		final int wuufHealth4 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.RIGHT, true));
		
		assertEquals(tiggyHealth4, tiggy.getStats().getHealth().value().intValue());
		assertTrue(daroou.getStats().getHealth().value() < daroouHealth4);
		assertEquals(halkHealth4, halk.getStats().getHealth().value().intValue());
		assertTrue(wuuf.getStats().getHealth().value() < wuufHealth4);
	}
	
	public void testInfluenceOfElvenBootsOnMaxLoad() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Item elvenBoots = ItemFactory.getFactory().newItem(
				Item.Type.ELVEN_BOOTS);

		final IntStat maxLoadBoost = tiggy.getStats().getMaxLoadBoost();
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
	
	public void testDispellIllusionSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		final Party party = new Party();
		party.addChampion(tiggy);

		// --- Lancer le sort
		tiggy.cast(PowerRune.LO, Spell.Type.DISPELL_ILLUSION);
		
		assertFalse(party.dispellsIllusions());
		assertFalse(party.getSpells().isDispellIllusionActive());
		
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		assertTrue(party.dispellsIllusions());
		assertTrue(party.getSpells().isDispellIllusionActive());
	}
	
	public void testInvisibilitySpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		final Party party = new Party();
		party.addChampion(tiggy);

		// --- Lancer le sort
		tiggy.cast(PowerRune.LO, Spell.Type.INVISIBILITY);
		
		assertFalse(party.isInvisible());
		assertFalse(party.getSpells().isInvisibilityActive());
		
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		assertTrue(party.isInvisible());
		assertTrue(party.getSpells().isInvisibilityActive());
	}
	
	public void testAntiMagicSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.setSkill(Skill.DEFEND, Champion.Level.ARCH_MASTER);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		final Party party = new Party();
		party.addChampion(tiggy);

		// --- Lancer le sort
		tiggy.cast(PowerRune.LO, Spell.Type.ANTI_MAGIC);
		
		assertEquals(0, party.getSpells().getAntiMagic().value().intValue());
		
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		assertTrue(party.getSpells().getAntiMagic().value().intValue() > 0);
	}
	
	public void testShieldSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.setSkill(Skill.DEFEND, Champion.Level.ARCH_MASTER);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		final Party party = new Party();
		party.addChampion(tiggy);

		// --- Lancer le sort
		tiggy.cast(PowerRune.LO, Spell.Type.SHIELD);
		
		assertEquals(0, party.getSpells().getShield().value().intValue());
		
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		assertTrue(party.getSpells().getShield().value().intValue() > 0);
	}
	
	public void testLightSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		final Party party = new Party();
		party.addChampion(tiggy);

		// --- Lancer le sort
		tiggy.cast(PowerRune.LO, Spell.Type.LIGHT);
		
		assertEquals(0, tiggy.getSpells().getLight().value().intValue());
		
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		assertTrue(tiggy.getSpells().getLight().value().intValue() > 0);
	}
	
	public void testSeeThroughWallsSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.setSkill(Skill.DEFEND, Champion.Level.ARCH_MASTER);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		final Party party = new Party();
		party.addChampion(tiggy);

		// --- Lancer le sort
		tiggy.cast(PowerRune.LO, Spell.Type.SEE_THROUGH_WALLS);
		
		assertFalse(party.seesThroughWalls());
		assertFalse(party.getSpells().isSeeThroughWallsActive());
		
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		assertTrue(party.seesThroughWalls());
		assertTrue(party.getSpells().isSeeThroughWallsActive());
	}
}