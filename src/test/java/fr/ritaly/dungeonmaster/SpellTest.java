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
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.body.Hand;
import fr.ritaly.dungeonmaster.champion.body.WeaponHand;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.magic.EmptyHandNeededException;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Door.Motion;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Door.State;

public class SpellTest extends TestCase {

	public SpellTest() {
	}

	public SpellTest(String name) {
		super(name);
	}

	public void testTorchSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		// Booster le niveau du champion pour pouvoir lancer tous les sorts
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final int light = tiggy.getLight();

		tiggy.cast(PowerRune.LO, Spell.Type.TORCH);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		assertTrue(tiggy.getLight() > light);
	}

	public void testZoKathRaSpellEmptyHands() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		// Booster le niveau du champion pour pouvoir lancer tous les sorts
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();
		final Hand shieldHand = tiggy.getBody().getShieldHand();

		// Les 2 mains doivent initialement être vides
		assertTrue(weaponHand.isEmpty());
		assertTrue(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		final Item item1 = weaponHand.getItem();
		final Item item2 = shieldHand.getItem();

		// L'une des deux mains doit contenir un item de type ZO_KATH_RA
		assertTrue(((item1 == null) && (item2 != null))
				|| ((item1 != null) && (item2 == null)));

		if (item1 != null) {
			assertEquals(Item.Type.ZOKATHRA_SPELL, item1.getType());
		} else if (item2 != null) {
			assertEquals(Item.Type.ZOKATHRA_SPELL, item2.getType());
		} else {
			fail();
		}
	}

	public void testZoKathRaSpellEmptyWeaponHand() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		// Booster le niveau du champion pour pouvoir lancer tous les sorts
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();

		final Hand shieldHand = tiggy.getBody().getShieldHand();
		shieldHand.putOn(new Torch());

		// Une seule main vide initialement
		assertTrue(weaponHand.isEmpty());
		assertFalse(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		// Les deux mains doivent être remplies
		assertFalse(weaponHand.isEmpty());
		assertEquals(Item.Type.ZOKATHRA_SPELL, weaponHand.getItem().getType());

		assertFalse(shieldHand.isEmpty());
		assertEquals(Item.Type.TORCH, shieldHand.getItem().getType());
	}

	public void testZoKathRaSpellEmptyShieldHand() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		// Booster le niveau du champion pour pouvoir lancer tous les sorts
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();
		weaponHand.putOn(new Torch());

		final Hand shieldHand = tiggy.getBody().getShieldHand();

		// Une seule main vide initialement
		assertFalse(weaponHand.isEmpty());
		assertTrue(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		// Les deux mains doivent être remplies
		assertFalse(weaponHand.isEmpty());
		assertEquals(Item.Type.TORCH, weaponHand.getItem().getType());

		assertFalse(shieldHand.isEmpty());
		assertEquals(Item.Type.ZOKATHRA_SPELL, shieldHand.getItem().getType());
	}

	public void testZoKathRaSpellNoEmptyHand() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		// Booster le niveau du champion pour pouvoir lancer tous les sorts
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();
		weaponHand.putOn(new Torch());

		final Hand shieldHand = tiggy.getBody().getShieldHand();
		shieldHand.putOn(new Torch());

		// Aucune main vide initialement
		assertFalse(weaponHand.isEmpty());
		assertFalse(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);

		try {
			tiggy.castSpell();

			fail();
		} catch (EmptyHandNeededException e) {
			// Erreur attendue
		}
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

	public void testOpenDoorSpellToOpenDoor() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH);

		dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
		
		assertEquals(State.CLOSED, door.getState());

		// Lancer le sort ZO
		tiggy.cast(PowerRune.LO, Spell.Type.OPEN_DOOR);
		final Spell spell = tiggy.castSpell();
		
		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		Clock.getInstance().tick(24);
		
		assertEquals(State.OPEN, door.getState());
	}
	
	public void testOpenDoorSpellToCloseDoor() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH,
				Door.State.OPEN);

		dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
		
		assertEquals(State.OPEN, door.getState());

		// Lancer le sort ZO
		tiggy.cast(PowerRune.LO, Spell.Type.OPEN_DOOR);
		final Spell spell = tiggy.castSpell();
		
		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		Clock.getInstance().tick(30);
		
		assertEquals(State.CLOSED, door.getState());
	}
	
	public void testFireballSpellCanDestroyClosedBreakableDoor() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.gainExperience(Skill.WIZARD, 100000);

		final Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH,
				Door.State.CLOSED);

		dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
		
		assertEquals(State.CLOSED, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertFalse(door.isBroken());

		// Lancer le sort FUL IR
		tiggy.cast(PowerRune.MON, Spell.Type.FIREBALL);
		final Spell spell = tiggy.castSpell();
		
		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		Clock.getInstance().tick(30);
		
		assertEquals(State.BROKEN, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertTrue(door.isBroken());
	}
	
	public void testFireballSpellCantDestroyAnOpenDoor() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.gainExperience(Skill.WIZARD, 100000);

		final Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH,
				Door.State.OPEN);

		dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
		
		assertEquals(State.OPEN, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertFalse(door.isBroken());

		// Lancer le sort FUL IR
		tiggy.cast(PowerRune.MON, Spell.Type.FIREBALL);
		final Spell spell = tiggy.castSpell();
		
		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		Clock.getInstance().tick(30);
		
		assertEquals(State.OPEN, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertFalse(door.isBroken());
	}
	
	public void testFireballSpellCantDestroyBrokenDoor() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.gainExperience(Skill.WIZARD, 100000);

		final Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH,
				Door.State.CLOSED);
		door.destroy();

		dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
		
		assertEquals(State.BROKEN, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertTrue(door.isBroken());

		// Lancer le sort FUL IR
		tiggy.cast(PowerRune.MON, Spell.Type.FIREBALL);
		final Spell spell = tiggy.castSpell();
		
		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		Clock.getInstance().tick(30);
		
		assertEquals(State.BROKEN, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertTrue(door.isBroken());
	}
	
	public void testFireballSpellCantDestroyAnUnbreakableDoor() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.gainExperience(Skill.WIZARD, 100000);

		final Party party = new Party();
		party.addChampion(tiggy);

		final Position initialPosition = new Position(2, 2, 1);

		dungeon.setParty(initialPosition, party);

		// Porte face au groupe
		final Door door = new Door(Door.Style.RA, Orientation.NORTH_SOUTH,
				Door.State.CLOSED);

		dungeon.setElement(initialPosition.towards(Direction.NORTH), door);
		
		assertEquals(State.CLOSED, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertFalse(door.isBroken());

		// Lancer le sort FUL IR
		tiggy.cast(PowerRune.MON, Spell.Type.FIREBALL);
		final Spell spell = tiggy.castSpell();
		
		assertNotNull(spell);
		assertTrue(spell.isValid());
		
		Clock.getInstance().tick(30);
		
		assertEquals(State.CLOSED, door.getState());
		assertEquals(Motion.IDLE, door.getMotion());
		assertFalse(door.isBroken());
	}
}