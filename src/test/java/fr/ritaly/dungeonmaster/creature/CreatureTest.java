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
package fr.ritaly.dungeonmaster.creature;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.ai.AttackType;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;

public class CreatureTest extends TestCase {

	public CreatureTest() {
	}

	public CreatureTest(String name) {
		super(name);
	}

	public void testSpellMethodsMustBeConsistent() {
		for (Creature.Type type : Creature.Type.values()) {
			// Si une créature peut lancer des sorts alors la liste de sorts
			// associée ne doit pas être vide
			if (type.canCastSpell()) {
				assertFalse(type.getSpells().isEmpty());
			} else {
				assertTrue(type.getSpells().isEmpty());
			}
		}
	}

	public void testCreatureIsAlive() {
		final Creature creature = new Creature(Creature.Type.MUMMY, 1);
		
		assertTrue(creature.isAlive());
	}
	
	public void testCreatureCanBeKilled() {
		final Creature creature = new Creature(Creature.Type.MUMMY, 1);
		
		assertTrue(creature.isAlive());
		assertFalse(creature.isDead());
		
		while (creature.isAlive()) {
			final int health = creature.getHealth();
			
			final int hitPoints = creature.hit(AttackType.NORMAL);
			
			assertTrue(hitPoints > 0);
			assertEquals(hitPoints, health - creature.getHealth());
		}
		
		assertFalse(creature.isAlive());
		assertTrue(creature.isDead());
	}
	
	public void testZytazMateriality() {
		final Creature creature = new Creature(Creature.Type.ZYTAZ, 1);
		
		Materiality materiality = creature.getMateriality();
		int count = 0;
		
		for (int i = 0; i < 20; i++) {
			Clock.getInstance().tick();
			
			if (materiality != creature.getMateriality()) {
				count++;
				
				materiality = creature.getMateriality();
			}
		}
		
		assertTrue(count > 0);
	}
	
	public void testMummyMateriality() {
		final Creature creature = new Creature(Creature.Type.MUMMY, 1);
		
		Materiality materiality = creature.getMateriality();
		int count = 0;
		
		for (int i = 0; i < 20; i++) {
			Clock.getInstance().tick();
			
			if (materiality != creature.getMateriality()) {
				count++;
				
				materiality = creature.getMateriality();
			}
		}
		
		assertTrue(count == 0);
	}
	
	public void testInvincibleCreatureCantBeHurt() {
		final Creature creature = new Creature(Creature.Type.LORD_CHAOS, 1);
		
		assertTrue(creature.isInvincible());
		
		for (int i = 0; i < 10; i++) {
			assertEquals(0, creature.hit(AttackType.CRITICAL));
		}
	}
	
	private void assertOneCreature(Element element, Creature creature) {
		assertTrue(element.hasCreatures());
		assertEquals(1, element.getCreatureCount());
		assertEquals(creature, element.getCreatures().iterator().next());
	}
	
	private void assertNoCreature(Element element) {
		assertFalse(element.hasCreatures());
		assertEquals(0, element.getCreatureCount());
	}
	
	public void testCreatureWhenIdle() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | D | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		final Element element = level1.getElement(2, 2);
		
		final Creature dragon = new Creature(Creature.Type.RED_DRAGON, 1);
		element.addCreature(dragon);
		
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				if ((x == 2) && (y == 2)) {
					// (2,2) a 1 créature
					assertOneCreature(element, dragon);					
				} else {
					assertNoCreature(level1.getElement(x, y));
				}
			}
		}
		
		assertEquals(Creature.State.IDLE, dragon.getState());
		assertTrue(element.equals(dragon.getElement()));
		
		// Laisser le temps passer (suffisamment pour que le dragon se déplace)
		Clock.getInstance().tick(dragon.getMoveDuration());
		
		// Le dragon doit avoir changé d'état et changé de place
		assertEquals(Creature.State.PATROLLING, dragon.getState());
		assertFalse(element.equals(dragon.getElement()));
	}
	
	public void testCreatureCanSeePosition() {
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | 3 | 3 | 3 | 3 | 3 | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | 2 | 2 | 2 | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | 1 | 1 | 1 | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | D | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 9, 9);
		final Element element = level1.getElement(4, 7);
		
		final Creature dragon = new Creature(Creature.Type.RED_DRAGON, 1);
		dragon.setDirection(Direction.NORTH);
		element.addCreature(dragon);
		
		final List<Position> visiblePositions = new ArrayList<Position>();
		visiblePositions.add(new Position(3, 6, 1)); // 1
		visiblePositions.add(new Position(4, 6, 1)); // 1
		visiblePositions.add(new Position(5, 6, 1)); // 1
		visiblePositions.add(new Position(3, 5, 1)); // 2
		visiblePositions.add(new Position(4, 5, 1)); // 2
		visiblePositions.add(new Position(5, 5, 1)); // 2
		visiblePositions.add(new Position(2, 4, 1)); // 3
		visiblePositions.add(new Position(3, 4, 1)); // 3
		visiblePositions.add(new Position(4, 4, 1)); // 3
		visiblePositions.add(new Position(5, 4, 1)); // 3
		visiblePositions.add(new Position(6, 4, 1)); // 3
		
		assertEquals(Direction.NORTH, dragon.getDirection());
		
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				final Position position = new Position(x, y, 1);
				
				if (visiblePositions.contains(position)) {
					assertTrue(dragon.canSeePosition(position));
				} else {
					assertFalse("Creature can't see position " + position,
							dragon.canSeePosition(position));
				}
			}
		}
	}
	
	public void testMummyCanAttackPosition() {
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | 1 | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | 1 | M | 1 | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | 1 | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 9, 9);
		final Element element = level1.getElement(4, 4);
		
		final Creature mummy = new Creature(Creature.Type.MUMMY, 1);
		element.addCreature(mummy);
		
		final List<Position> attackablePositions = new ArrayList<Position>();
		attackablePositions.add(new Position(3, 4, 1)); // 1
		attackablePositions.add(new Position(5, 4, 1)); // 1
		attackablePositions.add(new Position(4, 3, 1)); // 1
		attackablePositions.add(new Position(4, 5, 1)); // 1
		
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				final Position position = new Position(x, y, 1);
				
				if (attackablePositions.contains(position)) {
					assertTrue(mummy.canAttackPosition(position));
				} else {
					assertFalse("Creature can't attack position " + position,
							mummy.canAttackPosition(position));
				}
			}
		}
	}
	
	public void testDragonCanAttackPosition() {
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | 1 | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | 1 | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | 2 | 2 | D | 2 | 2 | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | 1 | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | 1 | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 9, 9);
		final Element element = level1.getElement(4, 4);
		
		final Creature dragon = new Creature(Creature.Type.RED_DRAGON, 1);
		element.addCreature(dragon);
		
		final List<Position> attackablePositions = new ArrayList<Position>();
		attackablePositions.add(new Position(4, 2, 1)); // 1
		attackablePositions.add(new Position(4, 3, 1)); // 1
		attackablePositions.add(new Position(4, 5, 1)); // 1
		attackablePositions.add(new Position(4, 6, 1)); // 1
		attackablePositions.add(new Position(2, 4, 1)); // 2
		attackablePositions.add(new Position(3, 4, 1)); // 2
		attackablePositions.add(new Position(5, 4, 1)); // 2
		attackablePositions.add(new Position(6, 4, 1)); // 2
		
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				final Position position = new Position(x, y, 1);
				
				if (attackablePositions.contains(position)) {
					assertTrue("Creature can't attack position " + position,
							dragon.canAttackPosition(position));
				} else {
					assertFalse("Creature can attack position " + position,
							dragon.canAttackPosition(position));
				}
			}
		}
	}
	
	public void testScorpionMustMoveTowardsParty() {
		/*
		 * Le scorpion voit les champions en face de lui. Il doit se rapprocher
		 * d'eux pour attaquer au contact 
		 */
		
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | P | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | S | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 9, 9);
		final Element element = level1.getElement(4, 4);
		
		final Creature scorpion = new Creature(Creature.Type.GIANT_SCORPION, 1);
		element.addCreature(scorpion);

		final Party party = new Party(ChampionFactory.getFactory().newChampion(
				Name.WUUF));
		
		dungeon.setParty(4, 1, 1, party);
		
		// --- Etat initial
		assertEquals(Creature.State.IDLE, scorpion.getState());
		assertEquals(Direction.NORTH, scorpion.getDirection());
		assertTrue(scorpion.canSeePosition(party.getPosition()));
		assertEquals(new Position(4, 4, 1), scorpion.getElement().getPosition());
		
		// --- On laisse la créature se déplacer une fois (vers les champions)
		Clock.getInstance().tick(Creature.Type.GIANT_SCORPION.getMoveDuration());
		
		// --- Contrôles sur nouvel état
		assertEquals(Creature.State.TRACKING, scorpion.getState());
		assertEquals(Direction.NORTH, scorpion.getDirection());
		assertTrue(scorpion.canSeePosition(party.getPosition()));
		assertEquals(new Position(4, 3, 1), scorpion.getElement().getPosition());
		
		// --- On laisse la créature se déplacer une fois (vers les champions)
		Clock.getInstance().tick(Creature.Type.GIANT_SCORPION.getMoveDuration());
		
		// --- Contrôles sur nouvel état
		assertEquals(Creature.State.ATTACKING, scorpion.getState());
		assertEquals(Direction.NORTH, scorpion.getDirection());
		assertTrue(scorpion.canSeePosition(party.getPosition()));
		assertEquals(new Position(4, 2, 1), scorpion.getElement().getPosition());
	}
	
	public void testDragonMustMoveAndTurnTowardsParty() {
		/*
		 * Le dragon regard au Nord. Il ne voit pas les champions mais doit les
		 * détecter (awareness) et se mettre à les chasser. Pour cela, sa
		 * direction doit changer pour pointer vers les champions. Quand il
		 * parvient à portée, il attaque à distance par une boule de feu.
		 */
		
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | D | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | P | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 9, 9);
		final Element element = level1.getElement(4, 1);
		
		final Creature dragon = new Creature(Creature.Type.RED_DRAGON, 1);
		element.addCreature(dragon);

		final Party party = new Party(ChampionFactory.getFactory().newChampion(
				Name.WUUF));
		
		dungeon.setParty(4, 6, 1, party);
		
		// --- Etat initial
		assertEquals(Creature.State.IDLE, dragon.getState());
		assertEquals(Direction.NORTH, dragon.getDirection());
		assertFalse(dragon.canSeePosition(party.getPosition()));
		assertEquals(new Position(4, 1, 1), dragon.getElement().getPosition());
		
		// --- On laisse la créature se déplacer une fois (vers les champions)
		Clock.getInstance().tick(Creature.Type.RED_DRAGON.getMoveDuration());
		
		// --- Contrôles sur nouvel état
		assertEquals(Creature.State.TRACKING, dragon.getState());
		assertEquals(Direction.SOUTH, dragon.getDirection()); // <--- !!!
		assertFalse(dragon.canSeePosition(party.getPosition()));
		assertEquals(new Position(4, 2, 1), dragon.getElement().getPosition());
		
		// --- On laisse la créature se déplacer une fois (vers les champions)
		Clock.getInstance().tick(Creature.Type.RED_DRAGON.getMoveDuration());
		
		// --- Contrôles sur nouvel état
		assertEquals(Creature.State.TRACKING, dragon.getState());
		assertEquals(Direction.SOUTH, dragon.getDirection());
		assertTrue(dragon.canSeePosition(party.getPosition())); // <--- !!!
		assertEquals(new Position(4, 3, 1), dragon.getElement().getPosition());
		
		// --- On laisse la créature se déplacer une fois (vers les champions)
		Clock.getInstance().tick(Creature.Type.RED_DRAGON.getMoveDuration());
		
		// --- Contrôles sur nouvel état
		assertEquals(Creature.State.ATTACKING, dragon.getState()); // <--- !!!
		assertEquals(Direction.SOUTH, dragon.getDirection());
		assertTrue(dragon.canSeePosition(party.getPosition()));
		assertEquals(new Position(4, 4, 1), dragon.getElement().getPosition());
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}