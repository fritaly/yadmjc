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
}