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
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class WallSlot extends DirectedElement implements HasActuator {

	private final Log log = LogFactory.getLog(WallSlot.class);
	
	// FIXME Un WallSlot doit pouvoir être activé avec plusieurs pièces

	/**
	 * Indique si la fente a été utilisée.
	 */
	private boolean used = false;

	private final Item.Type itemType;

	private Actuator actuator;

	public WallSlot(Direction direction, Item.Type itemType) {
		super(Type.WALL_SLOT, direction);

		// N'importe quel objet peut être utilisé en tant que "pièce"
		Validate.notNull(itemType, "The given item type is null");

		this.itemType = itemType;
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
		return "SL";
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException("A wall slot can't have champions");
		}
	}

	/**
	 * Indique si la fente a été utilisée.
	 * 
	 * @return si la fente a été utilisée.
	 */
	public boolean isUsed() {
		return used;
	}
	
	/**
	 * Tente d'utiliser l'objet donné sur la fente et retourne si
	 * l'opération a réussi.
	 * 
	 * @param item
	 *            une instance de {@link Item} représentant un objet à utiliser sur la fente.
	 * @return si l'opération a réussi.
	 */
	public boolean unlock(Item item) {
		Validate.notNull(item, "The given item is null");

		if (itemType.equals(item.getType())) {
			// Objet du bon type
			if (!used) {
				// L'objet active la fente
				this.used = true;

				if (log.isDebugEnabled()) {
					log.debug(this + ": slot used");
				}

				// Jouer le son
				SoundSystem.getInstance().play(AudioClip.LOCK);

				// Déclenchement de la cible
				if (actuator != null) {
					Clock.getInstance().register(actuator);
				}

				return true;
			} else {
				// Fente déjà utilisée, ne rien faire
			}
		} else {
			// Objet du mauvais type, jouer un son dédié FIXME Son à créer
			// SoundSystem.getInstance().play(AudioClip.LOCK_FAILED);
		}

		return false;
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

	public Item.Type getItemType() {
		return itemType;
	}
	
	@Override
	public void clearActuator() {
		this.actuator = null;
	}
}