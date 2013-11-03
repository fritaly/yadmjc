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
package fr.ritaly.dungeonmaster.item;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.magic.PowerRune;

// TODO Fire events ?
/**
 * This class is responsible for managing the curse of a cursed item.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Curse {

	private final Log log = LogFactory.getLog(Curse.class);

	// TODO Should strength be a power rune or an int ?
	/**
	 * The strength of the curse stored as an integer.
	 */
	private int strength;

	/**
	 * Whether the curse has been detected. In Dungeon Master, when a cursed
	 * item is grabbed by a champion, the curse is only detected when trying to
	 * drop the item.
	 */
	private boolean detected;

	// TODO Store the reference to the cursed item and use its name in the logs

	/**
	 * Returns the curse's strength.
	 *
	 * @return an integer representing the curse's strength.
	 */
	int getStrength() {
		return strength;
	}

	/**
	 * Sets the curse's strength to the given value.
	 *
	 * @param strength
	 *            an integer representing the curse strength to set.
	 */
	void setStrength(int strength) {
		if (this.strength != strength) {
			this.strength = strength;

			if (log.isDebugEnabled()) {
				log.debug(String.format("Curse's strength set to %d", strength));
			}
		}
	}

	/**
	 * Tells whether the curse is active.
	 *
	 * @return whether the curse is active.
	 */
	public boolean isActive() {
		return (strength > 0);
	}

	/**
	 * Conjures (that is, weakens) the curse with the power of the given power
	 * rune.
	 *
	 * @param powerRune
	 *            a power rune representing the force used to weaken the curse.
	 *            Can't be null.
	 */
	public void conjure(PowerRune powerRune) {
		Validate.notNull(powerRune, "The given power rune is null");

		if (log.isDebugEnabled()) {
			log.debug("Conjuring curse ...");
		}

		setStrength(Math.max(0, this.strength - powerRune.getPowerLevel()));
	}

	// TODO Use a power rune instead of an int
	/**
	 * Strengthens the curse with the force with as a power rune.
	 *
	 * @param strength
	 *            an integer representing to force to strengthen the curse. Must
	 *            be positive.
	 */
	public void curse(int strength) {
		Validate.isTrue(strength > 0, String.format("The given strength %d must be positive", strength));

		if (log.isDebugEnabled()) {
			log.debug("Strengthening curse ...");
		}

		setStrength(this.strength + strength);
	}

	/**
	 * Tells whether the curse has been detected.
	 *
	 * @return whether the curse has been detected.
	 */
	public boolean isDetected() {
		return detected;
	}

	/**
	 * Sets whether the curse has been detected.
	 *
	 * @param detected
	 *            whether the curse has been detected.
	 */
	public void setDetected(boolean detected) {
		if (!this.detected && detected) {
			// That's the only transition allowed
			this.detected = detected;

			if (log.isDebugEnabled()) {
				log.debug("Curse detected");
			}
		}
	}
}