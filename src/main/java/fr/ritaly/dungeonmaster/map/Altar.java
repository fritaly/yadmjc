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
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuator;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Bones;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Altar extends DirectedElement implements HasActuator {

	private Actuator actuator;

	public Altar(Direction direction) {
		super(Element.Type.ALTAR, direction);
	}

	@Override
	public String getCaption() {
		return "AL";
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
		Validate.isTrue(creature != null, "The given creature is null");

		return (creature != null) && creature.isImmaterial();
	}

	public final List<Item> getItems(Direction direction) {
		// Appel de la méthode non surchargée
		return super.getItems(map(direction));
	}

	public final Item pickItem(Direction direction) {
		// Appel de la méthode non surchargée
		final Item item = super.pickItem(map(direction));

		if (!hasItem() && (item != null) && (actuator != null)) {
			// Déclenchement au dernier objet pris
			Clock.getInstance().register(actuator);
		}

		return item;
	}

	public final void dropItem(Item item, Direction direction) {
		// Appel de la méthode non surchargée
		super.itemDroppedDown(item, map(direction));

		// Déclenchement au premier objet déposé
		if ((getItemCount() == 1) && (actuator != null)) {
			Clock.getInstance().register(actuator);
		}

		if (item instanceof Bones) {
			final Bones bones = (Bones) item;

			if (bones.hasChampion()) {
				// FIXME Animation à jouer ici

				// Jouer un son
				SoundSystem.getInstance().play(getPartyPosition(),
						AudioClip.ALTAR);

				// Réssusciter le champion
				bones.getChampion().resurrect();

				// Supprimer les os de l'autel
				final Item removed = pickItem(direction);

				if (removed != bones) {
					throw new IllegalStateException(
							"The removed items should be " + bones
									+ " (actual: " + removed + ")");
				}
			}
		}
	}

	@Override
	public final List<Item> getItems(SubCell subCell) {
		// Surcharge pour forcer l'appel à la bonne méthode
		throw new UnsupportedOperationException();
	}

	@Override
	public final Item pickItem(SubCell corner) {
		// Surcharge pour forcer l'appel à la bonne méthode
		throw new UnsupportedOperationException();
	}

	@Override
	public final void itemDroppedDown(Item item, SubCell corner) {
		// Surcharge pour forcer l'appel à la bonne méthode
		throw new UnsupportedOperationException();
	}

	private Direction map(SubCell subCell) {
		Validate.isTrue(subCell != null, "The given sub-cell is null");

		switch (subCell) {
		case NORTH_EAST:
			return Direction.NORTH;
		case NORTH_WEST:
			return Direction.SOUTH;
		case SOUTH_EAST:
			return Direction.EAST;
		case SOUTH_WEST:
			return Direction.WEST;
		}

		throw new UnsupportedOperationException();
	}

	private SubCell map(Direction direction) {
		Validate.isTrue(direction != null, "The given direction is null");

		switch (direction) {
		case NORTH:
			return SubCell.NORTH_EAST;
		case SOUTH:
			return SubCell.NORTH_WEST;
		case EAST:
			return SubCell.SOUTH_EAST;
		case WEST:
			return SubCell.SOUTH_WEST;
		}

		throw new UnsupportedOperationException();
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException(
					"A (one side) altar can't have champions");
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