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
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Poison implements ClockListener {

	private int strength;

	private final Temporizer temporizer = new Temporizer("Poison",
			10 * Clock.ONE_SECOND);

	public boolean isActive() {
		return (strength > 0);
	}

	public void cure(PowerRune powerRune) {
		Validate.isTrue(powerRune != null, "The given power rune is null");

		this.strength = Math.max(0, strength - powerRune.getPowerLevel());
	}

	public void strengthen(PowerRune powerRune) {
		Validate.isTrue(powerRune != null, "The given power rune is null");

		final boolean wasActive = isActive();

		this.strength += powerRune.getPowerLevel();

		if (!wasActive && isActive()) {
			Clock.getInstance().register(this);
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			// Le poison se dissipe
			cure(PowerRune.LO);
		}

		// Continuer tant que le poison est actif
		return isActive();
	}
}