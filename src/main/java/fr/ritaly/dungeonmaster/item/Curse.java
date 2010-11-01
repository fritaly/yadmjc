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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.magic.PowerRune;

// TODO Lever des events ?
/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Curse {

	private final Log log = LogFactory.getLog(Curse.class);

	// TODO Should strength be a PowerRune instead of an int ? 
	private int strength;

	private boolean detected;

	int getStrength() {
		return strength;
	}

	void setStrength(int strength) {
		if (this.strength != strength) {
			this.strength = strength;

			if (log.isDebugEnabled()) {
				log.debug("Curse's strength set to " + strength);
			}
		}
	}

	public boolean isActive() {
		return (strength > 0);
	}

	public void conjure(PowerRune powerRune) {
		if (powerRune == null) {
			throw new IllegalArgumentException("The given power rune is null");
		}

		if (log.isDebugEnabled()) {
			log.debug("Conjuring curse ...");
		}

		setStrength(Math.max(0, this.strength - powerRune.getPowerLevel()));
	}

	public void curse(int strength) {
		if (strength <= 0) {
			throw new IllegalArgumentException("The given strength <"
					+ strength + "> must be positive");
		}

		if (log.isDebugEnabled()) {
			log.debug("Strengthening curse ...");
		}

		setStrength(this.strength + strength);
	}

	public boolean isDetected() {
		return detected;
	}

	public void setDetected(boolean detected) {
		if (!this.detected && detected) {
			// Transition autorisée
			this.detected = detected;

			if (log.isDebugEnabled()) {
				log.debug("Curse detected");
			}
		}
	}
}