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
package fr.ritaly.dungeonmaster.ai.astar;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.ai.astar.PathFinder;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.DungeonUtils;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;

public class LevelPathFinderTest extends TestCase {

	public LevelPathFinderTest() {
	}

	public LevelPathFinderTest(String name) {
		super(name);
	}

	public void testFindPathWhenMaterial() {
		// La créature (matérielle) doit atteindre la cible X
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | S | W | . | . | . | W | X | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | W | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final List<String> rows = new ArrayList<String>();
		rows.add("WWWWWWWWW");
		rows.add("W W   W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W   W   W");
		rows.add("WWWWWWWWW");

		final Level level1 = DungeonUtils.parse(rows, dungeon, 1);
		final List<Element> nodes = new PathFinder(level1, Materiality.MATERIAL)
				.findBestPath(1, 1, 7, 1);
		
		// La solution passe par 31 noeuds
		assertNotNull(nodes);
		assertEquals(31, nodes.size());
	}
	
	public void testFindPathWhenImmaterial() {
		// La créature (immatérielle) doit atteindre la cible X
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | S | W | . | . | . | W | X | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | W | . | W | . | W | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | W | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final List<String> rows = new ArrayList<String>();
		rows.add("WWWWWWWWW");
		rows.add("W W   W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W W W W W");
		rows.add("W   W   W");
		rows.add("WWWWWWWWW");

		final Level level1 = DungeonUtils.parse(rows, dungeon, 1);

		final List<Element> nodes = new PathFinder(level1,
				Materiality.IMMATERIAL).findBestPath(1, 1, 7, 1);
		
		// La solution passe par 7 noeuds
		assertNotNull(nodes);
		assertEquals(7, nodes.size());
	}

	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}