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
package fr.ritaly.dungeonmaster.champion;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.stat.Stat;

/**
 * Class responsible for managing the spells acting on the whole party.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class PartySpells implements ClockListener, ChangeEventSource {

	private final Log log = LogFactory.getLog(this.getClass());

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The party whose spells are managed.
	 */
	private final Party party;

	/**
	 * The stat pertaining to the "invisibility" spell.
	 */
	private final Stat invisibility;

	/**
	 * The stat pertaining to the "anti-magic" spell.
	 */
	private final Stat antiMagic;

	/**
	 * The stat pertaining to the "shield" spell.
	 */
	private final Stat shield;

	/**
	 * The stat pertaining to the "dispell illusions" spell.
	 */
	private final Stat dispellIllusion;

	/**
	 * The stat pertaining to the "see through walls" spell.
	 */
	private final Stat seeThroughWalls;

	/**
	 * The temporizer used to manage the decay of those spells over time.
	 */
	private final Temporizer temporizer = new Temporizer("Party.Spells", 4);

	public PartySpells(Party party) {
		Validate.notNull(party, "The given party is null");

		this.party = party;
		this.invisibility = new Stat("Party.Spells", "Invisibility");
		this.antiMagic = new Stat("Party.Spells", "AntiMagic");
		this.shield = new Stat("Party.Spells", "Shield");
		this.dispellIllusion = new Stat("Party.Spells", "DispellIllusion");
		this.seeThroughWalls = new Stat("Party.Spells", "SeeThroughWalls");
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (invisibility.value() > 0) {
				if (invisibility.dec(1) == 0) {
					// Notify the change of state
					fireChangeEvent();

					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.Invisibility is now inactive");
					}
				}
			}
			if (antiMagic.value() > 0) {
				if (antiMagic.dec(1) == 0) {
					// Notify the change of state
					fireChangeEvent();

					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.AntiMagic is now inactive");
					}
				}
			}
			if (shield.value() > 0) {
				if (shield.dec(1) == 0) {
					// Notify the change of state
					fireChangeEvent();

					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.Shield is now inactive");
					}
				}
			}
			if (dispellIllusion.value() > 0) {
				if (dispellIllusion.dec(1) == 0) {
					// Notify the change of state
					fireChangeEvent();

					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.DispellIllusion is now inactive");
					}
				}
			}
			if (seeThroughWalls.value() > 0) {
				if (seeThroughWalls.dec(1) == 0) {
					// Notify the change of state
					fireChangeEvent();

					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.SeeThroughWalls is now inactive");
					}
				}
			}
		}

		// Always listen to the clock ticks
		return true;
	}

	/**
	 * Tells whether the "invisibility" spell is currently active.
	 *
	 * @return whether the "invisibility" spell is currently active.
	 */
	public boolean isInvisibilityActive() {
		return (invisibility.value() > 0);
	}

	/**
	 * Tells whether the "dispell illusions" spell is currently active.
	 *
	 * @return whether the "dispell illusions" spell is currently active.
	 */
	public boolean isDispellIllusionActive() {
		return (dispellIllusion.value() > 0);
	}
	/**
	 * Tells whether the "see through walls" spell is currently active.
	 *
	 * @return whether the "see through walls" spell is currently active.
	 */
	public boolean isSeeThroughWallsActive() {
		return (seeThroughWalls.value() > 0);
	}

	public Party getParty() {
		return party;
	}

	public Stat getAntiMagic() {
		return antiMagic;
	}

	public Stat getShield() {
		return shield;
	}

	public Stat getInvisibility() {
		return invisibility;
	}

	public Stat getDispellIllusion() {
		return dispellIllusion;
	}

	public Stat getSeeThroughWalls() {
		return seeThroughWalls;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	private void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}
}