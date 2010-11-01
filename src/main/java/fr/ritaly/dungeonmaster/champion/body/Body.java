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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart.Type;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A champion's body is made of 7 parts:
 * <ul>
 * <li>the head</li>
 * <li>the neck</li>
 * <li>the torso</li>
 * <li>the legs</li>
 * <li>the feet</li>
 * <li>the weapon hand</li>
 * <li>the shield hand</li>
 * </ul>
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Body {

	private final Log log = LogFactory.getLog(Body.class);

	/**
	 * The {@link Champion} this body belongs to.
	 */
	private final Champion champion;

	/**
	 * The body parts stored by type.
	 */
	private final Map<BodyPart.Type, BodyPart> parts = new EnumMap<BodyPart.Type, BodyPart>(
			BodyPart.Type.class);

	private final Random random = new Random();

	public Body(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		this.champion = champion;

		register(new Head(this));
		register(new Neck(this));
		register(new Torso(this));
		register(new Legs(this));
		register(new Feet(this));
		register(new WeaponHand(this));
		register(new ShieldHand(this));
	}

	private void register(BodyPart bodyPart) {
		Validate.notNull(bodyPart, "The given body part is null");

		parts.put(bodyPart.getType(), bodyPart);
	}

	public Champion getChampion() {
		return champion;
	}

	/**
	 * Returns the body part with given type.
	 * 
	 * @param type
	 *            a instance of {@link Type} representing the type of the
	 *            requested body part.
	 * @return an instance of {@link BodyPart}. Never returns null.
	 */
	public BodyPart getPart(BodyPart.Type type) {
		Validate.notNull(type, "The given body part type is null");

		switch (type) {
		case FEET:
			return getFeet();
		case HEAD:
			return getHead();
		case LEGS:
			return getLegs();
		case NECK:
			return getNeck();
		case SHIELD_HAND:
			return getShieldHand();
		case TORSO:
			return getTorso();
		case WEAPON_HAND:
			return getWeaponHand();
		default:
			throw new UnsupportedOperationException(
					"Unexpected body part type " + type);
		}
	}

	public BodyPart getHead() {
		return parts.get(BodyPart.Type.HEAD);
	}

	public BodyPart getNeck() {
		return parts.get(BodyPart.Type.NECK);
	}

	public BodyPart getTorso() {
		return parts.get(BodyPart.Type.TORSO);
	}

	public BodyPart getLegs() {
		return parts.get(BodyPart.Type.TORSO);
	}

	public BodyPart getFeet() {
		return parts.get(BodyPart.Type.FEET);
	}

	public WeaponHand getWeaponHand() {
		return (WeaponHand) parts.get(BodyPart.Type.WEAPON_HAND);
	}

	public Hand getShieldHand() {
		return (Hand) parts.get(BodyPart.Type.SHIELD_HAND);
	}

	/**
	 * Collects (and removes) all the items worn by this body's parts and
	 * returns them.
	 * 
	 * @return a List&lt;Item&gt;. Never returns null.
	 */
	public List<Item> removeAllItems() {
		final List<Item> items = new ArrayList<Item>();

		for (BodyPart bodyPart : parts.values()) {
			if (bodyPart.hasItem()) {
				items.add(bodyPart.takeOff(true));
			}
		}

		return items;
	}

	/**
	 * Collects (but doesn't remove) all the items worn by this body's parts and
	 * returns them.
	 * 
	 * @return a List&lt;Item&gt;. Never returns null.
	 */
	public List<Item> getItems() {
		final List<Item> items = new ArrayList<Item>();

		for (BodyPart bodyPart : parts.values()) {
			if (bodyPart.hasItem()) {
				items.add(bodyPart.getItem());
			}
		}

		return items;
	}

	/**
	 * Tells whether at least one part of this body is wounded.
	 * 
	 * @return whether at least one part of this body is wounded.
	 */
	public boolean isWounded() {
		// Version optimisée
		for (BodyPart bodyPart : parts.values()) {
			if (bodyPart.isWounded()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Tries to wound this body and returns whether the operation succeeded.
	 * 
	 * @return whether at least one body part was wounded.
	 */
	public boolean wound() {
		if (log.isDebugEnabled()) {
			log.debug("Wounding " + getChampion().getName() + "'s body ...");
		}

		// TODO Force de blessure ? Nombre de blessures ?
		final List<BodyPart> bodyParts = getNotWoundedParts();

		if (!bodyParts.isEmpty()) {
			Collections.shuffle(bodyParts);

			boolean wounded = false;

			while (!bodyParts.isEmpty() && !wounded) {
				wounded = bodyParts.remove(0).wound();
			}

			return wounded;
		}

		return false;
	}

	/**
	 * Tries to heal this body and returns whether the operation succeeded.
	 * 
	 * @return whether at least one body part was healed.
	 */
	public boolean heal() {
		if (log.isDebugEnabled()) {
			log.debug("Healing " + getChampion().getName() + "'s body ...");
		}

		// TODO Force de guérison ? Nombre de guérisons ?
		final List<BodyPart> bodyParts = getWoundedParts();

		if (!bodyParts.isEmpty()) {
			final BodyPart bodyPart = bodyParts.get(random.nextInt(bodyParts
					.size()));

			bodyPart.heal();

			if (log.isDebugEnabled()) {
				log.debug("Healed " + getChampion().getName() + "'s "
						+ bodyPart.getType());
			}

			return true;
		}

		return false;
	}

	/**
	 * Returns the parts of this body which are wounded.
	 * 
	 * @return a List&lt;BodyPart&gt; containing the wounded body parts. Never
	 *         returns null.
	 * @see #getNotWoundedParts()
	 */
	public List<BodyPart> getWoundedParts() {
		final List<BodyPart> result = new ArrayList<BodyPart>(7);

		for (BodyPart bodyPart : parts.values()) {
			if (bodyPart.isWounded()) {
				result.add(bodyPart);
			}
		}

		return result;
	}

	/**
	 * Returns the parts of this body which are NOT wounded.
	 * 
	 * @return a List&lt;BodyPart&gt; containing the not wounded body parts.
	 *         Never returns null.
	 * @see #getWoundedParts()
	 */
	public List<BodyPart> getNotWoundedParts() {
		final List<BodyPart> result = new ArrayList<BodyPart>(7);

		for (BodyPart bodyPart : parts.values()) {
			if (!bodyPart.isWounded()) {
				result.add(bodyPart);
			}
		}

		return result;
	}

	/**
	 * Returns the total weight for all the items put on this body.
	 * 
	 * @return a float representing a weight (in kilograms).
	 */
	public float getTotalWeight() {
		float weight = 0.0f;

		for (BodyPart bodyPart : parts.values()) {
			weight += bodyPart.getWeight();
		}

		return weight;
	}

	/**
	 * Returns the anti-magic bonus computed from the items worn by this body.
	 * 
	 * @return a positive or null integer representing an anti-magic bonus.
	 */
	public int getAntiMagic() {
		int antiMagic = 0;

		for (BodyPart bodyPart : parts.values()) {
			final Item item = bodyPart.getItem();

			if (item != null) {
				// Ne prendre en compte l'objet que s'il est sur la bonne partie
				// du corps
				if (item.isActivatedBy(bodyPart)) {
					antiMagic += item.getAntiMagic();
				}
			}
		}

		return antiMagic;
	}

	/**
	 * Returns the shield bonus computed from the items worn by this body.
	 * 
	 * @return a positive or null integer representing a shield bonus.
	 */
	public int getShield() {
		int shield = 0;

		for (BodyPart bodyPart : parts.values()) {
			final Item item = bodyPart.getItem();

			if (item != null) {
				// Ne prendre en compte l'objet que s'il est sur la bonne partie
				// du corps
				if (item.isActivatedBy(bodyPart)) {
					shield += item.getShield();
				}
			}
		}

		return shield;
	}
}
