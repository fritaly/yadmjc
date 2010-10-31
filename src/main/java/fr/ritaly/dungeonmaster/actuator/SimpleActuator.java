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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Temporizer;

/**
 * A simple {@link Actuator} used to trigger a {@link Triggered}.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class SimpleActuator implements Actuator {

	/**
	 * A {@link Temporizer} used to delay the triggering of a {@link Triggered}.
	 */
	private final Temporizer temporizer;

	/**
	 * The triggering action to be triggered.
	 */
	private final TriggerAction action;

	/**
	 * The list of {@link Triggered} to be triggered.
	 */
	private final List<Triggered> targets;

	/**
	 * The actuator's label.
	 */
	private final String label;

	/**
	 * The maximal number of clock ticks before triggering the {@link Triggered}
	 * .
	 */
	private final int max;

	public SimpleActuator(int count, TriggerAction action, Triggered... targets) {

		Validate.isTrue(count > 0, "The given tick count <" + count
				+ "> must be positive");
		Validate.notNull(action, "The given trigger action is null");
		Validate.notNull(targets, "The given array is null");
		Validate.isTrue(targets.length > 0, "The given array is empty");

		final StringBuilder builder = new StringBuilder(512);

		boolean first = true;
		for (Triggered triggered : targets) {
			if (!first) {
				builder.append(",");
			} else {
				first = false;
			}

			builder.append(triggered);
		}

		this.label = "Actuator[" + builder + "]";
		this.temporizer = new Temporizer(label, count);
		this.action = action;
		this.targets = Arrays.asList(targets);
		this.max = count;
	}

	public SimpleActuator(SimpleActuator actuator) {
		Validate.notNull(actuator, "The given actuator is null");

		this.action = actuator.action;
		this.label = actuator.label;
		this.max = actuator.max;
		this.temporizer = new Temporizer(label, max);
		this.targets = new ArrayList<Triggered>(actuator.targets);
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			// Déclenchement de chaque cible
			for (Triggered triggered : targets) {
				triggered.trigger(action);
			}

			return false;
		}

		return true;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Object clone() {
		return new SimpleActuator(this);
	}

	@Override
	public String toString() {
		return getLabel();
	}
}