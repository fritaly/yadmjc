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
 * Utility class surrounding the use of actuators.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Actuators {

	/**
	 * Combines the two given {@link Actuator}s and returned the composed
	 * {@link Actuator}.
	 * 
	 * @param actuator1
	 *            an {@link Actuator} to be composed. Can be null.
	 * @param actuator2
	 *            another {@link Actuator} to be composed. Can be null.
	 * @return an {@link Actuator} representing the composed {@link Actuator} or
	 *         null.
	 */
	public static Actuator combine(Actuator actuator1, Actuator actuator2) {
		if ((actuator1 == null) && (actuator2 == null)) {
			return null;
		}
		if (actuator1 == null) {
			return actuator2;
		}
		if (actuator2 == null) {
			return actuator1;
		}

		if (actuator1 instanceof SequentialActuator) {
			// Optimisation
			final SequentialActuator sequence = (SequentialActuator) actuator1;

			sequence.addActuator(actuator2);

			return sequence;
		}

		return new SequentialActuator(actuator1, actuator2);
	}
}