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
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;

public class PartySpellsTest extends TestCase {

	public PartySpellsTest() {
	}

	public PartySpellsTest(String name) {
		super(name);
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
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}