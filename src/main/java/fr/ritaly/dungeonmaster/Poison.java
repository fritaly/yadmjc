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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.magic.PowerRune;

/**
 * Manages the effects of poison over time.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Poison implements ClockListener {

	/**
	 * The current strength of poison. The value decreases over time. When the
	 * value reaches zero, the poisoning stops. Value is positive or zero.
	 */
	private int strength;

	/**
	 * Temporizer used for managing the poison decay over time. Triggers every
	 * 10 seconds.
	 */
	private final Temporizer temporizer = new Temporizer("Poison", 10 * Clock.ONE_SECOND);

	public Poison() {
	}

	/**
	 * Tells whether the poison is still active.
	 *
	 * @return whether the poison is still active.
	 */
	public boolean isActive() {
		return (strength > 0);
	}

	/**
	 * Cures the poisoning with the given power rune.
	 *
	 * @param powerRune
	 *            a power rune representing the strength of the cure. Can't be
	 *            null.
	 */
	public void cure(final PowerRune powerRune) {
		Validate.notNull(powerRune, "The given power rune is null");

		this.strength = Math.max(0, strength - powerRune.getPowerLevel());
	}

	/**
	 * Strenghtens the poisoning with the given power rune.
	 *
	 * @param powerRune
	 *            a power rune representing the strength of the poisoning. Can't
	 *            be null.
	 */
	public void strengthen(final PowerRune powerRune) {
		Validate.notNull(powerRune, "The given power rune is null");

		final boolean wasActive = isActive();

		this.strength += powerRune.getPowerLevel();

		if (!wasActive && isActive()) {
			// Listen to clock ticks
			Clock.getInstance().register(this);
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			// The poison naturally decays over time
			cure(PowerRune.LO);
		}

		// Listen as long as the poison is active
		return isActive();
	}
}