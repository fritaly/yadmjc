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
package fr.ritaly.dungeonmaster.champion.body;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.item.CarryLocation;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class WeaponHand extends Hand implements ClockListener {

	private final Log log = LogFactory.getLog(WeaponHand.class);

	/**
	 * Indique si la main est prête à être utilisée.
	 */
	private boolean enabled = true;

	private final String owner;

	private Temporizer temporizer;

	public WeaponHand(Body body) {
		super(body);

		// Mémoriser le nom de son "propriétaire"
		this.owner = body.getChampion().getName();
	}

	@Override
	public Type getType() {
		return Type.WEAPON_HAND;
	}

	@Override
	public final CarryLocation getCarryLocation() {
		return CarryLocation.HANDS;
	}

	@Override
	public boolean isWoundable() {
		return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	private void enable() {
		if (enabled) {
			throw new IllegalStateException("The hand is already enabled");
		}

		this.enabled = true;

		if (log.isDebugEnabled()) {
			log.debug(owner + ".WeaponHand.Enabled = " + enabled);
		}

		// TODO Notification
	}

	/**
	 * Utilise la main. Cela a pour effet de déclencher le time-out
	 * d'indisponibilité de la main si elle était "disponible".
	 * 
	 * @param duration
	 */
	public void disable(int duration) {
		Validate.isTrue(duration > 0, "The given duration <" + duration
				+ "> must be positive");

		// On ne peut utiliser la main que si elle est prête
		if (isEnabled()) {
			// La main n'est plus disponible
			if (temporizer != null) {
				throw new IllegalStateException("The temporizer must be null");
			}

			// TODO Notification
			this.enabled = false;

			if (log.isDebugEnabled()) {
				log.debug(owner + ".WeaponHand.Enabled = false (for "
						+ duration + " ticks)");
			}

			this.temporizer = new Temporizer(owner + ".WeaponHand", duration);

			Clock.getInstance().register(this);
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer != null) {
			if (temporizer.trigger()) {
				enable();

				temporizer = null;
			}
		}

		// Continuer tant que la main n'est pas "prête"
		return !isEnabled();
	}

	@Override
	public String toString() {
		return (owner != null) ? owner + ".WeaponHand" : "WeaponHand";
	}
}