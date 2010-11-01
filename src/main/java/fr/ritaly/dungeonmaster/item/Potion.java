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
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Potion extends Item {

	private final PowerRune powerRune;

	public Potion(Type type, PowerRune powerRune) {
		super(type);

		Validate.isTrue(Category.POTION.getTypes().contains(type),
				"The given item type " + type + " isn't a potion");
		Validate.notNull(powerRune, "The given power rune is null");

		this.powerRune = powerRune;
	}

	public Potion(Type type) {
		this(type, PowerRune.LO);
	}

	private static Item.Type getItemType(Spell.Type spellType) {
		Validate.notNull(spellType, "The given spell type is null");
		Validate.isTrue(spellType.isPotion(), "The given spell type <"
				+ spellType + "> doesn't yield a potion");

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
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Constructeur spécial permettant d'instancier une {@link Potion}
	 * directement à partir du sort.
	 * 
	 * @param spell
	 *            une instance de {@link Spell} représentant un sort de création
	 *            de {@link Potion}.
	 */
	public Potion(Spell spell) {
		this(getItemType(spell.getType()), spell.getPower());
	}

	@Override
	protected BodyPart.Type getActivationBodyPart() {
		// Les potions ne s'activent jamais
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
	protected Item consume(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		final Stats stats = champion.getStats();

		// FIXME Bornes du tirage ?
		final int points = Utils.random(3, 15) * powerRune.getPowerLevel();
		final int duration = powerRune.getPowerLevel() * Utils.random(60, 90); 

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
			champion.cure(powerRune);
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
			throw new UnsupportedOperationException();
		}

		// Consommer une potion retourne une fiole vide
		return new EmptyFlask();
	}

	public PowerRune getPowerRune() {
		return powerRune;
	}
}