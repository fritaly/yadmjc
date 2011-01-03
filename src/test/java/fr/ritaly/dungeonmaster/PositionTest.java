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

	public void testGetDirectionTowards() throws Exception {
		// +---+---+---+---+---+
		// | . | 1 | N | 2 | . |
		// +---+---+---+---+---+
		// | 8 | . | . | . | 3 |
		// +---+---+---+---+---+
		// | W | . | X | . | E |
		// +---+---+---+---+---+
		// | 7 | . | . | . | 4 |
		// +---+---+---+---+---+
		// | . | 6 | S | 5 | . |
		// +---+---+---+---+---+

		final Position positionX = new Position(2, 2, 1);
		final Position positionN = new Position(2, 0, 1);
		final Position positionS = new Position(2, 4, 1);
		final Position positionW = new Position(0, 2, 1);
		final Position positionE = new Position(4, 2, 1);
		
		assertEquals(Direction.NORTH, positionX.getDirectionTowards(positionN));
		assertEquals(Direction.SOUTH, positionX.getDirectionTowards(positionS));
		assertEquals(Direction.WEST, positionX.getDirectionTowards(positionW));
		assertEquals(Direction.EAST, positionX.getDirectionTowards(positionE));
		
		final Position position1 = new Position(1, 0, 1);
		final Position position2 = new Position(3, 0, 1);
		final Position position3 = new Position(4, 1, 1);
		final Position position4 = new Position(4, 3, 1);
		final Position position5 = new Position(3, 4, 1);
		final Position position6 = new Position(1, 4, 1);
		final Position position7 = new Position(0, 3, 1);
		final Position position8 = new Position(0, 1, 1);
		
		assertEquals(Direction.NORTH, positionX.getDirectionTowards(position1));
		assertEquals(Direction.NORTH, positionX.getDirectionTowards(position2));
		assertEquals(Direction.EAST, positionX.getDirectionTowards(position3));
		assertEquals(Direction.EAST, positionX.getDirectionTowards(position4));
		assertEquals(Direction.SOUTH, positionX.getDirectionTowards(position5));
		assertEquals(Direction.SOUTH, positionX.getDirectionTowards(position6));
		assertEquals(Direction.WEST, positionX.getDirectionTowards(position7));
		assertEquals(Direction.WEST, positionX.getDirectionTowards(position8));
	}
	
	public void testGetSurroundingPositionsInt() throws Exception {
		// 1.
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		// | 1 | P | 1 |
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		
		assertEquals(8, new Position(1,1,1).getSurroundingPositions(1).size());
		
		// 2.
		// +---+---+---+---+---+
		// |   | 2 | 2 | 2 |   |
		// +---+---+---+---+---+
		// | 2 | 1 | 1 | 1 | 2 |
		// +---+---+---+---+---+
		// | 2 | 1 | P | 1 | 2 |
		// +---+---+---+---+---+
		// | 2 | 1 | 1 | 1 | 2 |
		// +---+---+---+---+---+
		// |   | 2 | 2 | 2 |   |
		// +---+---+---+---+---+
		
		assertEquals(20, new Position(1,1,1).getSurroundingPositions(2).size());
		
		// 3.
		// +---+---+---+---+---+---+---+
		// |   |   | 3 | 3 | 3 |   |   |
		// +---+---+---+---+---+---+---+
		// |   | 3 | 2 | 2 | 2 | 3 |   |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | P | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// |   | 3 | 2 | 2 | 2 | 3 |   |
		// +---+---+---+---+---+---+---+
		// |   |   | 3 | 3 | 3 |   |   |
		// +---+---+---+---+---+---+---+
		
		assertEquals(36, new Position(1,1,1).getSurroundingPositions(3).size());
		
		// 4.
		// +---+---+---+---+---+---+---+---+---+
		// |   |   | 4 | 4 | 4 | 4 | 4 |   |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   | 4 | 4 | 3 | 3 | 3 | 4 | 4 |   |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 4 | 3 | 2 | 2 | 2 | 3 | 4 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 3 | 2 | 1 | P | 1 | 2 | 3 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 4 | 3 | 2 | 2 | 2 | 3 | 4 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// |   | 4 | 4 | 3 | 3 | 3 | 4 | 4 |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   |   | 4 | 4 | 4 | 4 | 4 |   |   |
		// +---+---+---+---+---+---+---+---+---+
		
		assertEquals(68, new Position(1,1,1).getSurroundingPositions(4).size());
		
		// 5.
		// +---+---+---+---+---+---+---+---+---+---+---+
		// |   |   |   | 5 | 5 | 5 | 5 | 5 |   |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// |   |   | 5 | 4 | 4 | 4 | 4 | 4 | 5 |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// |   | 5 | 4 | 3 | 3 | 3 | 3 | 3 | 4 | 5 |   |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// | 5 | 4 | 3 | 2 | 2 | 2 | 2 | 2 | 3 | 4 | 5 |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// | 5 | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 | 5 |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// | 5 | 4 | 3 | 2 | 1 | P | 1 | 2 | 3 | 4 | 5 |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// | 5 | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 | 5 |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// | 5 | 4 | 3 | 2 | 2 | 2 | 2 | 2 | 3 | 4 | 5 |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// |   | 5 | 4 | 3 | 3 | 3 | 3 | 3 | 4 | 5 |   |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// |   |   | 5 | 4 | 4 | 4 | 4 | 4 | 5 |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+
		// |   |   |   | 5 | 5 | 5 | 5 | 5 |   |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+
		
		assertEquals(96, new Position(1,1,1).getSurroundingPositions(5).size());
		
		// 6.
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   |   |   |   | 6 | 6 | 6 | 6 | 6 |   |   |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   |   | 6 | 5 | 5 | 5 | 5 | 5 | 5 | 5 | 6 |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   | 6 | 5 | 5 | 4 | 4 | 4 | 4 | 4 | 5 | 5 | 6 |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   | 5 | 5 | 4 | 3 | 3 | 3 | 3 | 3 | 4 | 5 | 5 |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// | 6 | 5 | 4 | 3 | 2 | 2 | 2 | 2 | 2 | 3 | 4 | 5 | 6 |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// | 6 | 5 | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 | 5 | 6 |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// | 6 | 5 | 4 | 3 | 2 | 1 | P | 1 | 2 | 3 | 4 | 5 | 6 |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// | 6 | 5 | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 | 5 | 6 |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// | 6 | 5 | 4 | 3 | 2 | 2 | 2 | 2 | 2 | 3 | 4 | 5 | 6 |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   | 5 | 5 | 4 | 3 | 3 | 3 | 3 | 3 | 4 | 5 | 5 |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   | 6 | 5 | 5 | 4 | 4 | 4 | 4 | 4 | 5 | 5 | 6 |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   |   | 6 | 5 | 5 | 5 | 5 | 5 | 5 | 5 | 6 |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		// |   |   |   |   | 6 | 6 | 6 | 6 | 6 |   |   |   |   |
		// +---+---+---+---+---+---+---+---+---+---+---+---+---+
		
		assertEquals(136, new Position(1,1,1).getSurroundingPositions(6).size());
	}
}