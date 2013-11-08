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

import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuator;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Alcove extends DirectedElement implements HasActuator {

	private Actuator actuator;

	/**
	 * The type of item triggering the actuator.
	 */
	private final Item.Type itemType;

	public Alcove(Direction direction, Item.Type itemType) {
		super(Element.Type.ALCOVE, direction);

		Validate.notNull(itemType, "The given item type is null");

		this.itemType = itemType;
	}

	public Alcove(Direction direction) {
		super(Element.Type.ALCOVE, direction);

		this.itemType = null;
	}

	public Item.Type getItemType() {
		return itemType;
	}

	@Override
	public String getSymbol() {
		return "1";
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		return Materiality.IMMATERIAL.equals(creature.getMateriality());
	}

	public final List<Item> getItems(Direction direction) {
		// Call the method from parent class
		return super.getItems(map(direction));
	}

	public final Item pickItem(Direction direction) {
		// Call the method from parent class
		final Item item = super.pickItem(map(direction));

		if (item != null) {
			// An item has been picked
			if (itemType == null) {
				// Triggered by any item type
				if (!hasItems() && (actuator != null)) {
					// The triggering occurs on the last item picked
					Clock.getInstance().register(actuator);
				}
			} else if (itemType.equals(item.getType())) {
				// Triggered for a given item type
				if (!hasItems() && (actuator != null)) {
					// The triggering occurs on the last item picked
					Clock.getInstance().register(actuator);
				}
			}
		}

		return item;
	}

	public final void dropItem(Item item, Direction direction) {
		// Call the method from parent class
		super.dropItem(item, map(direction));

		if (itemType == null) {
			// Triggered on the first item whatever the type
			if ((getItemCount() == 1) && (actuator != null)) {
				Clock.getInstance().register(actuator);
			}
		} else if (itemType.equals(item.getType())) {
			// Triggered on the first item with given type
			final List<Item> items = getItems(direction);

			int count = 0;

			for (Item anItem : items) {
				if (anItem.getType().equals(itemType)) {
					count++;
				}
			}

			if (count == 1) {
				Clock.getInstance().register(actuator);
			}
		}
	}

	@Override
	public final List<Item> getItems(Sector sector) {
		// Override to force the use of the relevant method
		throw new UnsupportedOperationException();
	}

	@Override
	public final Item pickItem(Sector corner) {
		// Override to force the use of the relevant method
		throw new UnsupportedOperationException();
	}

	@Override
	public final void dropItem(Item item, Sector corner) {
		// Override to force the use of the relevant method
		throw new UnsupportedOperationException();
	}

	private Sector map(final Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		switch (direction) {
		case NORTH:
			return Sector.NORTH_EAST;
		case SOUTH:
			return Sector.NORTH_WEST;
		case EAST:
			return Sector.SOUTH_EAST;
		case WEST:
			return Sector.SOUTH_WEST;
		default:
			throw new UnsupportedOperationException("Method unsupported for direction " + direction);
		}
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException("A (one side) alcove can't have champions");
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
}