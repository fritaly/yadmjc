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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.ValidationException;

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

	public void testAntiMagicPotionCasting() throws Throwable {
		fail("Not yet implemented");
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

	@Override
	protected void setUp() throws Exception {
		log.info("--- Running test " + getName() + " ---");
		
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
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
	
	// FIXME Implémenter le nombre de charges par item + utilisation limitée
}