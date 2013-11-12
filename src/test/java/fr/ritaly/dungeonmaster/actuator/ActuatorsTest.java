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
package fr.ritaly.dungeonmaster.actuator;

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;

public class ActuatorsTest extends TestCase {

	private final class TestTriggered implements Triggerable {
		@Override
		public void trigger(TriggerAction action) {
			// Do nothing
		}
	}

	public void testCombineNullNull() {
		// Combining 2 null actuators returns null
		assertNull(Actuators.combine(null, null));
	}

	public void testCombineNullNotNull() {
		// Combining null with one actuator returns the actuator
		final SimpleActuator actuator = new SimpleActuator(1, TriggerAction.ENABLE, new TestTriggered());

		assertEquals(actuator, Actuators.combine(null, actuator));
	}

	public void testCombineNotNullNull() {
		// Combining one actuator with null returns the actuator
		final SimpleActuator actuator = new SimpleActuator(1, TriggerAction.ENABLE, new TestTriggered());

		assertEquals(actuator, Actuators.combine(actuator, null));
	}

	public void testCombineTwoActuators() {
		// Combining two actuators returns a sequential actuator
		final SimpleActuator actuator1 = new SimpleActuator(1, TriggerAction.ENABLE, new TestTriggered());
		final SimpleActuator actuator2 = new SimpleActuator(1, TriggerAction.DISABLE, new TestTriggered());

		final Actuator combined = Actuators.combine(actuator1, actuator2);

		assertNotNull(combined);
		assertTrue(combined instanceof SequentialActuator);

		final SequentialActuator actuator = (SequentialActuator) combined;

		assertNotNull(actuator.getActuators());
		assertEquals(2, actuator.getActuators().size());
		assertEquals(actuator1, actuator.getActuators().get(0));
		assertEquals(actuator2, actuator.getActuators().get(1));
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}