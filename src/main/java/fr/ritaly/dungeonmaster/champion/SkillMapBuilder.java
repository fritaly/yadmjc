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
package fr.ritaly.dungeonmaster.champion;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Skill;

/**
 * A builder class used for simplifying the way champion skills are defined.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class SkillMapBuilder {

	/**
	 * Map containing the skills being configured and their associated level.
	 */
	private final Map<Skill, Champion.Level> skills = new LinkedHashMap<Skill, Champion.Level>();

	public SkillMapBuilder() {
	}

	private void validateLevel(final String name, final int level) {
		Validate.isTrue((0 <= level) && (level <= 15), String.format("The given %s level (%d) must be in [0,15]", name, level));
	}

	private Champion.Level getLevel(int level) {
		return Champion.Level.values()[level];
	}

	private SkillMapBuilder setFighterSkills(Champion.Level level, int swingLevel, int thrustLevel, int clubLevel, int parryLevel) {
		Validate.notNull(level, "The given level is null");
		validateLevel("swing", swingLevel);
		validateLevel("thrust", thrustLevel);
		validateLevel("club", clubLevel);
		validateLevel("parry", parryLevel);

		// Set the basic skill
		skills.put(Skill.FIGHTER, level);

		// ... and its associated hidden skills
		skills.put(Skill.SWING, getLevel(swingLevel));
		skills.put(Skill.THRUST, getLevel(thrustLevel));
		skills.put(Skill.CLUB, getLevel(clubLevel));
		skills.put(Skill.PARRY, getLevel(parryLevel));

		return this;
	}

	private SkillMapBuilder setPriestSkills(Champion.Level level, int identifyLevel, int healLevel, int influenceLevel,
			int defendLevel) {

		Validate.notNull(level, "The given level is null");
		validateLevel("identify", identifyLevel);
		validateLevel("heal", healLevel);
		validateLevel("influence", influenceLevel);
		validateLevel("defend", defendLevel);

		// Set the basic skill
		skills.put(Skill.PRIEST, level);

		// ... and its associated hidden skills
		skills.put(Skill.IDENTIFY, getLevel(identifyLevel));
		skills.put(Skill.HEAL, getLevel(healLevel));
		skills.put(Skill.INFLUENCE, getLevel(influenceLevel));
		skills.put(Skill.DEFEND, getLevel(defendLevel));

		return this;
	}

	private SkillMapBuilder setNinjaSkills(Champion.Level level, int stealLevel, int fightLevel, int throwLevel, int shootLevel) {
		Validate.notNull(level, "The given level is null");
		validateLevel("steal", stealLevel);
		validateLevel("fight", fightLevel);
		validateLevel("throw", throwLevel);
		validateLevel("shoot", shootLevel);

		// Set the basic skill
		skills.put(Skill.NINJA, level);

		// ... and its associated hidden skills
		skills.put(Skill.STEAL, getLevel(stealLevel));
		skills.put(Skill.FIGHT, getLevel(fightLevel));
		skills.put(Skill.THROW, getLevel(throwLevel));
		skills.put(Skill.SHOOT, getLevel(shootLevel));

		return this;
	}

	private SkillMapBuilder setWizardSkills(Champion.Level level, int fireLevel, int airLevel, int earthLevel, int waterLevel) {
		Validate.notNull(level, "The given level is null");
		validateLevel("fire", fireLevel);
		validateLevel("air", airLevel);
		validateLevel("earth", earthLevel);
		validateLevel("water", waterLevel);

		// Set the basic skill
		skills.put(Skill.WIZARD, level);

		// ... and its associated hidden skills
		skills.put(Skill.FIRE, getLevel(fireLevel));
		skills.put(Skill.AIR, getLevel(airLevel));
		skills.put(Skill.EARTH, getLevel(earthLevel));
		skills.put(Skill.WATER, getLevel(waterLevel));

		return this;
	}

	public SkillMapBuilder setSkills(Skill skill, Champion.Level level, int level1, int level2, int level3, int level4) {
		Validate.notNull(skill, "The given skill is null");
		Validate.isTrue(skill.isBasic());
		Validate.notNull(level, "The given level is null");
		validateLevel("first", level1);
		validateLevel("second", level2);
		validateLevel("third", level3);
		validateLevel("fourth", level4);

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

	public Map<Skill, Champion.Level> getSkills() {
		// Defensive recopy
		return new LinkedHashMap<Skill, Champion.Level>(skills);
	}
}