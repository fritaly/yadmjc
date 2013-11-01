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
 * The champion's hand that holds the weapon. The hand can be enabled or
 * disabled. Every time the weapon is used, the hand becomes unusable for a
 * given duration that depends on the weapon's action used.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class WeaponHand extends Hand implements ClockListener {

	private final Log log = LogFactory.getLog(WeaponHand.class);

	/**
	 * Whether the hand can be used.
	 */
	private boolean enabled = true;

	/**
	 * The name of the hand's owner (the champion).
	 */
	private final String owner;

	/**
	 * Temporizer used for managing when the hand is usable.
	 */
	private Temporizer temporizer;

	public WeaponHand(Body body) {
		super(body);

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
			log.debug(String.format("%s.WeaponHand.Enabled = %s", owner, enabled))	;
		}

		// TODO Notification
	}

	/**
	 * Disables the hand for the given number of clock ticks.
	 *
	 * @param duration
	 *            a positive integer representing a duration in clock ticks.
	 */
	public void disable(final int duration) {
		Validate.isTrue(duration > 0, String.format("The given duration %d must be positive", duration));

		// The hand can only be used if it's enabled
		if (isEnabled()) {
			// The hand isn't available any more
			if (temporizer != null) {
				throw new IllegalStateException("The temporizer must be null");
			}

			// TODO Notification
			this.enabled = false;

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.WeaponHand.Enabled = false (for %d ticks)", owner, duration));
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

		//  Keep listening as long as the hand is still disabled
		return !isEnabled();
	}

	@Override
	public String toString() {
		return (owner != null) ? owner + ".WeaponHand" : "WeaponHand";
	}
}