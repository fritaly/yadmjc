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
package fr.ritaly.dungeonmaster;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration of champion skills. A {@link Skill} is either basic or hidden.
 * There are 4 basic skills ({@link #FIGHTER}, {@link #NINJA}, {@link #PRIEST}
 * and {@link #WIZARD}) and 16 hidden skills (4 per basic skill).
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum Skill {
	// --- Compétences basiques --- //
	FIGHTER,
	NINJA,
	PRIEST,
	WIZARD,
	// --- Compétences cachées de FIGHTER --- //
	SWING,
	THRUST,
	CLUB,
	PARRY,
	// --- Compétences cachées de NINJA --- //
	STEAL,
	FIGHT,
	THROW,
	SHOOT,
	// --- Compétences cachées de PRIEST --- //
	IDENTIFY,
	HEAL,
	INFLUENCE,
	DEFEND,
	// --- Compétences cachées de WIZARD --- //
	FIRE,
	AIR,
	EARTH,
	WATER;

	/**
	 * Tells whether this {@link Skill} is basic (or hidden).
	 * 
	 * @return whether this {@link Skill} is basic (or hidden).
	 */
	public boolean isBasic() {
		switch (this) {
		case FIGHTER:
		case NINJA:
		case PRIEST:
		case WIZARD:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tells whether this {@link Skill} is hidden (or basic).
	 * 
	 * @return whether this {@link Skill} is hidden (or basic).
	 */
	public boolean isHidden() {
		return !isBasic();
	}

	/**
	 * Returns the basic {@link Skill} mapped to this hidden {@link Skill}. This
	 * method throws an {@link UnsupportedOperationException} if this skill
	 * isn't basic.
	 * 
	 * @return a {@link Skill}.
	 */
	public Skill getRelatedSkill() {
		if (isBasic()) {
			throw new UnsupportedOperationException();
		}

		switch (this) {
		case SWING:
		case THRUST:
		case CLUB:
		case PARRY:
			return FIGHTER;
		case STEAL:
		case FIGHT:
		case THROW:
		case SHOOT:
			return NINJA;
		case IDENTIFY:
		case HEAL:
		case INFLUENCE:
		case DEFEND:
			return PRIEST;
		case FIRE:
		case AIR:
		case EARTH:
		case WATER:
			return WIZARD;
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Tells whether the champion's strength improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's strength improves when the champion gains
	 *         a new level of this {@link Skill}.
	 */
	public boolean improvesStrength() {
		return equals(FIGHTER) || equals(NINJA);
	}

	/**
	 * Tells whether the champion's dexterity improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's dexterity improves when the champion gains
	 *         a new level of this {@link Skill}.
	 */
	public boolean improvesDexterity() {
		return equals(FIGHTER) || equals(NINJA);
	}

	/**
	 * Tells whether the champion's mana improves when the champion gains a new
	 * level of this {@link Skill}.
	 * 
	 * @return whether the champion's mana improves when the champion gains a
	 *         new level of this {@link Skill}.
	 */
	public boolean improvesMana() {
		return equals(PRIEST) || equals(WIZARD);
	}

	/**
	 * Tells whether the champion's wisdom improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's wisdom improves when the champion gains a
	 *         new level of this {@link Skill}.
	 */
	public boolean improvesWisdom() {
		return equals(PRIEST) || equals(WIZARD);
	}

	/**
	 * Tells whether the champion's anti-magic improves when the champion gains
	 * a new level of this {@link Skill}.
	 * 
	 * @return whether the champion's anti-magic improves when the champion
	 *         gains a new level of this {@link Skill}.
	 */
	public boolean improvesAntiMagic() {
		return equals(PRIEST) || equals(WIZARD);
	}

	/**
	 * Tells whether the champion's health improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's health improves when the champion gains a
	 *         new level of this {@link Skill}.
	 */
	public boolean improvesHealth() {
		return true;
	}

	/**
	 * Tells whether the champion's stamina improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's stamina improves when the champion gains a
	 *         new level of this {@link Skill}.
	 */
	public boolean improvesStamina() {
		return true;
	}

	/**
	 * Tells whether the champion's vitality improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's vitality improves when the champion gains
	 *         a new level of this {@link Skill}.
	 */
	public boolean improvesVitality() {
		return true;
	}

	/**
	 * Tells whether the champion's anti-fire improves when the champion gains a
	 * new level of this {@link Skill}.
	 * 
	 * @return whether the champion's anti-fire improves when the champion gains
	 *         a new level of this {@link Skill}.
	 */
	public boolean improvesAntiFire() {
		return true;
	}

	/**
	 * Returns this {@link Skill}'s label. Example: Returns "Ninja" for the
	 * {@link #NINJA} skill.
	 * 
	 * @return a {@link String}.
	 */
	public String getLabel() {
		return StringUtils.capitalize(name().toLowerCase());
	}
}
