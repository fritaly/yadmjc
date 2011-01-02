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

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ai.AttackType;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;
import junit.framework.TestCase;

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
		
		boolean material = creature.isMaterial();
		int count = 0;
		
		for (int i = 0; i < 20; i++) {
			Clock.getInstance().tick();
			
			if (material != creature.isMaterial()) {
				count++;
				
				material = creature.isMaterial();
			}
		}
		
		assertTrue(count > 0);
	}
	
	public void testMummyMateriality() {
		final Creature creature = new Creature(Creature.Type.MUMMY, 1);
		
		boolean material = creature.isMaterial();
		int count = 0;
		
		for (int i = 0; i < 20; i++) {
			Clock.getInstance().tick();
			
			if (material != creature.isMaterial()) {
				count++;
				
				material = creature.isMaterial();
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
}