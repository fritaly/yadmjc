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
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Food;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.item.Weapon;
import fr.ritaly.dungeonmaster.item.drink.WaterFlask;

public class PitTest extends TestCase {

	public PitTest() {
	}

	public PitTest(String name) {
		super(name);
	}

	public void testPartyFallingThroughOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		level1.setElement(3, 2, new Pit());

		dungeon.createLevel(2, 5, 5);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// --- Le groupe tombe à travers l'oubliette
		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(new Position(3, 2, 2), dungeon.getParty().getPosition());
	}
	
	public void testPartyFallingThroughSeveralStackedOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		
		// Level3:
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
		level1.setElement(3, 2, new Pit());

		final Level level2 = dungeon.createLevel(2, 5, 5);
		level2.setElement(3, 2, new Pit());
		
		dungeon.createLevel(3, 5, 5);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// --- Le groupe tombe à travers l'oubliette
		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(new Position(3, 2, 3), dungeon.getParty().getPosition());
	}
	
	public void testFakePit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		level1.setElement(3, 2, new Pit(true));

		dungeon.createLevel(2, 5, 5);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// --- Le groupe ne tombe pas à travers l'oubliette (car fausse)
		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(new Position(3, 2, 1), dungeon.getParty().getPosition());
	}
	
	public void testPartyFallingThroughPitWhenOpened() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, false);
		level1.setElement(3, 2, pit);

		dungeon.createLevel(2, 5, 5);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// --- Le groupe ne tombe pas à travers l'oubliette (car fermée)
		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(new Position(3, 2, 1), dungeon.getParty().getPosition());

		// --- Quand l'oubliette s'ouvre, le groupe tombe à travers
		pit.open();
		assertEquals(new Position(3, 2, 2), dungeon.getParty().getPosition());
	}

	public void testItemsFallingThroughPitWhenOpened() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, false);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Torch torch = new Torch();
		final Food apple = new Food(Item.Type.APPLE);
		final Weapon sword = new Weapon(Item.Type.SWORD);
		final WaterFlask waterFlask = new WaterFlask();

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		element1.itemDroppedDown(torch, SubCell.NORTH_WEST);
		element1.itemDroppedDown(apple, SubCell.NORTH_EAST);
		element1.itemDroppedDown(sword, SubCell.SOUTH_WEST);
		element1.itemDroppedDown(waterFlask, SubCell.SOUTH_EAST);

		// --- Situation initiale
		assertEquals(4, element1.getItemCount());
		assertEquals(1, element1.getItemCount(SubCell.NORTH_EAST));
		assertEquals(apple, element1.getItems(SubCell.NORTH_EAST).iterator()
				.next());
		assertEquals(1, element1.getItemCount(SubCell.NORTH_WEST));
		assertEquals(torch, element1.getItems(SubCell.NORTH_WEST).iterator()
				.next());
		assertEquals(1, element1.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(waterFlask, element1.getItems(SubCell.SOUTH_EAST)
				.iterator().next());
		assertEquals(1, element1.getItemCount(SubCell.SOUTH_WEST));
		assertEquals(sword, element1.getItems(SubCell.SOUTH_WEST).iterator()
				.next());
		assertEquals(0, element2.getItemCount());
		assertEquals(0, element2.getItemCount(SubCell.NORTH_EAST));
		assertEquals(0, element2.getItemCount(SubCell.NORTH_WEST));
		assertEquals(0, element2.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(0, element2.getItemCount(SubCell.SOUTH_WEST));

		// --- L'oubliette s'ouvre, les objets tombent au niveau inférieur
		assertTrue(pit.open());
		assertEquals(0, element1.getItemCount());
		assertEquals(0, element1.getItemCount(SubCell.NORTH_EAST));
		assertEquals(0, element1.getItemCount(SubCell.NORTH_WEST));
		assertEquals(0, element1.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(0, element1.getItemCount(SubCell.SOUTH_WEST));
		assertEquals(4, element2.getItemCount());
		assertEquals(1, element2.getItemCount(SubCell.NORTH_EAST));
		assertEquals(apple, element2.getItems(SubCell.NORTH_EAST).iterator()
				.next());
		assertEquals(1, element2.getItemCount(SubCell.NORTH_WEST));
		assertEquals(torch, element2.getItems(SubCell.NORTH_WEST).iterator()
				.next());
		assertEquals(1, element2.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(waterFlask, element2.getItems(SubCell.SOUTH_EAST)
				.iterator().next());
		assertEquals(1, element2.getItemCount(SubCell.SOUTH_WEST));
		assertEquals(sword, element2.getItems(SubCell.SOUTH_WEST).iterator()
				.next());
	}

	public void testItemsFallingThroughAlreadyOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, true);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Torch torch = new Torch();
		final Food apple = new Food(Item.Type.APPLE);
		final Weapon sword = new Weapon(Item.Type.SWORD);
		final WaterFlask waterFlask = new WaterFlask();

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getItemCount());
		assertEquals(0, element2.getItemCount(SubCell.NORTH_EAST));
		assertEquals(0, element2.getItemCount(SubCell.NORTH_WEST));
		assertEquals(0, element2.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(0, element2.getItemCount(SubCell.SOUTH_WEST));

		element1.itemDroppedDown(torch, SubCell.NORTH_WEST);
		element1.itemDroppedDown(apple, SubCell.NORTH_EAST);
		element1.itemDroppedDown(sword, SubCell.SOUTH_WEST);
		element1.itemDroppedDown(waterFlask, SubCell.SOUTH_EAST);

		// --- L'oubliette s'ouvre, les objets sont tombés au niveau inférieur
		assertEquals(0, element1.getItemCount());
		assertEquals(0, element1.getItemCount(SubCell.NORTH_EAST));
		assertEquals(0, element1.getItemCount(SubCell.NORTH_WEST));
		assertEquals(0, element1.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(0, element1.getItemCount(SubCell.SOUTH_WEST));

		assertEquals(4, element2.getItemCount());
		assertEquals(1, element2.getItemCount(SubCell.NORTH_EAST));
		assertEquals(apple, element2.getItems(SubCell.NORTH_EAST).iterator()
				.next());
		assertEquals(1, element2.getItemCount(SubCell.NORTH_WEST));
		assertEquals(torch, element2.getItems(SubCell.NORTH_WEST).iterator()
				.next());
		assertEquals(1, element2.getItemCount(SubCell.SOUTH_EAST));
		assertEquals(waterFlask, element2.getItems(SubCell.SOUTH_EAST)
				.iterator().next());
		assertEquals(1, element2.getItemCount(SubCell.SOUTH_WEST));
		assertEquals(sword, element2.getItems(SubCell.SOUTH_WEST).iterator()
				.next());
	}

	public void testCreaturesOfSizeOneFallingThroughAlreadyOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, true);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Creature mummy = new Creature(Creature.Type.MUMMY, 10);
		final Creature trolin = new Creature(Creature.Type.TROLIN, 10);
		final Creature rockPile = new Creature(Creature.Type.ROCK_PILE, 10);
		final Creature giggler = new Creature(Creature.Type.GIGGLER, 10);

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		element1.creatureSteppedOn(mummy, SubCell.NORTH_WEST);
		element1.creatureSteppedOn(trolin, SubCell.NORTH_EAST);
		element1.creatureSteppedOn(rockPile, SubCell.SOUTH_WEST);
		element1.creatureSteppedOn(giggler, SubCell.SOUTH_EAST);

		// --- L'oubliette était ouverte, les créatures sont tombés au niveau
		// inférieur
		assertEquals(0, element1.getCreatureCount());
		assertNull(element1.getCreature(SubCell.NORTH_EAST));
		assertNull(element1.getCreature(SubCell.NORTH_WEST));
		assertNull(element1.getCreature(SubCell.SOUTH_EAST));
		assertNull(element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(4, element2.getCreatureCount());
		assertEquals(trolin, element2.getCreature(SubCell.NORTH_EAST));
		assertEquals(mummy, element2.getCreature(SubCell.NORTH_WEST));
		assertEquals(giggler, element2.getCreature(SubCell.SOUTH_EAST));
		assertEquals(rockPile, element2.getCreature(SubCell.SOUTH_WEST));
	}

	public void testCreaturesOfSizeTwoFallingThroughAlreadyOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, true);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Creature worm = new Creature(Creature.Type.MAGENTA_WORM, 10);
		final Creature painRat = new Creature(Creature.Type.PAIN_RAT, 10);

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		element1.creatureSteppedOn(worm, Direction.NORTH);
		element1.creatureSteppedOn(painRat, Direction.SOUTH);

		// --- L'oubliette était ouverte, les créatures sont tombés au niveau
		// inférieur
		assertEquals(0, element1.getCreatureCount());
		assertNull(element1.getCreature(SubCell.NORTH_EAST));
		assertNull(element1.getCreature(SubCell.NORTH_WEST));
		assertNull(element1.getCreature(SubCell.SOUTH_EAST));
		assertNull(element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(2, element2.getCreatureCount());
		assertEquals(worm, element2.getCreature(SubCell.NORTH_EAST));
		assertEquals(worm, element2.getCreature(SubCell.NORTH_WEST));
		assertEquals(painRat, element2.getCreature(SubCell.SOUTH_EAST));
		assertEquals(painRat, element2.getCreature(SubCell.SOUTH_WEST));
	}

	public void testCreaturesOfSizeFourFallingThroughAlreadyOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, true);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Creature dragon = new Creature(Creature.Type.RED_DRAGON, 10);

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		element1.creatureSteppedOn(dragon);

		// --- L'oubliette était ouverte, les créatures sont tombés au niveau
		// inférieur
		assertEquals(0, element1.getCreatureCount());
		assertNull(element1.getCreature(SubCell.NORTH_EAST));
		assertNull(element1.getCreature(SubCell.NORTH_WEST));
		assertNull(element1.getCreature(SubCell.SOUTH_EAST));
		assertNull(element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(1, element2.getCreatureCount());
		assertEquals(dragon, element2.getCreature(SubCell.NORTH_EAST));
		assertEquals(dragon, element2.getCreature(SubCell.NORTH_WEST));
		assertEquals(dragon, element2.getCreature(SubCell.SOUTH_EAST));
		assertEquals(dragon, element2.getCreature(SubCell.SOUTH_WEST));
	}

	public void testCreaturesOfSizeOneFallingThroughPitWhenOpened() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, false);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Creature mummy = new Creature(Creature.Type.MUMMY, 10);
		final Creature trolin = new Creature(Creature.Type.TROLIN, 10);
		final Creature rockPile = new Creature(Creature.Type.ROCK_PILE, 10);
		final Creature giggler = new Creature(Creature.Type.GIGGLER, 10);

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		element1.creatureSteppedOn(mummy, SubCell.NORTH_WEST);
		element1.creatureSteppedOn(trolin, SubCell.NORTH_EAST);
		element1.creatureSteppedOn(rockPile, SubCell.SOUTH_WEST);
		element1.creatureSteppedOn(giggler, SubCell.SOUTH_EAST);

		// --- Situation initiale
		assertEquals(4, element1.getCreatureCount());
		assertEquals(trolin, element1.getCreature(SubCell.NORTH_EAST));
		assertEquals(mummy, element1.getCreature(SubCell.NORTH_WEST));
		assertEquals(giggler, element1.getCreature(SubCell.SOUTH_EAST));
		assertEquals(rockPile, element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		// --- L'oubliette s'ouvre, les créatures tombent au niveau inférieur
		assertTrue(pit.open());

		assertEquals(0, element1.getCreatureCount());
		assertNull(element1.getCreature(SubCell.NORTH_EAST));
		assertNull(element1.getCreature(SubCell.NORTH_WEST));
		assertNull(element1.getCreature(SubCell.SOUTH_EAST));
		assertNull(element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(4, element2.getCreatureCount());
		assertEquals(trolin, element2.getCreature(SubCell.NORTH_EAST));
		assertEquals(mummy, element2.getCreature(SubCell.NORTH_WEST));
		assertEquals(giggler, element2.getCreature(SubCell.SOUTH_EAST));
		assertEquals(rockPile, element2.getCreature(SubCell.SOUTH_WEST));
	}

	public void testCreaturesOfSizeTwoFallingThroughPitWhenOpened() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, false);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Creature worm = new Creature(Creature.Type.MAGENTA_WORM, 10);
		final Creature painRat = new Creature(Creature.Type.PAIN_RAT, 10);

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		element1.creatureSteppedOn(worm, Direction.NORTH);
		element1.creatureSteppedOn(painRat, Direction.SOUTH);

		// --- Situation initiale
		assertEquals(2, element1.getCreatureCount());
		assertEquals(worm, element1.getCreature(SubCell.NORTH_EAST));
		assertEquals(worm, element1.getCreature(SubCell.NORTH_WEST));
		assertEquals(painRat, element1.getCreature(SubCell.SOUTH_EAST));
		assertEquals(painRat, element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		// --- L'oubliette s'ouvre, les créatures tombent au niveau inférieur
		assertTrue(pit.open());

		assertEquals(0, element1.getCreatureCount());
		assertNull(element1.getCreature(SubCell.NORTH_EAST));
		assertNull(element1.getCreature(SubCell.NORTH_WEST));
		assertNull(element1.getCreature(SubCell.SOUTH_EAST));
		assertNull(element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(2, element2.getCreatureCount());
		assertEquals(worm, element2.getCreature(SubCell.NORTH_EAST));
		assertEquals(worm, element2.getCreature(SubCell.NORTH_WEST));
		assertEquals(painRat, element2.getCreature(SubCell.SOUTH_EAST));
		assertEquals(painRat, element2.getCreature(SubCell.SOUTH_WEST));
	}

	public void testCreaturesOfSizeFourFallingThroughPitWhenOpened() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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
		final Pit pit = new Pit(false, false);
		level1.setElement(3, 2, pit);

		final Level level2 = dungeon.createLevel(2, 5, 5);

		final Creature dragon = new Creature(Creature.Type.RED_DRAGON, 10);

		final Element element1 = level1.getElement(3, 2);
		final Element element2 = level2.getElement(3, 2);

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		element1.creatureSteppedOn(dragon);

		// --- Situation initiale
		assertEquals(1, element1.getCreatureCount());
		assertEquals(dragon, element1.getCreature(SubCell.NORTH_EAST));
		assertEquals(dragon, element1.getCreature(SubCell.NORTH_WEST));
		assertEquals(dragon, element1.getCreature(SubCell.SOUTH_EAST));
		assertEquals(dragon, element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(0, element2.getCreatureCount());
		assertNull(element2.getCreature(SubCell.NORTH_EAST));
		assertNull(element2.getCreature(SubCell.NORTH_WEST));
		assertNull(element2.getCreature(SubCell.SOUTH_EAST));
		assertNull(element2.getCreature(SubCell.SOUTH_WEST));

		// --- L'oubliette s'ouvre, les créatures tombent au niveau inférieur
		assertTrue(pit.open());

		assertEquals(0, element1.getCreatureCount());
		assertNull(element1.getCreature(SubCell.NORTH_EAST));
		assertNull(element1.getCreature(SubCell.NORTH_WEST));
		assertNull(element1.getCreature(SubCell.SOUTH_EAST));
		assertNull(element1.getCreature(SubCell.SOUTH_WEST));

		assertEquals(1, element2.getCreatureCount());
		assertEquals(dragon, element2.getCreature(SubCell.NORTH_EAST));
		assertEquals(dragon, element2.getCreature(SubCell.NORTH_WEST));
		assertEquals(dragon, element2.getCreature(SubCell.SOUTH_EAST));
		assertEquals(dragon, element2.getCreature(SubCell.SOUTH_WEST));
	}
	
	public void testChampionsHurtWhenFallingThroughOpenPit() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
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


		final Dungeon dungeon = new Dungeon();
		
		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, new Pit());

		dungeon.createLevel(2, 5, 5);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(
				Name.TIGGY);
		tiggy.getStats().getHealth().maxValue(500);
		tiggy.getStats().getHealth().value(500);
		
		final Champion daroou = ChampionFactory.getFactory().newChampion(
				Name.DAROOU);
		daroou.getStats().getHealth().maxValue(500);
		daroou.getStats().getHealth().value(500);
		
		final Champion halk = ChampionFactory.getFactory().newChampion(
				Name.HALK);
		halk.getStats().getHealth().maxValue(500);
		halk.getStats().getHealth().value(500);
		
		final Champion wuuf = ChampionFactory.getFactory().newChampion(
				Name.WUUF);
		wuuf.getStats().getHealth().maxValue(500);
		wuuf.getStats().getHealth().value(500);
		
		final Party party = new Party();
		party.addChampion(tiggy);
		party.addChampion(daroou);
		party.addChampion(halk);
		party.addChampion(wuuf);

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Situation initiale
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// --- Le groupe tombe à travers l'oubliette
		final int tiggyHealth = tiggy.getStats().getHealth().value();
		final int daroouHealth = daroou.getStats().getHealth().value();
		final int halkHealth = halk.getStats().getHealth().value();
		final int wuufHealth = wuuf.getStats().getHealth().value();
		
		assertTrue(dungeon.moveParty(Move.RIGHT, true));
		assertEquals(new Position(3, 2, 2), dungeon.getParty().getPosition());
		
		// --- Les champions doivent avoir perdu de la vie
		assertTrue(tiggy.getStats().getHealth().value().intValue() < tiggyHealth);
		assertTrue(daroou.getStats().getHealth().value().intValue() < daroouHealth);
		assertTrue(halk.getStats().getHealth().value().intValue() < halkHealth);
		assertTrue(wuuf.getStats().getHealth().value().intValue() < wuufHealth);
	}
}