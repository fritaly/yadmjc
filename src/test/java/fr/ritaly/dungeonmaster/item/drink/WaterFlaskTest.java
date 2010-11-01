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
package fr.ritaly.dungeonmaster.item.drink;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.item.Item;

public class WaterFlaskTest extends TestCase {

	public WaterFlaskTest() {
	}

	public WaterFlaskTest(String name) {
		super(name);
	}

	public void testChampionDrinksWaterFlask() {
		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);

		final WaterFlask waterFlask = new WaterFlask();

		// --- Flasque remplie
		assertEquals(0.4f, waterFlask.getWeight(), 0.0001f);

		// --- Tiggy boit la fiole
		final Item emptyFlask = tiggy.consume(waterFlask);

		// --- On récupère une fiole vide
		assertNotNull(emptyFlask);
		assertEquals(0.1f, emptyFlask.getWeight(), 0.0001f);
	}
}