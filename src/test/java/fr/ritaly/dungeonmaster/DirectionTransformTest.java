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

public class DirectionTransformTest extends TestCase {

	public DirectionTransformTest(String name) {
		super(name);
	}

	public void testTransform_East() {
		final DirectionTransform transform = DirectionTransform.EAST;

		for (Direction direction : Direction.values()) {
			assertEquals(Direction.EAST, transform.transform(direction));
		}
	}

	public void testTransform_West() {
		final DirectionTransform transform = DirectionTransform.WEST;

		for (Direction direction : Direction.values()) {
			assertEquals(Direction.WEST, transform.transform(direction));
		}
	}

	public void testTransform_North() {
		final DirectionTransform transform = DirectionTransform.NORTH;

		for (Direction direction : Direction.values()) {
			assertEquals(Direction.NORTH, transform.transform(direction));
		}
	}

	public void testTransform_South() {
		final DirectionTransform transform = DirectionTransform.SOUTH;

		for (Direction direction : Direction.values()) {
			assertEquals(Direction.SOUTH, transform.transform(direction));
		}
	}

	public void testTransform_Identity() {
		final DirectionTransform transform = DirectionTransform.IDENTITY;

		for (Direction direction : Direction.values()) {
			assertEquals(direction, transform.transform(direction));
		}
	}

	public void testTransform_NextAntiClockWise() {
		final DirectionTransform transform = DirectionTransform.NEXT_ANTI_CLOCKWISE;

		assertEquals(Direction.DOWN, transform.transform(Direction.DOWN));
		assertEquals(Direction.NORTH, transform.transform(Direction.EAST));
		assertEquals(Direction.WEST, transform.transform(Direction.NORTH));
		assertEquals(Direction.EAST, transform.transform(Direction.SOUTH));
		assertEquals(Direction.UP, transform.transform(Direction.UP));
		assertEquals(Direction.SOUTH, transform.transform(Direction.WEST));
	}

	public void testTransform_NextClockWise() {
		final DirectionTransform transform = DirectionTransform.NEXT_CLOCKWISE;

		assertEquals(Direction.DOWN, transform.transform(Direction.DOWN));
		assertEquals(Direction.SOUTH, transform.transform(Direction.EAST));
		assertEquals(Direction.EAST, transform.transform(Direction.NORTH));
		assertEquals(Direction.WEST, transform.transform(Direction.SOUTH));
		assertEquals(Direction.UP, transform.transform(Direction.UP));
		assertEquals(Direction.NORTH, transform.transform(Direction.WEST));
	}

	public void testTransform_Opposite() {
		final DirectionTransform transform = DirectionTransform.OPPOSITE;

		assertEquals(Direction.UP, transform.transform(Direction.DOWN));
		assertEquals(Direction.WEST, transform.transform(Direction.EAST));
		assertEquals(Direction.SOUTH, transform.transform(Direction.NORTH));
		assertEquals(Direction.NORTH, transform.transform(Direction.SOUTH));
		assertEquals(Direction.DOWN, transform.transform(Direction.UP));
		assertEquals(Direction.EAST, transform.transform(Direction.WEST));
	}
}