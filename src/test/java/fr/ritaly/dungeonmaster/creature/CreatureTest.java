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

import fr.ritaly.dungeonmaster.ai.AttackType;
import fr.ritaly.dungeonmaster.ai.Creature;
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
}