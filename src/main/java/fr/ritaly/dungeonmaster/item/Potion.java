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
package fr.ritaly.dungeonmaster.item;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.stat.Stats;

/**
 * A potion item. This type of item is consumable.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Potion extends Item {

	/**
	 * The strength of the potion as a power rune. Never null.
	 */
	private final PowerRune strength;

	/**
	 * Creates a new potion with the given type and strength.
	 *
	 * @param type
	 *            the type of potion to create. Can't be null.
	 * @param strength
	 *            a power rune representing the strength of the potion. Can't be
	 *            null.
	 */
	public Potion(Type type, PowerRune strength) {
		super(type);

		Validate.isTrue(Category.POTION.getTypes().contains(type), "The given item type " + type + " isn't a potion");
		Validate.notNull(strength, "The given power rune is null");

		this.strength = strength;
	}

	/**
	 * Creates a new potion with the given type and with strength LO.
	 *
	 * @param type
	 *            the type of potion to create. Can't be null.
	 */
	public Potion(Type type) {
		this(type, PowerRune.LO);
	}

	/**
	 * Returns the type for the potion item associated to the given spell type.
	 *
	 * @param spellType
	 *            the spell type whose associated potion type is requested.
	 *            Can't be null.
	 * @return an item type corresponding to the potion associated to the given
	 *         spell type.
	 */
	private static Item.Type getItemType(final Spell.Type spellType) {
		Validate.notNull(spellType, "The given spell type is null");
		Validate.isTrue(spellType.isPotion(), "The given spell type <" + spellType + "> doesn't create a potion");

		switch (spellType) {
		case ANTIDOTE_POTION:
			return Item.Type.ANTIDOTE_POTION;
		case DEXTERITY_POTION:
			return Item.Type.DEXTERITY_POTION;
		case HEALTH_POTION:
			return Item.Type.HEALTH_POTION;
		case MANA_POTION:
			return Item.Type.MANA_POTION;
		case POISON_POTION:
			return Item.Type.POISON_POTION;
		case SHIELD_POTION:
			return Item.Type.ANTI_MAGIC_POTION;
		case STAMINA_POTION:
			return Item.Type.STAMINA_POTION;
		case STRENGTH_POTION:
			return Item.Type.STRENGTH_POTION;
		case VITALITY_POTION:
			return Item.Type.VITALITY_POTION;
		case WISDOM_POTION:
			return Item.Type.WISDOM_POTION;
		default:
			throw new UnsupportedOperationException("Unsupported spell type " + spellType);
		}
	}

	/**
	 * Creates a new potion from the given spell.
	 *
	 * @param spell
	 *            a potion spell. Can't be null.
	 */
	public Potion(Spell spell) {
		this(getItemType(spell.getType()), spell.getPower());
	}

	@Override
	protected BodyPart.Type getActivationBodyPart() {
		// Potions are never activated
		return null;
	}

	@Override
	public int getAntiMagic() {
		return 0;
	}

	@Override
	public int getShield() {
		return 0;
	}

	@Override
	protected Item consume(final Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		final Stats stats = champion.getStats();

		// FIXME Refine the values below ?
		final int points = Utils.random(3, 15) * strength.getPowerLevel();
		final int duration = strength.getPowerLevel() * Utils.random(60, 90);

		// Change the champion's stats depending on the consumed potion
		switch (getType()) {
		case DEXTERITY_POTION:
			stats.getDexterity().incBoost(points, duration);
			break;
		case STRENGTH_POTION:
			stats.getStrength().incBoost(points, duration);
			break;
		case WISDOM_POTION:
			stats.getWisdom().incBoost(points, duration);
			break;
		case VITALITY_POTION:
			stats.getVitality().incBoost(points, duration);
			break;
		case ANTIDOTE_POTION:
			champion.cure(strength);
			break;
		case STAMINA_POTION:
			stats.getStamina().incBoost(points, duration);
			break;
		case ANTI_MAGIC_POTION:
			stats.getAntiMagic().incBoost(points, duration);
			break;
		case MANA_POTION:
			stats.getMana().incBoost(points, duration);
			break;
		case HEALTH_POTION:
			stats.getHealth().incBoost(points, duration);
			break;
		default:
			throw new UnsupportedOperationException("Unsupported potion type " + getType());
		}

		// Consuming a potion "creates" an empty flask
		return new EmptyFlask();
	}

	/**
	 * Returns the strength of this potion as a power rune.
	 *
	 * @return a power rune. Never returns null.
	 */
	public PowerRune getStrength() {
		return strength;
	}
}