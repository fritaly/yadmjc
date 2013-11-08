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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion.Level;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * Handles the experience of a champion in for a given skill.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Experience implements ChangeEventSource {

	private final Log log = LogFactory.getLog(Experience.class);

	/**
	 * The skill whose experience is managed.
	 */
	private final Skill skill;

	/**
	 * The current experience points.
	 */
	private int points;

	/**
	 * The current level in this skill.
	 */
	private Level level;

	/**
	 * The possible temporary (level) boost. This value can be positive, zero or
	 * negative. A boost of "+1" means the level is boosted to the following
	 * level.
	 */
	private int boost;

	/**
	 * Support class used for firing change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The champion whose experience is managed.
	 */
	private final Champion champion;

	public Experience(Champion champion, Skill skill, Level level) {
		Validate.notNull(champion, "The given champion is null");
		Validate.notNull(skill, "The given skill is null");
		Validate.notNull(level, "The given level is null");

		this.champion = champion;
		this.skill = skill;
		this.level = level;
		this.points = level.getLowerBound();
	}

	/**
	 * Returns the skill associated to this experience.
	 *
	 * @return a skill. Never returns null.
	 */
	public Skill getSkill() {
		return skill;
	}

	/**
	 * Returns the level corresponding to the current experience points. Note:
	 * this level is not the actual level used in game as the level can be
	 * temporarily boosted.
	 *
	 * @return a level. Never returns null.
	 * @see #getActualLevel()
	 */
	public Champion.Level getLevel() {
		return level;
	}

	/**
	 * Sets the champion's experience level from the given value.
	 *
	 * @param level
	 *            the level to set. Can't be null.
	 */
	public void setLevel(Champion.Level level) {
		Validate.notNull(level, "The given level is null");

		if (this.level != Level.NONE) {
			// The level can be set only once from NONE to another value
			throw new IllegalStateException(champion.getName() + "'s " + skill + " level is already defined");
		}

		if (this.level != level) {
			this.level = level;

			// Set the min xp points corresponding to the level set
			this.points = level.getLowerBound();

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.%s.Level: %s (xp: %d points)", champion.getName(), skill.getLabel(), level,
						level.getLowerBound()));
			}
		}
	}

	// TODO Define an aspect to enforce the rule below
	// This method should only be called from the champion's class
	void gain(final int xp) {
		Validate.isTrue(xp > 0, String.format("The experience points (%d) must be positive", points));

		final int oldPoints = points;

		points += xp;

		if (log.isDebugEnabled()) {
			// Delta is always positive
			log.debug(String.format("%s.%s.Experience: %d -> %d [+%d]", champion.getName(), skill.getLabel(), oldPoints, points, xp));
		}

		// WARNING ! The end value if out of the range of points
		if (points > level.getUpperBound()) {
			final Level oldLevel = this.level;

			// The champion levelled up
			this.level = Level.fromExperience(points);

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.%s.Level: %s -> %s [%+d]", champion.getName(), skill.getLabel(), oldLevel, level,
						(level.ordinal() - oldLevel.ordinal())));
			}

			// Randomly increase the champion's stats depending on the skill
			// improved. Depending on the number of levels gained, we may need
			// to increase the stats several times (hence the loop)
			for (int i = 0; i < this.level.ordinal() - oldLevel.ordinal(); i++) {
				skill.improve(champion.getStats());
			}

			// FIXME Fire an event "<champion> gained a <skill> level"
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

	/**
	 * Returns the current experience points.
	 *
	 * @return a positive integer representing some experience points.
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Returns the current level boost.
	 *
	 * @return an integer representing the current level boost. Zero means the
	 *         level isn't boosted.
	 */
	public int getBoost() {
		return boost;
	}

	/**
	 * Increases the level boost by the given amount and returns the updated
	 * level boost.
	 *
	 * @param n
	 *            the amount added to the level boost.
	 * @return the updated level boost.
	 */
	public int incBoost(int n) {
		if (n != 0) {
			final int oldValue = this.boost;

			this.boost += n;

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.%s.Boost: %d -> %d [%d]", champion.getName(), skill.getLabel(), oldValue, this.boost,
						n));
			}

			// Notify the change
			fireChangeEvent();
		}

		return this.boost;
	}

	/**
	 * Decreases the level boost by the given amount and returns the updated
	 * level boost.
	 *
	 * @param n
	 *            the amount removed from the level boost.
	 * @return the updated level boost.
	 */
	public int decBoost(int n) {
		if (n != 0) {
			final int oldValue = this.boost;

			this.boost -= n;

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.%s.Boost: %d -> %d [%d]", champion.getName(), skill.getLabel(), oldValue, this.boost,
						n));
			}

			// Notify the change
			fireChangeEvent();
		}

		return this.boost;
	}

	/**
	 * Tells whether the level is currently boosted.
	 *
	 * @return whether the level is currently boosted.
	 */
	public boolean isBoosted() {
		return (boost != 0);
	}

	/**
	 * Returns the actual experience level taking into account the possible level boost.
	 *
	 * @return the actual level. Never returns null.
	 */
	public Level getActualLevel() {
		if (boost == 0) {
			// No boost, the actual level is the "base" level
			return level;
		}

		// Compute the actual level including the level boost (which can be negative)
		// The final level must be in range [0,15].
		final int index = Math.min(15, Math.max(0, level.ordinal() + boost));

		return Level.values()[index];
	}
}
