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
package fr.ritaly.dungeonmaster.map;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.ai.Creature;

public class GeneratorTest extends TestCase {

	public GeneratorTest() {
	}

	public GeneratorTest(String name) {
		super(name);
	}
	
	public void testGenerateCreaturesOfSizeFour() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | G | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Generator generator = new Generator(Creature.Type.RED_DRAGON, 1);
		level1.setElement(3, 2, generator);

		assertEquals(0, generator.getCreatureCount());
		assertNull(generator.getCreature(SubCell.NORTH_EAST));
		assertNull(generator.getCreature(SubCell.NORTH_WEST));
		assertNull(generator.getCreature(SubCell.SOUTH_EAST));
		assertNull(generator.getCreature(SubCell.SOUTH_WEST));

		// --- On laisse le générateur créer des créatures
		Clock.getInstance().tick(Generator.PERIOD);

		assertTrue(generator.getCreatureCount() > 0);
		assertNotNull(generator.getCreatures());
		assertFalse(generator.getCreatures().isEmpty());
	}
	
	public void testGenerateCreaturesOfSizeOne() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | G | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Generator generator = new Generator(Creature.Type.MUMMY, 1);
		level1.setElement(3, 2, generator);

		assertEquals(0, generator.getCreatureCount());
		assertNull(generator.getCreature(SubCell.NORTH_EAST));
		assertNull(generator.getCreature(SubCell.NORTH_WEST));
		assertNull(generator.getCreature(SubCell.SOUTH_EAST));
		assertNull(generator.getCreature(SubCell.SOUTH_WEST));

		// --- On laisse le générateur créer des créatures
		Clock.getInstance().tick(Generator.PERIOD);

		assertTrue(generator.getCreatureCount() > 0);
		assertNotNull(generator.getCreatures());
		assertFalse(generator.getCreatures().isEmpty());
	}
	
	public void testGenerateCreaturesOfSizeTwo() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | G | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Generator generator = new Generator(Creature.Type.MAGENTA_WORM, 1);
		level1.setElement(3, 2, generator);

		assertEquals(0, generator.getCreatureCount());
		assertNull(generator.getCreature(SubCell.NORTH_EAST));
		assertNull(generator.getCreature(SubCell.NORTH_WEST));
		assertNull(generator.getCreature(SubCell.SOUTH_EAST));
		assertNull(generator.getCreature(SubCell.SOUTH_WEST));

		// --- On laisse le générateur créer des créatures
		Clock.getInstance().tick(Generator.PERIOD);

		assertTrue(generator.getCreatureCount() > 0);
		assertNotNull(generator.getCreatures());
		assertFalse(generator.getCreatures().isEmpty());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}