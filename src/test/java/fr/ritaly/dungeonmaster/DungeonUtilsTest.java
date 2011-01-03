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

import java.util.ArrayList;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.DungeonUtils;
import fr.ritaly.dungeonmaster.map.Level;

public class DungeonUtilsTest extends TestCase {

	public DungeonUtilsTest(String name) {
		super(name);
	}

	public void testSimpleParse() {
		final Dungeon dungeon = new Dungeon();
		
		final ArrayList<String> list = new ArrayList<String>();
		list.add("WWWWWWWWWW");
		list.add("W        W");
		list.add("WWWWWWWWWW");
		
		final Level level = DungeonUtils.parse(list, dungeon, 1);

		assertNotNull(level);
		assertEquals(1, level.getLevel());
		assertEquals(10, level.getWidth());
		assertEquals(3, level.getHeight());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}