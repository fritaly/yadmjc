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

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class LoopingActuator implements Actuator {

	private final Actuator prototype;

	private Actuator actuator;

	private final int max;

	private int current = 0;

	/**
	 * Le libellé de l'actuator.
	 */
	private final String label;

	public LoopingActuator(int max, Actuator actuator)
			throws CloneNotSupportedException {

		Validate.notNull(actuator, "The given actuator is null");
		Validate.isTrue(max >= 0, "The given max count <" + max
				+ "> must be positive or zero");

		this.prototype = actuator;
		this.max = max;
		this.label = getClass().getSimpleName() + "[" + max + " x "
				+ actuator.getLabel() + "]";
	}

	public LoopingActuator(Actuator... actuators)
			throws CloneNotSupportedException {

		this(Integer.MAX_VALUE, actuators);
	}

	public LoopingActuator(int max, Actuator... actuators)
			throws CloneNotSupportedException {

		Validate.notNull(actuators, "The given array of actuators is null");
		Validate.isTrue(actuators.length > 0,
				"The given array of actuators is empty");

		this.prototype = new CompositeActuator(actuators);
		this.max = max;
		this.label = getClass().getSimpleName() + "[" + max + " x "
				+ prototype.getLabel() + "]";
	}

	public LoopingActuator(Actuator actuator) throws CloneNotSupportedException {
		this(Integer.MAX_VALUE, actuator);
	}

	public LoopingActuator(LoopingActuator actuator)
			throws CloneNotSupportedException {

		Validate.notNull(actuator, "The given actuator is null");

		this.prototype = (Actuator) actuator.prototype.clone();
		this.max = actuator.max;
		this.label = actuator.label;
	}

	@Override
	public boolean clockTicked() {
		if (actuator == null) {
			try {
				actuator = (Actuator) prototype.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}

		if (!actuator.clockTicked()) {
			// Déclenchement
			current++;

			actuator = null;
		}

		return current < max;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
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