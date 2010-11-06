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
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;

public class DungeonTest extends TestCase {

	public DungeonTest(String name) {
		super(name);
	}

	public void testSimpleDungeon() {
		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 10, 10);
		assertNotNull(level1);
		assertEquals(10, level1.getWidth());
		assertEquals(10, level1.getHeight());

		// Le niveau doit être entouré de murs
		for (int x = 0; x < level1.getWidth(); x++) {
			final Element element1 = level1.getElement(x, 0);
			final Element element2 = level1.getElement(x,
					level1.getHeight() - 1);

			assertNotNull(element1);
			assertEquals(Element.Type.WALL, element1.getType());

			assertNotNull(element2);
			assertEquals(Element.Type.WALL, element2.getType());
		}

		// Le niveau doit être rempli de dalles de sol
		for (int x = 1; x < level1.getWidth() - 1; x++) {
			for (int y = 1; y < level1.getHeight() - 1; y++) {
				final Element element = level1.getElement(x, y);

				assertNotNull(element);
				assertEquals(Element.Type.FLOOR, element.getType());
			}
		}
	}
}