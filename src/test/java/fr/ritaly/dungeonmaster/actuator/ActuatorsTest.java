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

import fr.ritaly.dungeonmaster.Clock;
import junit.framework.TestCase;

public class ActuatorsTest extends TestCase {

	private final class TestTriggered implements Triggered {
		@Override
		public void trigger(TriggerAction action) {
		}
	}

	public void testCombineNullNull() {
		assertNull(Actuators.combine(null, null));
	}

	public void testCombineNullNotNull() {
		final SimpleActuator actuator = new SimpleActuator(1,
				TriggerAction.ENABLE, new TestTriggered());

		assertEquals(actuator, Actuators.combine(null, actuator));
	}
	
	public void testCombineNotNullNull() {
		final SimpleActuator actuator = new SimpleActuator(1,
				TriggerAction.ENABLE, new TestTriggered());

		assertEquals(actuator, Actuators.combine(actuator, null));
	}
	
	@Override
	protected void setUp() throws Exception {
		// On nettoie l'horloge entre deux tests
		Clock.getInstance().reset();
	}
}