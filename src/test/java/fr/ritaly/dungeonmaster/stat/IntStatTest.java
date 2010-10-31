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
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeListener;

public class IntStatTest extends TestCase {

	private final class Listener implements ChangeListener {
		private boolean notified;

		@Override
		public void onChangeEvent(ChangeEvent event) {
			notified = true;
		}
	}

	public IntStatTest(String name) {
		super(name);
	}

	public void testIntStatString() {
		final IntStat stat = new IntStat("Owner", "Name");

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 0);
		assertTrue(stat.getPrevious() == 0);
	}

	public void testIntStatStringInteger() {
		final IntStat stat = new IntStat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);
	}

	public void testIntStatStringIntegerIntegerInteger() {
		final IntStat stat = new IntStat("Owner", "Name", 10, 20);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);
	}

	public void testIntStatStringIntegerInteger() {
		final IntStat stat = new IntStat("Owner", "Name", 10, 0, 20);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);
		assertTrue(stat.getPrevious() == 10);
	}

	public void testAddChangeListener() {
		final Listener listener = new Listener();

		final IntStat stat = new IntStat("Owner", "Name");
		stat.addChangeListener(listener);

		assertFalse(listener.notified);

		stat.value(1);

		assertTrue(listener.notified);
	}

	public void testRemoveChangeListener() {
		fail("Not yet implemented");
	}

	public void testGet() {
		final IntStat stat = new IntStat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.value(20);

		assertTrue(stat.value() == 20);
	}

	public void testInc() {
		final IntStat stat = new IntStat("Owner", "Name", 10, 30);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.inc(10);

		assertTrue(stat.value() == 20);

		// Il ne faut pas dépasser la valeur max définie
		stat.inc(20);

		assertTrue(stat.value() == 30);
	}

	public void testDec() {
		final IntStat stat = new IntStat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
		assertTrue(stat.value() == 10);

		stat.dec(10);

		assertTrue(stat.value() == 0);

		// Il ne faut pas dépasser la valeur min définie
		stat.dec(20);

		assertTrue(stat.value() == 0);
	}

	public void testSet() {
		final IntStat stat = new IntStat("Owner", "Name", 10, 30);

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
		final IntStat stat = new IntStat("Owner", "Name", 10);

		assertEquals("Name", stat.getName());
	}

	public void testGetPrevious() {
		final IntStat stat = new IntStat("Owner", "Name", 10);

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
}
