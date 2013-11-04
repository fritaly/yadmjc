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

import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.CarryLocation;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * Abstraction representing a champion's body part.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public abstract class BodyPart implements ChangeEventSource {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The body this part is bound to.
	 */
	private final Body body;

	/**
	 * Whether this part is wounded.
	 */
	private boolean wounded;

	/**
	 * Support class to fire change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The possible item carried by this body part.
	 */
	private Item item;

	protected BodyPart(Body body) {
		Validate.notNull(body, "The given body is null");

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
	 * Enumerates the different body part types.
	 *
	 * @author @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Type {
		HEAD("Head"),
		NECK("Neck"),
		TORSO("Torso"),
		LEGS("Legs"),
		FEET("Feet"),
		WEAPON_HAND("WeaponHand"),
		SHIELD_HAND("ShieldHand");

		private final String label;

		private Type(String label) {
			this.label = label;
		}

		/**
		 * Returns whether this body part is a hand.
		 *
		 * @return whether this body part is a hand.
		 */
		public boolean isHand() {
			return equals(SHIELD_HAND) || equals(WEAPON_HAND);
		}

		/**
		 * Returns this body part's label as a string. Mainly used for debugging.
		 *
		 * @return a string representing the body part's label.
		 */
		public String getLabel() {
			return label;
		}
	}

	/**
	 * Returns the type of this body part.
	 *
	 * @return the body part type.
	 */
	public abstract Type getType();

	public abstract CarryLocation getCarryLocation();

	public Body getBody() {
		return body;
	}

	/**
	 * Returns whether this body part can be wounded.
	 *
	 * @return whether this body part can be wounded.
	 */
	public abstract boolean isWoundable();

	/**
	 * Try to wound this body part and returns whether the body part was wounded.
	 *
	 * @return whether the body part was wounded.
	 */
	public boolean wound() {
		if (isWoundable() && !wounded) {
			// TODO Strength of wound ?
			wounded = true;

			fireChangeEvent();

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.%s.Wounded: true", getBody().getChampion().getName(), getType().getLabel()));
			}

			return true;
		}

		return false;
	}

	/**
	 * Heals this body part if it's wounded.
	 */
	public void heal() {
		if (isWoundable() && wounded) {
			// TODO Strength of healing ?
			wounded = false;

			fireChangeEvent();

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.%s.Wounded: false", getBody().getChampion().getName(), getType().getLabel()));
			}
		}
	}

	/**
	 * Returns whether this body part is wounded.
	 *
	 * @return whether this body part is wounded.
	 */
	public boolean isWounded() {
		return wounded;
	}

	/**
	 * Returns the item carried by this body part or null if there's none.
	 *
	 * @return an item or null.
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * Returns the weight of the carried item or zero if there's none.
	 *
	 * @return a float representing the weight of the carried item.
	 */
	public float getWeight() {
		return (item != null) ? item.getWeight() : 0.0f;
	}

	// TODO Rename into equip() ?
	public Item putOn(Item item) {
		Validate.notNull(item, "The given item is null");

		if (accepts(item)) {
			if (this.item != item) {
				// Remove the current item
				final Item removed = takeOff(false, false);

				this.item = item;

				// Notify the item that it's held
				this.item.itemPutOn(this);

				fireChangeEvent();

				if (log.isDebugEnabled()) {
					log.debug(String.format("%s.%s.Item: [+] %s", getBody().getChampion().getName(), getType().getLabel(), item));
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

		if (!this.item.isRemovable() && !force) {
			// The item can't be removed
			if (log.isDebugEnabled()) {
				log.debug(this.item + " can't be removed");
			}

			return null;
		}

		final Item removed = this.item;

		if (removed != null) {
			// Notify the item it has been released
			removed.itemTakenOff();
		}

		this.item = null;

		if (notify) {
			fireChangeEvent();
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s.%s.Item: [-] %s", getBody().getChampion().getName(), getType().getLabel(), removed));
		}

		return removed;
	}

	/**
	 * Returns whether this body part is carrying an item.
	 *
	 * @return whether this body part is carrying an item.
	 */
	public boolean hasItem() {
		return (this.item != null);
	}

	/**
	 * Returns whether this body part is carrying an item of the given type.
	 *
	 * @param type
	 *            the type of item to test. Can't bve null. .
	 * @return whether this body part is carrying an item of the given type.
	 */
	public boolean hasItem(Item.Type type) {
		Validate.notNull(type, "The given item type is null");

		return (this.item != null) && this.item.getType().equals(type);
	}

	/**
	 * Returns whether this given item can be carried by this body part.
	 *
	 * @param item
	 *            the item to test. Can't be null.
	 * @return whether this given item can be carried by this body part.
	 */
	public boolean accepts(Item item) {
		Validate.notNull(item, "The given item is null");

		if (getType().isHand()) {
			// All objects can be carried by a hand
			return true;
		}

		if (item.isActivatedBy(this)) {
			// The item is activated by this body part
			return true;
		}

		return false;
	}
}