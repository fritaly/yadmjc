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
	 * Indique si cette {@link Skill} est basique (ou cachée).
	 * 
	 * @return si cette {@link Skill} est basique (ou cachée).
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
	 * Indique si cette {@link Skill} est cachée (ou basique).
	 * 
	 * @return si cette {@link Skill} est cachée (ou basique).
	 */
	public boolean isHidden() {
		return !isBasic();
	}

	/**
	 * Retourne la {@link Skill} basique associée à cette compétence cachée.
	 * 
	 * @return une instance de {@link Skill}.
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
	 * Indique si un un gain de niveau dans la compétence améliore la force du
	 * champion.
	 * 
	 * @return si un un gain de niveau dans la compétence améliore la force du
	 *         champion.
	 */
	public boolean improvesStrength() {
		return equals(FIGHTER) || equals(NINJA);
	}

	public boolean improvesDexterity() {
		return equals(FIGHTER) || equals(NINJA);
	}

	public boolean improvesMana() {
		return equals(PRIEST) || equals(WIZARD);
	}

	public boolean improvesWisdom() {
		return equals(PRIEST) || equals(WIZARD);
	}

	public boolean improvesAntiMagic() {
		return equals(PRIEST) || equals(WIZARD);
	}
	
	public boolean improvesHealth() {
		return true;
	}
	
	public boolean improvesStamina() {
		return true;
	}
	
	public boolean improvesVitality() {
		return true;
	}
	
	public boolean improvesAntiFire() {
		return true;
	}

	/**
	 * Retourne le libellé de la {@link Skill}. Exemple: Retourne "Ninja" pour
	 * la skill {@link #NINJA}.
	 * 
	 * @return une {@link String}.
	 */
	public String getLabel() {
		return StringUtils.capitalize(name().toLowerCase());
	}
}
