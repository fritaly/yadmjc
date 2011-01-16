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
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuators;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class FloorSwitch extends Element implements HasActuators {

	/**
	 * Map contenant les {@link Actuator}s à déclencher quand un évènement d'un
	 * type donné survient.
	 */
	private Map<EventType, Actuator> actuators;

	/**
	 * Indique si une dalle de pression est visible au sol. Vaut true par
	 * défaut.
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
	public String getCaption() {
		return "P";
	}

	@Override
	public void validate() throws ValidationException {
	}

	@Override
	protected void afterItemDropped(Item item, SubCell subCell) {
		super.afterItemDropped(item, subCell);

		// Ne déclencher qu'au premier objet déposé !!!
		if ((actuators != null)
				&& actuators.containsKey(EventType.ITEM_DROPPED)
				&& (getItemCount() == 1)) {

			final Actuator actuator = actuators.get(EventType.ITEM_DROPPED);

			if (log.isDebugEnabled()) {
				log.debug("Triggering actuator " + actuator.getLabel() + " ...");
			}

			Clock.getInstance().register(actuator);
		}
	}

	@Override
	protected void afterItemPicked(Item item, SubCell subCell) {
		super.afterItemPicked(item, subCell);

		// Ne déclencher qu'au dernier objet ramassé !!!
		if ((actuators != null)
				&& actuators.containsKey(EventType.ITEM_PICKED_UP)
				&& (getItemCount() == 0)) {

			final Actuator actuator = actuators.get(EventType.ITEM_PICKED_UP);

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
				&& actuators.containsKey(EventType.PARTY_STEPPED_ON)) {

			final Actuator actuator = actuators.get(EventType.PARTY_STEPPED_ON);

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
				&& actuators.containsKey(EventType.PARTY_STEPPED_OFF)) {

			final Actuator actuator = actuators
					.get(EventType.PARTY_STEPPED_OFF);

			if (log.isDebugEnabled()) {
				log.debug("Triggering actuator " + actuator.getLabel() + " ...");
			}

			Clock.getInstance().register(actuator);
		}
	}

	public void addActuator(EventType eventType, Actuator actuator) {
		Validate.notNull(eventType, "The given event type is null");
		Validate.notNull(actuator, "The given actuator is null");

		if (actuators == null) {
			// Créer la Map à la volée
			actuators = new EnumMap<EventType, Actuator>(EventType.class);
		}

		actuators.put(eventType,
				Actuators.combine(actuators.get(eventType), actuator));
	}

	public void setActuator(EventType eventType, Actuator actuator) {
		Validate.notNull(eventType, "The given event type is null");
		Validate.notNull(actuator, "The given actuator is null");

		if (actuators == null) {
			// Créer la Map à la volée
			actuators = new EnumMap<EventType, Actuator>(EventType.class);
		}

		actuators.put(eventType, actuator);
	}

	public void clearActuator(EventType eventType) {
		Validate.notNull(eventType, "The given event type is null");

		if (actuators != null) {
			actuators.remove(eventType);
		}
	}

	public Actuator getActuator(EventType eventType) {
		Validate.notNull(eventType, "The given event type is null");

		return (actuators != null) ? actuators.get(eventType) : null;
	}

	public boolean isPressurePadVisible() {
		return pressurePadVisible;
	}
}