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
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuator;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class WallLock extends DirectedElement implements HasActuator {

	private final Log log = LogFactory.getLog(WallLock.class);

	/**
	 * Indique si la serrure a �t� d�verrouill�e.
	 */
	private boolean unlocked = false;
	
	// TODO Implement maxUseCount like for WallSlot

	private final Item.Type keyType;

	private Actuator actuator;

	public WallLock(Direction direction, Item.Type keyType) {
		super(Type.WALL_LOCK, direction);

		Validate.notNull(keyType, "The given key type is null");
		Validate.isTrue(keyType.isKey(), "The given item type <" + keyType + "> isn't a key");

		this.keyType = keyType;
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.isTrue(creature != null, "The given creature is null");

		return (creature != null) && Materiality.IMMATERIAL.equals(creature.getMateriality());
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public String getCaption() {
		return "K";
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException("A wall lock can't have champions");
		}
	}

	/**
	 * Indique si la serrure a �t� d�verrouill�e.
	 * 
	 * @return si la serrure a �t� d�verrouill�e.
	 */
	public boolean isUnlocked() {
		return unlocked;
	}
	
	/**
	 * Indique si la serrure est verrouill�e.
	 * 
	 * @return si la serrure est verrouill�e.
	 */
	public boolean isLocked() {
		return !unlocked;
	}

	/**
	 * Tente de d�verrouiller la serrure avec la cl� donn�e et retourne si
	 * l'op�ration a r�ussi.
	 * 
	 * @param item
	 *            une instance de {@link Item} repr�sentant une cl� pour
	 *            d�verrouiller la serrure.
	 * @return si la serrure a �t� d�verrouill�e avec succ�s.
	 */
	public boolean unlock(Item item) {
		Validate.notNull(item, "The given item is null");

		if (item.getType().isKey()) {
			// L'objet est une cl�
			if (keyType.equals(item.getType())) {
				// Cl� du bon type
				if (!unlocked) {
					// La cl� d�verrouille la serrure
					this.unlocked = true;

					if (log.isDebugEnabled()) {
						log.debug(this + ": lock used");
					}

					// Jouer le son
					SoundSystem.getInstance().play(AudioClip.LOCK);

					// D�clenchement de la cible
					if (actuator != null) {
						Clock.getInstance().register(actuator);
					}

					return true;
				} else {
					// Serrure d�j� utilis�e, ne rien faire
				}
			} else {
				// Cl� du mauvais type, jouer un son d�di� FIXME Son � cr�er
				// SoundSystem.getInstance().play(AudioClip.LOCK_FAILED);
			}
		} else {
			// Mauvais type d'objet, ne rien faire
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

	public Item.Type getKeyType() {
		return keyType;
	}
	
	@Override
	public void clearActuator() {
		this.actuator = null;
	}
}