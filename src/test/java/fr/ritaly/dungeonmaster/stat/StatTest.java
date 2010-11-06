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
package fr.ritaly.dungeonmaster.stat;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeListener;

public class StatTest extends TestCase {

	private final class Listener implements ChangeListener {
		private boolean notified;

		@Override
		public void onChangeEvent(ChangeEvent event) {
			notified = true;
		}
	}

	public StatTest(String name) {
		super(name);
	}

	public void testStatString() {
		final Stat stat = new Stat("Owner", "Name");

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 0);
		assertTrue(stat.getPrevious() == 0);
	}

	public void testStatStringInteger() {
		final Stat stat = new Stat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);
	}

	public void testStatStringIntegerIntegerInteger() {
		final Stat stat = new Stat("Owner", "Name", 10, 20);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);
	}

	public void testStatStringIntegerInteger() {
		final Stat stat = new Stat("Owner", "Name", 10, 0, 20);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);
	}

	public void testAddChangeListener() {
		final Listener listener = new Listener();

		final Stat stat = new Stat("Owner", "Name");
		stat.addChangeListener(listener);

		assertFalse(listener.notified);

		stat.value(1);

		assertTrue(listener.notified);
	}

	public void testRemoveChangeListener() {
		fail("Not yet implemented");
	}

	public void testGet() {
		final Stat stat = new Stat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.value(20);

		assertTrue(stat.value() == 20);
	}

	public void testInc() {
		final Stat stat = new Stat("Owner", "Name", 10, 30);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.inc(10);

		assertTrue(stat.value() == 20);

		// Il ne faut pas dépasser la valeur max définie
		stat.inc(20);

		assertTrue(stat.value() == 30);
	}

	public void testDec() {
		final Stat stat = new Stat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.dec(10);

		assertTrue(stat.value() == 0);

		// Il ne faut pas dépasser la valeur min définie
		stat.dec(20);

		assertTrue(stat.value() == 0);
	}

	public void testSet() {
		final Stat stat = new Stat("Owner", "Name", 10, 30);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.value(20);

		assertTrue(stat.value() == 20);

		// Il ne faut pas dépasser la valeur max définie
		stat.value(40);

		assertTrue(stat.value() == 30);

		// Il ne faut pas dépasser la valeur min définie
		stat.value(-10);

		assertTrue(stat.value() == 0);
	}

	public void testGetName() {
		final Stat stat = new Stat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
	}

	public void testGetPrevious() {
		final Stat stat = new Stat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);

		stat.value(20);

		assertTrue(stat.value() == 20);
		assertTrue(stat.getPrevious() == 10);

		stat.value(30);

		assertTrue(stat.value() == 30);
		assertTrue(stat.getPrevious() == 20);
	}
	
	public void testStatBoostWearsOff() {
		final Stat stat = new Stat("Test", "Stat", 1);

		// --- Vérifier l'état initial
		assertEquals(1, stat.actualValue().intValue());
		assertEquals(1, stat.value().intValue());
		assertEquals(0, stat.boostValue().intValue());

		// --- Augmenter le boost pour 6 tics
		stat.incBoost(10, 6);

		assertEquals(11, stat.actualValue().intValue()); // <---
		assertEquals(1, stat.value().intValue());
		assertEquals(10, stat.boostValue().intValue()); // <---

		// Attendre que l'effet du boost se dissipe
		Clock.getInstance().tick(5);

		assertEquals(11, stat.actualValue().intValue()); // <---
		assertEquals(1, stat.value().intValue());
		assertEquals(10, stat.boostValue().intValue());

		Clock.getInstance().tick();

		assertEquals(1, stat.actualValue().intValue()); // <---
		assertEquals(1, stat.value().intValue());
		assertEquals(0, stat.boostValue().intValue()); // <---
	}
}