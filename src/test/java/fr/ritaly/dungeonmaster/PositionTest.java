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

import junit.framework.TestCase;

public class PositionTest extends TestCase {

	public PositionTest(String name) {
		super(name);
	}

	public void testGetDirection() throws Exception {
		// +---+---+---+---+---+
		// | . | . | N | . | . |
		// +---+---+---+---+---+
		// | . | . | . | . | . |
		// +---+---+---+---+---+
		// | W | . | X | . | E |
		// +---+---+---+---+---+
		// | . | . | . | . | . |
		// +---+---+---+---+---+
		// | . | . | S | . | . |
		// +---+---+---+---+---+

		final Position positionX = new Position(2, 2, 1);
		final Position positionN = new Position(2, 0, 1);
		final Position positionS = new Position(2, 4, 1);
		final Position positionW = new Position(0, 2, 1);
		final Position positionE = new Position(4, 2, 1);
		
		assertEquals(Direction.NORTH, positionX.getDirection(positionN));
		assertEquals(Direction.SOUTH, positionX.getDirection(positionS));
		assertEquals(Direction.WEST, positionX.getDirection(positionW));
		assertEquals(Direction.EAST, positionX.getDirection(positionE));
	}
}