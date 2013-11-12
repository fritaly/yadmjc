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

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuators;
import fr.ritaly.dungeonmaster.actuator.TriggerType;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class FloorSwitch extends FloorTile implements HasActuators {

	/**
	 * Map contenant les {@link Actuator}s � d�clencher quand un �v�nement d'un
	 * type donn� survient.
	 */
	private Map<TriggerType, Actuator> actuators;

	/**
	 * Indique si une dalle de pression est visible au sol. Vaut true par
	 * d�faut.
	 */
	private final boolean pressurePadVisible;

	public FloorSwitch(boolean pressurePadVisible) {
		super(Type.FLOOR_SWITCH);

		this.pressurePadVisible = pressurePadVisible;
	}

	public FloorSwitch() {
		this(true);
	}

	@Override
	public boolean isTraversable(Party party) {
		return true;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		return true;
	}

	@Override
	public boolean isTraversableByProjectile() {
		return true;
	}

	@Override
	public String getSymbol() {
		return "P";
	}

	@Override
	public void validate() throws ValidationException {
	}

	@Override
	protected void afterItemAdded(Item item, Sector sector) {
		super.afterItemAdded(item, sector);

		// Ne d�clencher qu'au premier objet d�pos� !!!
		if ((actuators != null)
				&& actuators.containsKey(TriggerType.ITEM_DROPPED)
				&& (getItemCount() == 1)) {

			final Actuator actuator = actuators.get(TriggerType.ITEM_DROPPED);

			if (log.isDebugEnabled()) {
				log.debug("Triggering actuator " + actuator.getLabel() + " ...");
			}

			Clock.getInstance().register(actuator);
		}
	}

	@Override
	protected void afterItemRemoved(Item item, Sector sector) {
		super.afterItemRemoved(item, sector);

		// Ne d�clencher qu'au dernier objet ramass� !!!
		if ((actuators != null)
				&& actuators.containsKey(TriggerType.ITEM_PICKED_UP)
				&& (getItemCount() == 0)) {

			final Actuator actuator = actuators.get(TriggerType.ITEM_PICKED_UP);

			if (log.isDebugEnabled()) {
				log.debug("Triggering actuator " + actuator.getLabel() + " ...");
			}

			Clock.getInstance().register(actuator);
		}
	}

	@Override
	protected void afterPartySteppedOn() {
		super.afterPartySteppedOn();

		if ((actuators != null)
				&& actuators.containsKey(TriggerType.PARTY_STEPPED_ON)) {

			final Actuator actuator = actuators.get(TriggerType.PARTY_STEPPED_ON);

			if (log.isDebugEnabled()) {
				log.debug("Triggering actuator " + actuator.getLabel() + " ...");
			}

			Clock.getInstance().register(actuator);
		}
	}

	@Override
	protected void afterPartySteppedOff(Party party) {
		super.afterPartySteppedOff(party);

		if ((actuators != null)
				&& actuators.containsKey(TriggerType.PARTY_STEPPED_OFF)) {

			final Actuator actuator = actuators
					.get(TriggerType.PARTY_STEPPED_OFF);

			if (log.isDebugEnabled()) {
				log.debug("Triggering actuator " + actuator.getLabel() + " ...");
			}

			Clock.getInstance().register(actuator);
		}
	}

	public void addActuator(TriggerType triggerType, Actuator actuator) {
		Validate.notNull(triggerType, "The given trigger type is null");
		Validate.notNull(actuator, "The given actuator is null");

		if (actuators == null) {
			// Cr�er la Map � la vol�e
			actuators = new EnumMap<TriggerType, Actuator>(TriggerType.class);
		}

		actuators.put(triggerType,
				Actuators.combine(actuators.get(triggerType), actuator));
	}

	public void setActuator(TriggerType triggerType, Actuator actuator) {
		Validate.notNull(triggerType, "The given trigger type is null");
		Validate.notNull(actuator, "The given actuator is null");

		if (actuators == null) {
			// Cr�er la Map � la vol�e
			actuators = new EnumMap<TriggerType, Actuator>(TriggerType.class);
		}

		actuators.put(triggerType, actuator);
	}

	public void clearActuator(TriggerType triggerType) {
		Validate.notNull(triggerType, "The given trigger type is null");

		if (actuators != null) {
			actuators.remove(triggerType);
		}
	}

	public Actuator getActuator(TriggerType triggerType) {
		Validate.notNull(triggerType, "The given trigger type is null");

		return (actuators != null) ? actuators.get(triggerType) : null;
	}

	public boolean isPressurePadVisible() {
		return pressurePadVisible;
	}
}