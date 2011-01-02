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

public class TemporizerTest extends TestCase {

	public TemporizerTest(String name) {
		super(name);
	}

	public void testNegativeCount() {
		try {
			Temporizer temporizer = new Temporizer("Test", -1);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	public void testNullCount() {
		try {
			Temporizer temporizer = new Temporizer("Test", 0);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	public void testPositiveCount() {
		Temporizer temporizer = new Temporizer("Test", 2);

		for (int i = 0; i < 5; i++) {
			assertFalse(temporizer.trigger());
			assertTrue(temporizer.trigger());
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}