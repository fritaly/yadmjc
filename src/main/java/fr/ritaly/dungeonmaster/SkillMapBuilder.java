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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Level;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class SkillMapBuilder {

	private final Map<Skill, Level> skills = new LinkedHashMap<Skill, Champion.Level>();

	public SkillMapBuilder() {
	}

	private SkillMapBuilder setFighterSkills(Level level, int swingLevel,
			int thrustLevel, int clubLevel, int parryLevel) {

		Validate.notNull(level);
		Validate.isTrue(swingLevel >= 0 && swingLevel <= 15);
		Validate.isTrue(thrustLevel >= 0 && thrustLevel <= 15);
		Validate.isTrue(clubLevel >= 0 && clubLevel <= 15);
		Validate.isTrue(parryLevel >= 0 && parryLevel <= 15);

		// Compétence basique
		skills.put(Skill.FIGHTER, level);

		// Compétences cachées
		skills.put(Skill.SWING, Level.values()[swingLevel]);
		skills.put(Skill.THRUST, Level.values()[thrustLevel]);
		skills.put(Skill.CLUB, Level.values()[clubLevel]);
		skills.put(Skill.PARRY, Level.values()[parryLevel]);

		return this;
	}

	private SkillMapBuilder setPriestSkills(Level level, int identifyLevel,
			int healLevel, int influenceLevel, int defendLevel) {

		Validate.notNull(level);
		Validate.isTrue(identifyLevel >= 0 && identifyLevel <= 15);
		Validate.isTrue(healLevel >= 0 && healLevel <= 15);
		Validate.isTrue(influenceLevel >= 0 && influenceLevel <= 15);
		Validate.isTrue(defendLevel >= 0 && defendLevel <= 15);

		// Compétence basique
		skills.put(Skill.PRIEST, level);

		// Compétences cachées
		skills.put(Skill.IDENTIFY, Level.values()[identifyLevel]);
		skills.put(Skill.HEAL, Level.values()[healLevel]);
		skills.put(Skill.INFLUENCE, Level.values()[influenceLevel]);
		skills.put(Skill.DEFEND, Level.values()[defendLevel]);

		return this;
	}

	private SkillMapBuilder setNinjaSkills(Level level, int stealLevel,
			int fightLevel, int throwLevel, int shootLevel) {

		Validate.notNull(level);
		Validate.isTrue(stealLevel >= 0 && stealLevel <= 15);
		Validate.isTrue(fightLevel >= 0 && fightLevel <= 15);
		Validate.isTrue(throwLevel >= 0 && throwLevel <= 15);
		Validate.isTrue(shootLevel >= 0 && shootLevel <= 15);

		// Compétence basique
		skills.put(Skill.NINJA, level);

		// Compétences cachées
		skills.put(Skill.STEAL, Level.values()[stealLevel]);
		skills.put(Skill.FIGHT, Level.values()[fightLevel]);
		skills.put(Skill.THROW, Level.values()[throwLevel]);
		skills.put(Skill.SHOOT, Level.values()[shootLevel]);

		return this;
	}

	private SkillMapBuilder setWizardSkills(Level level, int fireLevel,
			int airLevel, int earthLevel, int waterLevel) {

		Validate.notNull(level);
		Validate.isTrue(fireLevel >= 0 && fireLevel <= 15);
		Validate.isTrue(airLevel >= 0 && airLevel <= 15);
		Validate.isTrue(earthLevel >= 0 && earthLevel <= 15);
		Validate.isTrue(waterLevel >= 0 && waterLevel <= 15);

		// Compétence basique
		skills.put(Skill.WIZARD, level);

		// Compétences cachées
		skills.put(Skill.FIRE, Level.values()[fireLevel]);
		skills.put(Skill.AIR, Level.values()[airLevel]);
		skills.put(Skill.EARTH, Level.values()[earthLevel]);
		skills.put(Skill.WATER, Level.values()[waterLevel]);

		return this;
	}

	public SkillMapBuilder setSkills(Skill skill, Level level, int level1,
			int level2, int level3, int level4) {

		Validate.notNull(skill);
		Validate.isTrue(skill.isBasic());
		Validate.notNull(level);
		Validate.isTrue(level1 >= 0 && level1 <= 15);
		Validate.isTrue(level2 >= 0 && level2 <= 15);
		Validate.isTrue(level3 >= 0 && level3 <= 15);
		Validate.isTrue(level4 >= 0 && level4 <= 15);

		switch (skill) {
		case FIGHTER:
			return setFighterSkills(level, level1, level2, level3, level4);
		case WIZARD:
			return setWizardSkills(level, level1, level2, level3, level4);
		case PRIEST:
			return setPriestSkills(level, level1, level2, level3, level4);
		case NINJA:
			return setNinjaSkills(level, level1, level2, level3, level4);
		default:
			throw new IllegalArgumentException("Unsupported skill " + skill);
		}
	}
	
	public Map<Skill, Level> getSkills() {
		// Recopie défensive
		return new LinkedHashMap<Skill, Champion.Level>(skills);
	}
}