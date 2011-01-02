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
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;

public class ActionTest extends TestCase {

	public ActionTest() {
	}

	public ActionTest(String name) {
		super(name);
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

	public void testThrowAction() throws Throwable {
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
		// | W | . | . | . | . | . | <-+---+ P | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		final Weapon dagger = new Weapon(Item.Type.DAGGER);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(dagger);

		final Party party = new Party(tiggy);
		party.setDirection(Direction.WEST);

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 10, 10);

		dungeon.setParty(8, 5, 1, party);

		// --- Pas d'objet au sol initialement
		final Element target = level1.getElement(1, 5);
		final Element neighbour = level1.getElement(7, 5);

		assertFalse(target.hasItems());

		// --- Déclencher l'action de lancer
		dagger.perform(Action.THROW);

		// --- Un projectile doit être apparu sur la position voisine en SE
		assertTrue(neighbour.hasProjectiles());
		assertEquals(1, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(SubCell.SOUTH_EAST));

		assertFalse(target.hasProjectiles());

		// Laisser le projectile bouger
		Clock.getInstance().tick(3);

		// --- Vérifier le déplacement du projectile en SW
		assertTrue(neighbour.hasProjectiles());
		assertEquals(1, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(SubCell.SOUTH_WEST));

		assertFalse(target.hasProjectiles());

		// Laisser le projectile atteindre le mur opposée (attendre suffisamment
		// longtemps)
		Clock.getInstance().tick(60);

		assertFalse(neighbour.hasProjectiles());
		assertFalse(target.hasProjectiles());

		// Il doit y avoir 1 objet au sol
		assertEquals(1, target.getItemCount());

		assertFalse(target.getItems(SubCell.SOUTH_WEST).isEmpty());
		assertEquals(1, target.getItemCount(SubCell.SOUTH_WEST));
		assertEquals(Item.Type.DAGGER, target.getItems(SubCell.SOUTH_WEST)
				.iterator().next().getType());
	}

	public void testFluxCageAction() throws Throwable {
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
		// | W | . | . | . | . | . | . | F | P | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		final Weapon firestaff = new Weapon(Item.Type.THE_FIRESTAFF_COMPLETE);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(firestaff);

		final Party party = new Party(tiggy);
		party.setDirection(Direction.WEST);

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 10, 10);

		dungeon.setParty(8, 5, 1, party);

		// --- Pas de cage initialement
		final Element neighbour = level1.getElement(7, 5);

		assertFalse(neighbour.hasFluxCage());

		// --- Déclencher l'action
		firestaff.perform(Action.FLUX_CAGE);

		// --- Une cage doit être apparue sur la position voisine
		assertTrue(neighbour.hasFluxCage());

		// Laisser le temps à la cage de disparaître
		Clock.getInstance().tick(60);

		assertFalse(neighbour.hasFluxCage());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}