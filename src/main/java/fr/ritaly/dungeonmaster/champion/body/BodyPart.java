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

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.CarryLocation;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * Une partie du corps d'un {@link Champion}.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public abstract class BodyPart implements ChangeEventSource {

	private final Log log = LogFactory.getLog(this.getClass());

	private final Body body;

	/**
	 * Indique si cette partie de corps est blessée.
	 */
	private boolean wounded;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * L'objet eventuellement porté par cette partie de corps.
	 */
	private Item item;

	protected BodyPart(Body body) {
		if (body == null) {
			throw new IllegalArgumentException("The given body is null");
		}

		this.body = body;
	}

	protected void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	/**
	 * Enumération des différents types de partie de corps.
	 */
	public static enum Type {
		HEAD,
		NECK,
		TORSO,
		LEGS,
		FEET,
		WEAPON_HAND,
		SHIELD_HAND;

		/**
		 * Indique si ce type de partie de corps désigne une main.
		 */
		public boolean isHand() {
			return equals(SHIELD_HAND) || equals(WEAPON_HAND);
		}

		public String getLabel() {
			switch (this) {
			case FEET:
				return "Feet";
			case HEAD:
				return "Head";
			case LEGS:
				return "Legs";
			case NECK:
				return "Neck";
			case SHIELD_HAND:
				return "ShieldHand";
			case TORSO:
				return "Torso";
			case WEAPON_HAND:
				return "WeaponHand";
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	public abstract Type getType();

	/**
	 * Retourne l'instance de {@link CarryLocation} associée à cette
	 * {@link BodyPart}. Permet de savoir si un {@link Item} peut être équipé
	 * par une {@link BodyPart}.
	 * 
	 * @return une {@link CarryLocation}. Ne retourne jamais null.
	 */
	public abstract CarryLocation getCarryLocation();

	public Body getBody() {
		return body;
	}

	/**
	 * Indique si cette {@link BodyPart} peut être blessée.
	 * 
	 * @return si cette {@link BodyPart} peut être blessée.
	 */
	public abstract boolean isWoundable();

	public boolean wound() {
		if (isWoundable() && !wounded) {
			// TODO Force de blessure ?
			wounded = true;

			fireChangeEvent();

			if (log.isDebugEnabled()) {
				log.debug(getBody().getChampion().getName() + "."
						+ getType().getLabel() + ".Wounded: true");
			}

			return true;
		}

		return false;
	}

	public void heal() {
		if (isWoundable() && wounded) {
			// TODO Force de guérison ?
			wounded = false;

			fireChangeEvent();

			if (log.isDebugEnabled()) {
				log.debug(getBody().getChampion().getName() + "."
						+ getType().getLabel() + ".Wounded: false");
			}
		}
	}

	/**
	 * Indique si cette {@link BodyPart} est blessée.
	 * 
	 * @return si cette {@link BodyPart} est blessée.
	 */
	public boolean isWounded() {
		return wounded;
	}

	/**
	 * Retourne l'objet porté par cette partie du corps ou null s'il n'y en a
	 * aucun.
	 * 
	 * @return une instance de {@link Item}.
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * Retourne le poids de l'objet porté par cette {@link BodyPart}.
	 * 
	 * @return un float représentant un poids.
	 */
	public float getWeight() {
		return (item != null) ? item.getWeight() : 0.0f;
	}

	public Item putOn(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("The given item is null");
		}

		if (accepts(item)) {
			// if (log.isDebugEnabled()) {
			// log.debug("Putting " + item.getType() + " on "
			// + getBody().getChampion().getName() + "'s " + getType()
			// + " ...");
			// }

			if (this.item != item) {
				// Retirer l'objet actuel
				final Item removed = takeOff(false, false);

				this.item = item;

				// Notifier l'objet qu'il vient d'être pris / revêtu
				this.item.itemPutOn(this);

				fireChangeEvent();

				if (log.isDebugEnabled()) {
					log.debug(getBody().getChampion().getName() + "."
							+ getType().getLabel() + ".Item: [+] " + item);

					// log.debug("Put " + item + " on "
					// + getBody().getChampion().getName() + "'s "
					// + getType());
				}

				return removed;
			}

			return item;
		}

		return item;
	}

	public Item takeOff() {
		return takeOff(true, false);
	}

	public Item takeOff(boolean force) {
		return takeOff(true, force);
	}

	private Item takeOff(boolean notify, boolean force) {
		if (this.item == null) {
			return null;
		}

		// if (log.isDebugEnabled()) {
		// log.debug("Taking " + this.item.getType() + " off "
		// + getBody().getChampion().getName() + "'s " + getType()
		// + " ...");
		// }

		if (!this.item.tryRemove() && !force) {
			// Object non retirable
			if (log.isDebugEnabled()) {
				log.debug(this.item + " can't be removed");
			}

			return null;
		}

		final Item removed = this.item;

		if (removed != null) {
			// Notifier l'objet qu'il vient d'être lâché
			removed.itemTakenOff();
		}

		this.item = null;

		if (notify) {
			fireChangeEvent();
		}

		if (log.isDebugEnabled()) {
			log.debug(getBody().getChampion().getName() + "."
					+ getType().getLabel() + ".Item: [-] " + removed);

			// log.debug("Took " + removed + " off "
			// + getBody().getChampion().getName() + "'s " + getType());
		}

		return removed;
	}

	/**
	 * Indique si cette partie de corps porte un objet.
	 * 
	 * @return si cette partie de corps porte un objet.
	 */
	public boolean hasItem() {
		return (this.item != null);
	}

	/**
	 * Indique si cette partie du corps porte un objet du type donné.
	 * 
	 * @param type
	 *            une instance de {@link fr.ritaly.dungeonmaster.item.Item.Type}
	 *            .
	 * @return si cette partie du corps porte un objet du type donné.
	 */
	public boolean hasItem(Item.Type type) {
		Validate.notNull(type, "The given item type is null");

		return (this.item != null) && this.item.getType().equals(type);
	}

	/**
	 * Indique si l'objet donné peut être porté / pris par cette partie de
	 * corps. Remarque: A ne pas confondre avec le concept "d'activation".
	 * 
	 * @param item
	 *            une instance de {@link Item}.
	 * @return si l'objet donné peut être porté / pris par cette partie de
	 *         corps.
	 */
	public boolean accepts(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("The given item is null");
		}

		if (getType().isHand()) {
			// Tous les objets peuvent être pris en main
			return true;
		}

		if (item.isActivatedBy(this)) {
			// L'objet peut être "revêtu" si la partie du corps correspond
			return true;
		}

		return false;
	}
}