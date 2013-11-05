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
package fr.ritaly.dungeonmaster.champion;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.champion.Champion.Level;

public class LevelTest extends TestCase {

	public LevelTest() {
	}

	public LevelTest(String name) {
		super(name);
	}

	private static void testLevel(Level level, int lowerBound, int upperBound) {
		assertEquals(lowerBound, level.getLowerBound());
		assertEquals(upperBound, level.getUpperBound());
	}

	public void testRanges() throws Exception {
		testLevel(Level.NONE, 0, 500);
		testLevel(Level.NEOPHYTE, 500, 1000);
		testLevel(Level.NOVICE, 1000, 2000);
		testLevel(Level.APPRENTICE, 2000, 4000);
		testLevel(Level.JOURNEYMAN, 4000, 8000);
		testLevel(Level.CRAFTSMAN, 8000, 16000);
		testLevel(Level.ARTISAN, 16000, 32000);
		testLevel(Level.ADEPT, 32000, 64000);
		testLevel(Level.EXPERT, 64000, 128000);
		testLevel(Level.LO_MASTER, 128000, 256000);
		testLevel(Level.UM_MASTER, 256000, 512000);
		testLevel(Level.ON_MASTER, 512000, 1024000);
		testLevel(Level.EE_MASTER, 1024000, 2048000);
		testLevel(Level.PAL_MASTER, 2048000, 4096000);
		testLevel(Level.MON_MASTER, 4096000, 8192000);
		testLevel(Level.ARCH_MASTER, 8192000, 16384000);
	}
}