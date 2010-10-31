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
package fr.ritaly.dungeonmaster.map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuator;
import fr.ritaly.dungeonmaster.actuator.Triggered;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Lever extends DirectedElement implements HasActuator,
		Triggered {

	private final Log log = LogFactory.getLog(Lever.class);

	/**
	 * Indique si le levier est en haut ou en bas. Par défaut, il est en haut.
	 */
	private boolean leverUp = true;

	private Actuator actuator;

	public Lever(Direction direction) {
		super(Type.LEVER, direction);
	}

	public Lever(Direction direction, boolean leverUp) {
		this(direction);

		this.leverUp = leverUp;
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.isTrue(creature != null, "The given creature is null");

		return (creature != null) && creature.isImmaterial();
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public String getCaption() {
		return "LV";
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException("A lever wall can't have champions");
		}
	}

	/**
	 * Indique si le levier est en position haute.
	 * 
	 * @return si le levier est en position haute.
	 */
	public boolean isLeverUp() {
		return leverUp;
	}

	/**
	 * Indique si le levier est en position basse.
	 * 
	 * @return si le levier est en position basse.
	 */
	public boolean isLeverDown() {
		return !isLeverUp();
	}

	public void toggle() {
		this.leverUp = !leverUp;

		if (log.isDebugEnabled()) {
			log.debug(this + ": " + (leverUp ? "down -> up" : "up -> down"));
		}

		// Jouer un son
		SoundSystem.getInstance().play(getPartyPosition(), AudioClip.SWITCH);

		// Déclenchement de la cible
		if (actuator != null) {
			Clock.getInstance().register(actuator);
		}
	}

	public Actuator getActuator() {
		return actuator;
	}

	public void addActuator(Actuator actuator) {
		Validate.notNull(actuator, "The given actuator is null");

		this.actuator = Actuators.combine(this.actuator, actuator);
	}

	public void setActuator(Actuator actuator) {
		this.actuator = actuator;
	}

	@Override
	public void clearActuator() {
		this.actuator = null;
	}

	public boolean pushUp() {
		if (isLeverDown()) {
			toggle();

			return true;
		}

		return false;
	}

	public boolean pushDown() {
		if (isLeverUp()) {
			toggle();

			return true;
		}

		return false;
	}

	@Override
	public final void trigger(TriggerAction action) {
		Validate.notNull(action);

		if (log.isDebugEnabled()) {
			log.debug(this + " is being triggered [action=" + action + "]");
		}

		switch (action) {
		case ENABLE:
			pushDown();
			break;
		case DISABLE:
			pushUp();
			break;
		case TOGGLE:
			toggle();
			break;

		default:
			throw new UnsupportedOperationException();
		}
	}
}