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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * A composite actuator used for triggering a sequence of actuators.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class SequentialActuator implements Actuator {

	/**
	 * The actuators triggered.
	 */
	private final LinkedList<Actuator> actuators = new LinkedList<Actuator>();

	/**
	 * The actuator's label.
	 */
	private final String label;

	public SequentialActuator(Actuator... actuators) {
		Validate.notNull(actuators, "The given array of actuators is null");
		Validate.isTrue(actuators.length > 0, "The given array of actuators is empty");

		this.actuators.addAll(Arrays.asList(actuators));

		final StringBuilder builder = new StringBuilder(512);

		boolean first = true;

		for (Actuator actuator : actuators) {
			if (!first) {
				builder.append(",");
			} else {
				first = false;
			}

			builder.append(actuator.getLabel());
		}

		this.label = getClass().getSimpleName() + "[" + builder + "]";
	}

	public SequentialActuator(SequentialActuator actuator) throws CloneNotSupportedException {
		Validate.notNull(actuator, "The given actuator is null");

		for (Actuator a : actuator.actuators) {
			this.actuators.add((Actuator) a.clone());
		}

		this.label = actuator.label;
	}

	public void addActuator(Actuator actuator) {
		Validate.notNull(actuator, "The given actuator is null");

		actuators.addLast(actuator);
	}

	@Override
	public boolean clockTicked() {
		final Actuator actuator = actuators.getFirst();

		if (!actuator.clockTicked()) {
			// The actuator has been used, remove it
			actuators.removeFirst();
		}

		// Keep on listening to clock ticks as long as there are actuators left
		return !actuators.isEmpty();
	}

	public List<Actuator> getActuators() {
		// Defensive recopy
		return Collections.unmodifiableList(actuators);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new SequentialActuator(this);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return getLabel();
	}
}