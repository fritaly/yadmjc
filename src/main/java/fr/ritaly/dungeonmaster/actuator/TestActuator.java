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

/**
 * Implementation of {@link Actuator} meant for testing.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class TestActuator implements Actuator {

	/**
	 * Whether the actuator was triggered.
	 */
	private boolean triggered;

	@Override
	public boolean clockTicked() {
		triggered = true;

		// Stop listening to clock ticks
		return false;
	}

	@Override
	public String getLabel() {
		return "TestActuator";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new TestActuator();
	}

	public boolean isTriggered() {
		return triggered;
	}

	/**
	 * Resets the actuator to its initial state.
	 */
	public void reset() {
		triggered = false;
	}

	@Override
	public String toString() {
		return getLabel();
	}
}