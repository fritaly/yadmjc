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

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.stat.Stat;
import junit.framework.TestCase;

public class RabbitFootTest extends TestCase {

	public RabbitFootTest() {
	}

	public RabbitFootTest(String name) {
		super(name);
	}

	public void testRabbitsFoot() {
		Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		Party party = new Party();
		party.addChampion(tiggy);

		final Stat luck = tiggy.getStats().getLuck();
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
}