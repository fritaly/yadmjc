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

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion.Level;
import fr.ritaly.dungeonmaster.stat.Stats;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class ChampionFactory {

	private static final ChampionFactory FACTORY = new ChampionFactory();

	private ChampionFactory() {
	}

	public static ChampionFactory getFactory() {
		return FACTORY;
	}

	public Champion newChampion(Champion.Name name) {
		if (name == null) {
			throw new IllegalArgumentException(
					"The given champion name is null");
		}

		final Champion champion = new Champion(StringUtils.capitalize(name
				.name().toLowerCase()));
		champion.setGender(name.getGender());

		// Positionner les compétences du champion
		final Map<Skill, Level> skills = name.getSkills();

		for (Skill skill : skills.keySet()) {
			champion.setSkill(skill, skills.get(skill));
		}

		final Stats stats = champion.getStats();

		// TODO Objets dans inventaire
		switch (name) {
		case ALEX:
			stats.init(50, 57, 13, 47, 44, 55, 45, 40, 40, 35);
			break;
		case AZIZI:
			stats.init(61, 77, 7, 47, 47, 48, 42, 45, 35, 30);
			break;
		case BORIS:
			stats.init(35, 65, 28, 35, 35, 45, 55, 40, 40, 45);
			break;
		case CHANI:
			stats.init(47, 67, 20, 57, 37, 47, 57, 37, 37, 47);
			break;
		case DAROOU:
			stats.init(100, 65, 6, 35, 50, 30, 35, 45, 45, 30);
			break;
		case ELIJA:
			stats.init(60, 58, 22, 50, 42, 40, 42, 36, 40, 53);
			break;
		case GANDO:
			stats.init(39, 63, 26, 50, 39, 45, 47, 33, 43, 48);
			break;
		case GOTHMOG:
			stats.init(60, 55, 18, 30, 40, 43, 48, 34, 59, 80);
			break;
		case HALK:
			stats.init(90, 75, 0, 40, 55, 43, 30, 46, 48, 38);
			break;
		case HAWK:
			stats.init(70, 85, 10, 40, 45, 35, 38, 55, 35, 35);
			break;
		case HISSA:
			stats.init(80, 61, 5, 40, 58, 48, 35, 35, 55, 43);
			break;
		case IAIDO:
			stats.init(48, 65, 11, 40, 43, 55, 40, 35, 50, 45);
			break;
		case LEIF:
			stats.init(75, 70, 7, 35, 46, 40, 39, 50, 45, 45);
			break;
		case LEYLA:
			stats.init(48, 60, 3, 50, 40, 53, 45, 47, 35, 45);
			break;
		case LINFLAS:
			stats.init(65, 50, 12, 45, 45, 45, 47, 35, 35, 50);
			break;
		case MOPHUS:
			stats.init(55, 55, 19, 40, 42, 35, 40, 48, 45, 40);
			break;
		case NABI:
			stats.init(55, 65, 15, 40, 41, 36, 45, 45, 55, 55);
			break;
		case SONJA:
			stats.init(65, 70, 2, 40, 54, 45, 39, 49, 40, 40);
			break;
		case STAMM:
			stats.init(75, 80, 0, 35, 52, 43, 35, 50, 55, 35);
			break;
		case SYRA:
			stats.init(53, 72, 15, 55, 38, 35, 43, 45, 40, 42);
			break;
		case TIGGY:
			stats.init(25, 45, 36, 45, 30, 45, 50, 35, 40, 59);
			break;
		case WUTSE:
			stats.init(45, 47, 20, 40, 38, 35, 53, 45, 40, 47);
			break;
		case WUUF:
			stats.init(40, 50, 30, 60, 33, 57, 45, 40, 40, 35);
			break;
		case ZED:
			stats.init(30, 30, 10, 58, 40, 40, 40, 50, 40, 40);
			break;

		default:
			break;
		}

		return champion;
	}
}