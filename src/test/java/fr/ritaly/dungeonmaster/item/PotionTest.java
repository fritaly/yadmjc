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

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.stat.Stat;

public class PotionTest extends TestCase {

	public PotionTest() {
	}

	public PotionTest(String name) {
		super(name);
	}

	private void testPotion(Champion tiggy, Stat statistic,
			fr.ritaly.dungeonmaster.item.Item.Type itemType) {

		final Stat stat = statistic;

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
	
	public void testDexterityPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getDexterity(),
				Item.Type.DEXTERITY_POTION);
	}
	
	public void testStrengthPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getStrength(),
				Item.Type.STRENGTH_POTION);
	}
	
	public void testWisdomPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getWisdom(), Item.Type.WISDOM_POTION);
	}
	
	public void testVitalityPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getVitality(),
				Item.Type.VITALITY_POTION);
	}
	
	public void testStaminaPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getStamina(),
				Item.Type.STAMINA_POTION);
	}
	
	public void testAntiMagicPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getAntiMagic(),
				Item.Type.ANTI_MAGIC_POTION);
	}
	
	public void testManaPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getMana(), Item.Type.MANA_POTION);
	}
	
	public void testHealthPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		testPotion(tiggy, tiggy.getStats().getHealth(), Item.Type.HEALTH_POTION);
	}
	
	public void testCurePoisonPotion() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		assertFalse(tiggy.isPoisoned());
		tiggy.poison(PowerRune.LO);
		assertTrue(tiggy.isPoisoned());

		tiggy.consume(ItemFactory.getFactory().newItem(
				Item.Type.ANTIDOTE_POTION));
		assertFalse(tiggy.isPoisoned());
	}
}