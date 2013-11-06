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
package fr.ritaly.dungeonmaster.magic;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.body.Hand;
import fr.ritaly.dungeonmaster.champion.body.WeaponHand;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.magic.EmptyHandNeededException;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Door.Motion;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Door.State;
import fr.ritaly.dungeonmaster.map.Floor;

public class SpellTest extends TestCase {

	public SpellTest() {
	}

	public SpellTest(String name) {
		super(name);
	}

	private void testCastPotion(Spell.Type spellType, Item.Type expectedItem) throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(ItemFactory.getFactory().newItem(Item.Type.EMPTY_FLASK));

		// Boost all the champion's skills to be able to cast any spell
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		tiggy.cast(PowerRune.LO);

		for (Rune rune : spellType.getRunes()) {
			tiggy.cast(rune);
		}

		assertTrue(tiggy.getBody().getWeaponHand().hasItem(Item.Type.EMPTY_FLASK));

		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());
		assertTrue(tiggy.getBody().getWeaponHand().hasItem(expectedItem));
	}

	public void testCastPotion_Health() throws Exception {
		testCastPotion(Spell.Type.HEALTH_POTION, Item.Type.HEALTH_POTION);
	}

	public void testCastPotion_Stamina() throws Exception {
		testCastPotion(Spell.Type.STAMINA_POTION, Item.Type.STAMINA_POTION);
	}

	public void testCastPotion_Mana() throws Exception {
		testCastPotion(Spell.Type.MANA_POTION, Item.Type.MANA_POTION);
	}

	public void testCastPotion_Strength() throws Exception {
		testCastPotion(Spell.Type.STRENGTH_POTION, Item.Type.STRENGTH_POTION);
	}

	public void testCastPotion_Dexterity() throws Exception {
		testCastPotion(Spell.Type.DEXTERITY_POTION, Item.Type.DEXTERITY_POTION);
	}

	public void testCastPotion_Wisdom() throws Exception {
		testCastPotion(Spell.Type.WISDOM_POTION, Item.Type.WISDOM_POTION);
	}

	public void testCastPotion_Vitality() throws Exception {
		testCastPotion(Spell.Type.VITALITY_POTION, Item.Type.VITALITY_POTION);
	}

	public void testCastPotion_AntiDote() throws Exception {
		testCastPotion(Spell.Type.ANTIDOTE_POTION, Item.Type.ANTIDOTE_POTION);
	}

	public void testCastPotion_Shield() throws Exception {
		testCastPotion(Spell.Type.SHIELD_POTION, Item.Type.ANTI_MAGIC_POTION);
	}

	// --- //

	public void testCastTorchSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Boost all the champion's skills to be able to cast any spell
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		// Backup the initial light value
		final int light = tiggy.getLight();

		// Cast a TORCH spell
		tiggy.cast(PowerRune.LO, Spell.Type.TORCH);

		final Spell spell = tiggy.castSpell();

		// Casting must have succeeded
		assertNotNull(spell);
		assertTrue(spell.isValid());

		// The spell must have generated some light
		assertTrue(tiggy.getLight() > light);
	}

	// --- ZO KATH RA --- //

	public void testCastZoKathRaSpell_EmptyHands() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Boost all the champion's skills to be able to cast any spell
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();
		final Hand shieldHand = tiggy.getBody().getShieldHand();

		// Both hands must be empty
		assertTrue(weaponHand.isEmpty());
		assertTrue(shieldHand.isEmpty());

		// Cast ZO KATH RA
		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);

		final Spell spell = tiggy.castSpell();

		// Casting must have succeeded
		assertNotNull(spell);
		assertTrue(spell.isValid());

		final Item item1 = weaponHand.getItem();
		final Item item2 = shieldHand.getItem();

		// Either two hands must hold a ZO_KATH_RA item
		assertTrue(((item1 == null) && (item2 != null)) || ((item1 != null) && (item2 == null)));

		// The item must have the correct type
		if (item1 != null) {
			assertEquals(Item.Type.ZOKATHRA_SPELL, item1.getType());
		} else if (item2 != null) {
			assertEquals(Item.Type.ZOKATHRA_SPELL, item2.getType());
		} else {
			fail();
		}
	}

	public void testCastZoKathRaSpell_EmptyWeaponHand() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Boost all the champion's skills to be able to cast any spell
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();

		final Hand shieldHand = tiggy.getBody().getShieldHand();
		shieldHand.putOn(new Torch());

		// One empty hand initially
		assertTrue(weaponHand.isEmpty());
		assertFalse(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		// Both hands must be holding an item
		assertFalse(weaponHand.isEmpty());
		assertEquals(Item.Type.ZOKATHRA_SPELL, weaponHand.getItem().getType());

		assertFalse(shieldHand.isEmpty());
		assertEquals(Item.Type.TORCH, shieldHand.getItem().getType());
	}

	public void testCastZoKathRaSpell_EmptyShieldHand() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Boost all the champion's skills to be able to cast any spell
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();
		weaponHand.putOn(new Torch());

		final Hand shieldHand = tiggy.getBody().getShieldHand();

		// Only one hand empty initially
		assertFalse(weaponHand.isEmpty());
		assertTrue(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);

		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		// Both hands must hold an item
		assertFalse(weaponHand.isEmpty());
		assertEquals(Item.Type.TORCH, weaponHand.getItem().getType());

		assertFalse(shieldHand.isEmpty());
		assertEquals(Item.Type.ZOKATHRA_SPELL, shieldHand.getItem().getType());
	}

	public void testCastZoKathRaSpell_NoEmptyHand() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// Boost all the champion's skills to be able to cast any spell
		for (Skill skill : Skill.values()) {
			tiggy.gainExperience(skill, 1000000);
		}

		final WeaponHand weaponHand = tiggy.getBody().getWeaponHand();
		weaponHand.putOn(new Torch());

		final Hand shieldHand = tiggy.getBody().getShieldHand();
		shieldHand.putOn(new Torch());

		// No empty hand initially
		assertFalse(weaponHand.isEmpty());
		assertFalse(shieldHand.isEmpty());

		tiggy.cast(PowerRune.LO, Spell.Type.ZO_KATH_RA);

		try {
			tiggy.castSpell();

			fail("Casting ZO KATH RA with no empty hand should fail");
		} catch (EmptyHandNeededException e) {
			// Expected error
		}
	}

	// --- //

	public void testCastLightSpell() throws Exception {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getStats().getMana().maxValue(200);
		tiggy.getStats().getMana().value(200);

		// --- Cast the spell LIGHT
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

	public void testFireballSpellCanDestroyClosedBreakableDoor()
			throws Exception {
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

	public void testFireballSpellCantDestroyAnUnbreakableDoor()
			throws Exception {
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

	public void testPoisonCloudSpell() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
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
		tiggy.gainExperience(Skill.WATER, 100000);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 2, 1), party);

		final Floor floor = (Floor) dungeon.getElement(2, 1, 1);

		// --- Situation initiale
		assertFalse(floor.hasPoisonClouds());

		// Lancer le sort POISON_CLOUD
		tiggy.cast(PowerRune.MON, Spell.Type.POISON_CLOUD);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		// Laisser le temps au projectile de "mourir"
		Clock.getInstance().tick(18);

		// --- Un nuage de poison doit �tre apparu
		assertTrue(floor.hasPoisonClouds());
		assertEquals(1, floor.getPoisonCloudCount());

		// --- Si on attend suffisamment longtemps, le nuage va dispara�tre de
		// lui-m�me
		Clock.getInstance().tick(60);

		assertFalse(floor.hasPoisonClouds());
	}

	public void testPoisonCloudSpellWhenPartyNearWall() throws Exception {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
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
		tiggy.gainExperience(Skill.WATER, 100000);
		tiggy.getStats().getHealth().maxValue(500);
		tiggy.getStats().getHealth().value(500);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 1, 1), party);

		final Floor floor = (Floor) dungeon.getElement(2, 1, 1);

		// --- Situation initiale
		assertFalse(floor.hasPoisonClouds());

		// Lancer le sort POISON_CLOUD (qui explose sur place)
		tiggy.cast(PowerRune.MON, Spell.Type.POISON_CLOUD);
		final Spell spell = tiggy.castSpell();

		assertNotNull(spell);
		assertTrue(spell.isValid());

		// Laisser le temps au projectile de "mourir"
		Clock.getInstance().tick(18);

		final int health = tiggy.getStats().getHealth().value();

		// --- Un nuage de poison doit �tre apparu
		assertTrue(floor.hasPoisonClouds());
		assertEquals(1, floor.getPoisonCloudCount());

		// --- Si on attend suffisamment longtemps, le nuage va dispara�tre de
		// lui-m�me
		Clock.getInstance().tick(60);

		assertFalse(floor.hasPoisonClouds());

		// --- La sant� du champion doit avoir diminu� du fait du poison
		assertTrue(health > tiggy.getStats().getHealth().value().intValue());
	}

	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}