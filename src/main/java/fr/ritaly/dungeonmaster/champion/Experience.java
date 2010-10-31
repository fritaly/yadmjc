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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.champion.Champion.Level;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.stat.Stats;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Experience implements ChangeEventSource {

	private final Log log = LogFactory.getLog(Experience.class);

	private final Skill skill;

	private int points;

	private Level level;

	private int boost;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	private final Champion champion;

	public Experience(Champion champion, Skill skill, Level level) {
		if (champion == null) {
			throw new IllegalArgumentException("The given champion is null");
		}
		if (skill == null) {
			throw new IllegalArgumentException("The given skill is null");
		}
		if (level == null) {
			throw new IllegalArgumentException("The given level is null");
		}

		this.champion = champion;
		this.skill = skill;
		this.level = level;
		this.points = level.getStartRange();
	}

	public Skill getSkill() {
		return skill;
	}

	public Champion.Level getLevel() {
		return level;
	}

	public void setLevel(Champion.Level level) {
		if (level == null) {
			throw new IllegalArgumentException("The given level is null");
		}

		if (this.level != Level.NONE) {
			// On n'autorise l'opération que si le level existant est à NONE !
			throw new IllegalStateException(champion.getName() + "'s " + skill
					+ " level is already defined");
		}

		if (this.level != level) {
			// this.level vaut forcément NONE à ce stade
			this.level = level;

			// Initialiser le nombre de points de départ
			this.points = level.getStartRange();

			if (log.isDebugEnabled()) {
				log.debug(champion.getName() + "." + skill.getLabel()
						+ ".Level: " + level + " (xp: " + level.getStartRange()
						+ " points)");
			}
		}
	}

	// Ne doit pouvoir être appelée que depuis la classe Champion
	void gain(int xp) {
		if (xp <= 0) {
			throw new IllegalArgumentException("The points must be positive");
		}

		final int oldPoints = points;

		points += xp;

		// Delta forcément positif
		final int delta = points - oldPoints;

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + "." + skill.getLabel()
					+ ".Experience: " + oldPoints + " -> " + points + " [+"
					+ delta + "]");
		}

		// ATTENTION ! La valeur de fin est exclue de l'intervalle !
		if (points > level.getEndRange()) {
			final Level oldLevel = this.level;

			// Changement de niveau
			this.level = Level.fromExperience(points);

			if (log.isDebugEnabled()) {
				log.debug(champion.getName() + "." + skill.getLabel()
						+ ".Level: " + oldLevel + " -> " + level + " [+"
						+ (level.ordinal() - oldLevel.ordinal()) + "]");
			}

			// Augmenter les stats du champion en fonction de la compétence
			// améliorée. On augmente autant de fois les Stats que le
			// champion a gagné de niveau (pour les tests) !!!
			for (int i = 0; i < this.level.ordinal() - oldLevel.ordinal(); i++) {
				final Stats stats = champion.getStats();

				if (skill.improvesHealth()) {
					final int healthBonus = Utils.random(5, 15);

					stats.getHealth().incMax(healthBonus);
					stats.getHealth().inc(healthBonus);
				}
				if (skill.improvesStamina()) {
					final int staminaBonus = Utils.random(5, 15);

					stats.getStamina().incMax(staminaBonus);
					stats.getStamina().inc(staminaBonus);
				}
				if (skill.improvesVitality()) {
					final int vitalityBonus = Utils.random(5, 15);

					stats.getVitality().incMax(vitalityBonus);
					stats.getVitality().inc(vitalityBonus);
				}
				if (skill.improvesAntiFire()) {
					final int antiFireBonus = Utils.random(5, 15);

					stats.getAntiFire().incMax(antiFireBonus);
					stats.getAntiFire().inc(antiFireBonus);
				}
				if (skill.improvesStrength()) {
					final int strengthBonus = Utils.random(5, 15);

					stats.getStrength().incMax(strengthBonus);
					stats.getStrength().inc(strengthBonus);
				}
				if (skill.improvesDexterity()) {
					final int dexterityBonus = Utils.random(5, 15);

					stats.getDexterity().incMax(dexterityBonus);
					stats.getDexterity().inc(dexterityBonus);
				}
				if (skill.improvesMana()) {
					final int manaBonus = Utils.random(5, 15);

					stats.getMana().incMax(manaBonus);
					stats.getMana().inc(manaBonus);
				}
				if (skill.improvesWisdom()) {
					final int wisdomBonus = Utils.random(5, 15);

					stats.getWisdom().incMax(wisdomBonus);
					stats.getWisdom().inc(wisdomBonus);
				}
				if (skill.improvesAntiMagic()) {
					final int antiMagicBonus = Utils.random(5, 15);

					stats.getAntiMagic().incMax(antiMagicBonus);
					stats.getAntiMagic().inc(antiMagicBonus);
				}
			}

			// FIXME Lever un event "X gained a Y level"
			fireChangeEvent();
		}
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	private void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	public int getPoints() {
		return points;
	}

	public int getBoost() {
		return boost;
	}

	public int incBoost(int n) {
		if (n != 0) {
			final int oldValue = this.boost;

			this.boost += n;

			if (log.isDebugEnabled()) {
				log.debug(champion.getName() + "." + skill.getLabel()
						+ ".Boost: " + oldValue + " -> " + this.boost + " "
						+ (n < 0 ? "[" + n + "]" : "[+" + n + "]"));
			}
		}

		return this.boost;
	}

	public int decBoost(int n) {
		if (n != 0) {
			final int oldValue = this.boost;

			this.boost -= n;

			if (log.isDebugEnabled()) {
				log.debug(champion.getName() + "." + skill.getLabel()
						+ ".Boost: " + oldValue + " -> " + this.boost + " "
						+ (n < 0 ? "[" + n + "]" : "[+" + n + "]"));
			}
		}

		return this.boost;
	}

	public boolean isBoosted() {
		return (boost != 0);
	}

	/**
	 * Retourne le niveau <b>réel</b> dans la compétence associée. La valeur
	 * retournée prend donc en compte l'éventuel boost de compétence.
	 * 
	 * @return une instance de {@link Level}. Ne retourne jamais null.
	 */
	public Level getActualLevel() {
		if (boost == 0) {
			// Pas de bonus de compétence
			return level;
		}

		// Prendre en compte le bonus de compétence. Attention le boost peut
		// être négatif ! Retourner une valeur dans l'intervalle [0-15]
		final int index = Math.min(15, Math.max(0, level.ordinal() + boost));

		return Level.values()[index];
	}
}
