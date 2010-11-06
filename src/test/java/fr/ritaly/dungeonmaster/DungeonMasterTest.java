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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.body.WeaponHand;
import fr.ritaly.dungeonmaster.item.Action;
import fr.ritaly.dungeonmaster.item.Cloth;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.item.Weapon;
import fr.ritaly.dungeonmaster.magic.ElementRune;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;
import fr.ritaly.dungeonmaster.map.ValidationException;
import fr.ritaly.dungeonmaster.map.Wall;
import fr.ritaly.dungeonmaster.stat.Stat;

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
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(initialPosition.towards(Direction.NORTH),
				party.getPosition());

		// --- Pas en arrière
		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));

		assertEquals(initialPosition, party.getPosition());

		// --- Pas à gauche
		assertTrue(dungeon.moveParty(Move.LEFT, true, AudioClip.STEP));

		assertEquals(initialPosition.towards(Direction.WEST),
				party.getPosition());

		// --- Pas à droite
		assertTrue(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));

		assertEquals(initialPosition, party.getPosition());

		// --- Quart de tour gauche (x4)
		assertEquals(Direction.NORTH, party.getLookDirection());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		// --- Quart de tour droite (x4)
		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(initialPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

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

		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertFalse(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertFalse(dungeon.moveParty(Move.LEFT, true, AudioClip.STEP));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertFalse(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));

		assertEquals(partyPosition, party.getPosition());

		// ---
		assertEquals(Direction.NORTH, party.getLookDirection());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		// ---
		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.EAST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.WEST, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());

		assertTrue(dungeon.moveParty(Move.TURN_RIGHT, true, AudioClip.STEP));

		assertEquals(Direction.NORTH, party.getLookDirection());
		assertEquals(partyPosition, party.getPosition());
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

	public void testAntiMagicPotionCasting() throws Throwable {
		fail("Not yet implemented");
	}

	public void testBoostedStatWearsOff() {
		final Stat stat = new Stat("Test", "Stat", 1);

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

	// FIXME testRopeWhenFacingPit

	// FIXME testFloorTriggeringWhenSpecificItemDropped
	// FIXME testFloorTriggeringWhenSpecificItemPickedUp
	// FIXME testActuatorTriggeredWhenPickingItemFrom4SideAlcove
	// FIXME testActuatorTriggeredWhenDroppingItemInto4SideAlcove
	// FIXME testActuatorTriggeredWhenPushingDoorButton
	// FIXME testActuatorTriggeredWhenUsingDoorLock

	public void testElementIsConcrete() {
		for (Element.Type type : Element.Type.values()) {
			// La méthode doit toujours retourner un résultat
			type.isConcrete();
		}
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
		
		assertFalse(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		
		assertTrue(tiggy.getStats().getHealth().value() < tiggyHealth1);
		assertTrue(daroou.getStats().getHealth().value() < daroouHealth1);
		assertEquals(halkHealth1, halk.getStats().getHealth().value().intValue());
		assertEquals(wuufHealth1, wuuf.getStats().getHealth().value().intValue());
		
		// --- BACKWARD. Seuls deux héros sont blessés
		final int tiggyHealth2 = tiggy.getStats().getHealth().value();
		final int daroouHealth2 = daroou.getStats().getHealth().value();
		final int halkHealth2 = halk.getStats().getHealth().value();
		final int wuufHealth2 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		
		assertEquals(tiggyHealth2, tiggy.getStats().getHealth().value().intValue());
		assertEquals(daroouHealth2, daroou.getStats().getHealth().value().intValue());
		assertTrue(halk.getStats().getHealth().value() < halkHealth2);
		assertTrue(wuuf.getStats().getHealth().value() < wuufHealth2);
		
		// --- LEFT. Seuls deux héros sont blessés
		final int tiggyHealth3 = tiggy.getStats().getHealth().value();
		final int daroouHealth3 = daroou.getStats().getHealth().value();
		final int halkHealth3 = halk.getStats().getHealth().value();
		final int wuufHealth3 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.LEFT, true, AudioClip.STEP));
		
		assertTrue(tiggy.getStats().getHealth().value() < tiggyHealth3);
		assertEquals(daroouHealth3, daroou.getStats().getHealth().value().intValue());
		assertTrue(halk.getStats().getHealth().value() < halkHealth3);
		assertEquals(wuufHealth3, wuuf.getStats().getHealth().value().intValue());

		// --- RIGHT. Seuls deux héros sont blessés
		final int tiggyHealth4 = tiggy.getStats().getHealth().value();
		final int daroouHealth4 = daroou.getStats().getHealth().value();
		final int halkHealth4 = halk.getStats().getHealth().value();
		final int wuufHealth4 = wuuf.getStats().getHealth().value();
		
		assertFalse(dungeon.moveParty(Move.RIGHT, true, AudioClip.STEP));
		
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
	
	public void testHealActionCanHealChampions() {
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
		tiggy.getStats().getHealth().value(400);
		
		final Champion daroou = ChampionFactory.getFactory().newChampion(
				Name.DAROOU);
		daroou.getStats().getHealth().maxValue(500);
		daroou.getStats().getHealth().value(400);
		
		final Champion halk = ChampionFactory.getFactory().newChampion(
				Name.HALK);
		halk.getStats().getHealth().maxValue(500);
		halk.getStats().getHealth().value(400);
		
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);
		wuuf.getStats().getHealth().maxValue(500);
		wuuf.getStats().getHealth().value(400);
		
		final Weapon crossOfNeta = new Weapon(Item.Type.CROSS_OF_NETA);
		tiggy.getBody().getWeaponHand().putOn(crossOfNeta);
		
		final Party party = new Party();
		party.addChampion(tiggy);
		party.addChampion(daroou);
		party.addChampion(halk);
		party.addChampion(wuuf);
		
		dungeon.setParty(new Position(1, 1, 1), party);
		
		// --- Tous les héros sont guéris par l'action HEAL
		final int tiggyHealth = tiggy.getStats().getHealth().value();
		final int daroouHealth = daroou.getStats().getHealth().value();
		final int halkHealth = halk.getStats().getHealth().value();
		final int wuufHealth = wuuf.getStats().getHealth().value();
		
		assertTrue(crossOfNeta.perform(Action.HEAL));
		
		assertTrue(tiggy.getStats().getHealth().value() > tiggyHealth);
		assertTrue(daroou.getStats().getHealth().value() > daroouHealth);
		assertTrue(halk.getStats().getHealth().value() > halkHealth);
		assertTrue(wuuf.getStats().getHealth().value() > wuufHealth);
	}
	
	// FIXME Implémenter le nombre de charges par item + utilisation limitée
}