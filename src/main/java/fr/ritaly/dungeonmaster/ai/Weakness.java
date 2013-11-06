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
package fr.ritaly.dungeonmaster.ai;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.magic.Spell;

/**
 * Enumerates the different types of "weaknesses". Some creatures can only be
 * hurt / killed by some specific weapons or spells. This enumeration list the
 * special weapons and spells that can only affect those creatures. Creatures
 * can have one or several weaknesses.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Weakness {

	// --- Weaknesses to weapons --- //

	/** Hurt by any weapon */
	WEAPONS,

	/** Hurt by the {@link Item.Type#VORPAL_BLADE} weapon */
	VORPAL_BLADE,

	/** Hurt by the {@link Item.Type#YEW_STAFF} weapon */
	YEW_STAFF,

	/** Hurt by the {@link Item.Type#STAFF_OF_MANAR} weapon */
	STAFF_OF_MANAR,

	/** Hurt by the {@link Item.Type#THE_FIRESTAFF} weapon */
	THE_FIRESTAFF,

	// --- Weaknesses to spells --- //

	/** Hurt the {@link Spell.Type#POISON_BOLT} spell */
	POISON_BOLT,

	/** Hurt the {@link Spell.Type#FIREBALL} spell */
	FIREBALL,

	/** Hurt the {@link Spell.Type#LIGHTNING_BOLT} spell */
	LIGHTNING_BOLT,

	/** Hurt the {@link Spell.Type#WEAKEN_IMMATERIAL} spell */
	WEAKEN_NON_MATERIAL_BEINGS_SPELL,

	// --- Miscellaneous --- //

	/** Hurt when inside a poison cloud */
	POISON_CLOUD;

	/**
	 * Tells whether the given weapon matches this weakness.
	 *
	 * @param weapon
	 *            the weapon item to test. Can't be null.
	 * @return whether the given weapon matches this weakness.
	 */
	public boolean acceptsWeapon(Item weapon) {
		Validate.notNull(weapon, "The given weapon item is null");

		switch (this) {
		case WEAPONS:
			// Any weapon work
			return true;
		case VORPAL_BLADE:
			// Only the appropriate weapon works
			return Item.Type.VORPAL_BLADE.equals(weapon.getType());
		case YEW_STAFF:
			// Only the appropriate weapon works
			return Item.Type.YEW_STAFF.equals(weapon.getType());
		case STAFF_OF_MANAR:
			// Only the appropriate weapon works
			return Item.Type.STAFF_OF_MANAR.equals(weapon.getType());
		case THE_FIRESTAFF:
			// Only the appropriate weapon works
			return Item.Type.THE_FIRESTAFF_COMPLETE.equals(weapon.getType());
		default:
			return false;
		}
	}

	/**
	 * Tells whether the given spell type matches this weakness.
	 *
	 * @param spellType
	 *            the spell type to test. Can't be null.
	 * @return whether the given spell type matches this weakness.
	 */
	public boolean acceptsSpell(Spell.Type spellType) {
		Validate.notNull(spellType, "The given spell type is null");

		switch (this) {
		case FIREBALL:
			return Spell.Type.FIREBALL.equals(spellType);
		case LIGHTNING_BOLT:
			return Spell.Type.LIGHTNING_BOLT.equals(spellType);
		case POISON_BOLT:
			return Spell.Type.POISON_BOLT.equals(spellType);
		case WEAKEN_NON_MATERIAL_BEINGS_SPELL:
			return Spell.Type.WEAKEN_IMMATERIAL.equals(spellType);
		default:
			return false;
		}
	}

	/**
	 * Tells whether a poison cloud matches this weakness.
	 *
	 * @return whether a poison cloud matches this weakness.
	 */
	public boolean acceptsPoisonCloud() {
		return equals(POISON_CLOUD);
	}
}