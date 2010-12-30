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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.ai.Creature;

public class ElementTest extends TestCase {

	public ElementTest() {
	}

	public ElementTest(String name) {
		super(name);
	}

	public void testMethodIsConcreteMustBeSupported() {
		for (Element.Type type : Element.Type.values()) {
			// La méthode doit toujours retourner un résultat
			type.isConcrete();
		}
	}
	
	public void testCantInstallTwoCreaturesAtSamePlace() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Element element = level1.getElement(2, 2);

		final Creature mummy = new Creature(Creature.Type.MUMMY, 10);

		// --- Situation initiale
		assertFalse(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertTrue(element.getCreatureMap().isEmpty());

		// 1. On installe la momie
		element.creatureSteppedOn(mummy, SubCell.NORTH_EAST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(mummy, element.getCreatureMap().get(SubCell.NORTH_EAST));

		// 2.
		try {
			element.creatureSteppedOn(mummy, SubCell.NORTH_EAST);

			fail();
		} catch (RuntimeException e) {
			// Erreur attendue
		}

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(mummy, element.getCreatureMap().get(SubCell.NORTH_EAST));
	}

	public void testCantInstallCreatureIfNotEnoughRoomLeft() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Element element = level1.getElement(2, 2);

		final Creature mummy = new Creature(Creature.Type.MUMMY, 10);
		final Creature trolin = new Creature(Creature.Type.TROLIN, 10);
		final Creature rockPile = new Creature(Creature.Type.ROCK_PILE, 10);
		final Creature giggler = new Creature(Creature.Type.GIGGLER, 10);

		// --- Situation initiale
		assertFalse(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(0, element.getCreatureCount());
		assertTrue(element.getCreatureMap().isEmpty());

		// 1. On installe les 4 créatures de taille 1
		element.creatureSteppedOn(mummy, SubCell.NORTH_EAST);
		element.creatureSteppedOn(trolin, SubCell.NORTH_WEST);
		element.creatureSteppedOn(rockPile, SubCell.SOUTH_EAST);
		element.creatureSteppedOn(giggler, SubCell.SOUTH_WEST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(4, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(mummy, element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertEquals(trolin, element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

		// 2. On supprime une créature de taille 1 et on tente d'installer un
		// ver (de taille 2) ou un dragon (de taille 4)
		element.creatureSteppedOff(mummy, SubCell.NORTH_EAST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(3, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertNull(element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertEquals(trolin, element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

		// On teste dans toutes les directions
		for (Direction direction : Arrays.asList(Direction.NORTH,
				Direction.EAST, Direction.SOUTH, Direction.WEST)) {

			try {
				element.creatureSteppedOn(new Creature(
						Creature.Type.MAGENTA_WORM, 10), direction);
				fail();
			} catch (IllegalArgumentException e) {
				// Erreur attendue
			}
		}

		try {
			element.creatureSteppedOn(new Creature(Creature.Type.RED_DRAGON, 10));
			fail();
		} catch (IllegalArgumentException e) {
			// Erreur attendue
		}

		// 3. On supprime une autre créature de taille 1 et on tente d'installer
		// un dragon (de taille 4) ou un ver (de taille 2)
		element.creatureSteppedOff(trolin, SubCell.NORTH_WEST);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(2, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertNull(element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertNull(element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));

		try {
			// Dragon trop gros
			element.creatureSteppedOn(new Creature(Creature.Type.RED_DRAGON, 10));
			fail();
		} catch (IllegalArgumentException e) {
			// Erreur attendue
		}

		// On teste dans les 3 directions qui doivent échouer (E,S,W)
		for (Direction direction : Arrays.asList(Direction.EAST,
				Direction.SOUTH, Direction.WEST)) {

			try {
				element.creatureSteppedOn(new Creature(
						Creature.Type.MAGENTA_WORM, 10), direction);
				fail();
			} catch (IllegalArgumentException e) {
				// Erreur attendue
			}
		}

		// Ca doit marcher dans la dernière direction
		final Creature worm = new Creature(Creature.Type.MAGENTA_WORM, 10);

		element.creatureSteppedOn(worm, Direction.NORTH);

		assertTrue(element.hasCreatures());
		assertNotNull(element.getCreatureMap());
		assertEquals(3, element.getCreatureCount());
		assertFalse(element.getCreatureMap().isEmpty());
		assertEquals(worm, element.getCreatureMap().get(SubCell.NORTH_EAST));
		assertEquals(worm, element.getCreatureMap().get(SubCell.NORTH_WEST));
		assertEquals(rockPile, element.getCreatureMap().get(SubCell.SOUTH_EAST));
		assertEquals(giggler, element.getCreatureMap().get(SubCell.SOUTH_WEST));
	}
	
	public void testSurroundingElements() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | . | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 3, 3);
		
		{
			// Element en (0,0)
			final List<Element> surroundingElements = level1.getElement(0, 0)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(3, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(1, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(0, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
		}
		{
			// Element en (1,0)
			final List<Element> surroundingElements = level1.getElement(1, 0)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(5, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(0, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(0, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 1)));
		}
		{
			// Element en (2,0)
			final List<Element> surroundingElements = level1.getElement(2, 0)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(3, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(1, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 1)));
		}
		{
			// Element en (0,1)
			final List<Element> surroundingElements = level1.getElement(0, 1)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(5, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(0, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(0, 2)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 2)));
		}
		{
			// Element en (1,1)
			final List<Element> surroundingElements = level1.getElement(1, 1)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(8, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(0, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(0, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(0, 2)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 2)));
		}
		{
			// Element en (2,1)
			final List<Element> surroundingElements = level1.getElement(2, 1)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(5, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(2, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 0)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 2)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 2)));
		}
		{
			// Element en (0,2)
			final List<Element> surroundingElements = level1.getElement(0, 2)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(3, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(0, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 2)));
		}
		{
			// Element en (1,2)
			final List<Element> surroundingElements = level1.getElement(1, 2)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(5, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(0, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(0, 2)));
			assertTrue(surroundingElements.contains(level1.getElement(2, 2)));
		}
		{
			// Element en (2,2)
			final List<Element> surroundingElements = level1.getElement(2, 2)
					.getSurroundingElements();
			
			assertNotNull(surroundingElements);
			assertEquals(3, surroundingElements.size());
			assertTrue(surroundingElements.contains(level1.getElement(2, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 1)));
			assertTrue(surroundingElements.contains(level1.getElement(1, 2)));
		}
	}
}