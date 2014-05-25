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

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Constants;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.ai.attack.Attack;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Pit;

/**
 * Enumerates the actions that can be attached to items. Actions are never
 * individually tied to an item. Instead actions are grouped into combos and
 * combos are attached to items.<br>
 * <br>
 * Source: <a href="http://dmweb.free.fr/?q=node/690">Technical Documentation -
 * Dungeon Master and Chaos Strikes Back Actions and Combos</a>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Action {
	N(Skill.FIGHTER, 0, 0, 0, 0, 0, 0),
	BLOCK(Skill.PARRY, 8, 36, 4, 22, 15, 6),
	CHOP(Skill.CLUB, 10, 0, 8, 48, 48, 8),
	X(Skill.FIGHTER, 0, 0, 0, 0, 0, 0),
	BLOW_HORN(Skill.INFLUENCE, 0, -4, 1, 0, 0, 6),
	FLIP(Skill.IDENTIFY, 0, -10, 0, 0, 0, 3),
	PUNCH(Skill.FIGHT, 8, -10, 1, 38, 32, 1),
	KICK(Skill.FIGHT, 13, -5, 3, 28, 48, 5),
	WAR_CRY(Skill.INFLUENCE, 7, 4, 1, 0, 0, 3),
	STAB(Skill.FIGHT, 15, -20, 3, 30, 48, 5),
	CLIMB_DOWN(Skill.STEAL, 15, -15, 40, 0, 0, 35),
	FREEZE_LIFE(Skill.INFLUENCE, 22, -10, 3, 0, 0, 20),
	HIT(Skill.FIGHT, 10, 16, 3, 20, 20, 4),
	SWING(Skill.SWING, 6, 5, 2, 32, 16, 6),
	THRUST(Skill.THRUST, 19, -17, 13, 57, 66, 16),
	JAB(Skill.THRUST, 11, -5, 3, 70, 8, 2),
	PARRY(Skill.PARRY, 17, 29, 1, 18, 8, 18),
	HACK(Skill.SWING, 9, 10, 6, 27, 25, 8),
	BERZERK(Skill.SWING, 40, -10, 40, 46, 96, 30),
	FIREBALL(Skill.FIRE, 35, -7, 5, 0, 0, 42),
	DISPELL(Skill.AIR, 25, -7, 2, 0, 0, 31),
	CONFUSE(Skill.INFLUENCE, 0, -7, 2, 0, 0, 10),
	LIGHTNING(Skill.AIR, 30, -7, 4, 0, 0, 38),
	DISRUPT(Skill.AIR, 10, -7, 5, 46, 55, 9),
	MELEE(Skill.CLUB, 24, -5, 25, 64, 60, 20),
	X2(Skill.STEAL, 0, -15, 1, 0, 0, 10),
	INVOKE(Skill.WIZARD, 25, -9, 2, 0, 0, 16),
	SLASH(Skill.SWING, 9, 4, 2, 26, 16, 4),
	CLEAVE(Skill.SWING, 12, 0, 10, 40, 48, 12),
	BASH(Skill.CLUB, 11, 0, 9, 32, 50, 20),
	STUN(Skill.CLUB, 10, 5, 2, 50, 16, 7),
	SHOOT(Skill.SHOOT, 20, -15, 3, 0, 0, 14),
	SPELL_SHIELD(Skill.DEFEND, 20, -7, 1, 0, 0, 30),
	FIRE_SHIELD(Skill.DEFEND, 20, -7, 2, 0, 0, 35),
	FLUX_CAGE(Skill.WIZARD, 12, 8, 6, 0, 0, 2),
	HEAL(Skill.HEAL, 0, -20, 1, 0, 0, 10),
	CALM(Skill.INFLUENCE, 0, -5, 1, 0, 0, 9),
	LIGHT(Skill.AIR, 20, 0, 3, 0, 0, 10),
	WINDOW(Skill.EARTH, 30, -15, 2, 0, 0, 15),
	SPIT(Skill.FIRE, 25, -7, 3, 0, 0, 22),
	BRANDISH(Skill.INFLUENCE, 0, -4, 2, 0, 0, 10),
	THROW(Skill.THROW, 5, 0, 0, 0, 0, 0),
	FUSE(Skill.WIZARD, 1, 8, 2, 0, 0, 2);

	/**
	 * The skill involved and improved every time the action is used.
	 */
	private final Skill skill;

	/**
	 * The experience gained by a champion when successfully using this action.
	 */
	private final int experienceGain;

	/**
	 * The shield protection provided by this action.
	 */
	private final int shieldModifier;

	/**
	 * The amount of stamina consumed when the champion uses this action.
	 */
	private final int stamina;

	/**
	 * The base probability of hitting an enemy when using this action. Value
	 * within [0,75].
	 */
	private final int hitProbability;

	/**
	 * The base damage points inflicted when the action succeeds.
	 */
	private final int damage;

	/**
	 * The fatigue generated when using this action. The higher the value, the
	 * longer the weapon hand will be unavailable. This value actually
	 * represents a duration in number of clock ticks.
	 */
	private final int fatigue;

	private Action(Skill skill, int experienceGain, int shieldModifier, int stamina, int hitProbability, int damage, int fatigue) {
		Validate.notNull(skill, "The given skill is null");

		this.skill = skill;
		this.experienceGain = experienceGain;
		this.shieldModifier = shieldModifier;
		this.stamina = stamina;
		this.hitProbability = hitProbability;
		this.damage = damage;
		this.fatigue = fatigue;
	}

	/**
	 * Tells whether the action succeeded by taking into account the action's
	 * hit probability. The result of this method is random. This method is only
	 * relevant for an attack action (it returns false for the non-attack
	 * actions).
	 *
	 * @return whether the action succeeded.
	 */
	public boolean isSuccess() {
		if (hitProbability == 75) {
			// The attack always succeds
			return true;
		}

		return (Utils.random(0, 75) <= hitProbability);
	}

	public Skill getSkill() {
		return skill;
	}

	public int getExperienceGain() {
		return experienceGain;
	}

	public int getShieldModifier() {
		return shieldModifier;
	}

	public int getStamina() {
		return stamina;
	}

	public int getHitProbability() {
		return hitProbability;
	}

	public int getDamage() {
		return damage;
	}

	public int getFatigue() {
		return fatigue;
	}

	private int computeEarnedExperience(Dungeon dungeon) {
		// See Magic.cpp

		// How much experience was gained ?
		int xp = experienceGain;

		// We need to know when the champion was attacked for the last time
		final int currentTick = Clock.getInstance().getTickId();
		final int lastAttackTick = dungeon.getParty().getLastAttackTick();

		// Whether the party was attacked in the latest 150 clock ticks by a
		// creature
		final boolean attackedDuringLast150Ticks = (currentTick - lastAttackTick) <= 150;

		// Whether the party was attacked in the latest 25 clock ticks by a
		// creature
		final boolean attackedDuringLast25Ticks = (currentTick - lastAttackTick) <= 25;

		if (skill.isHidden()) {
			// The improved skill is hidden, what's the related skill improved ?
			final Skill relatedSkill = skill.getRelatedSkill();

			if (Skill.FIGHTER.equals(relatedSkill) || Skill.NINJA.equals(relatedSkill)) {
				if (!attackedDuringLast150Ticks) {
					xp = xp / 2;
				}
			}
		}

		// Take into account the current level's xp multiplier
		final int experienceMultiplier = dungeon.getCurrentLevel().getExperienceMultiplier();

		if (experienceMultiplier != 0) {
			xp = xp * experienceMultiplier;
		}
		if (skill.isHidden() && attackedDuringLast25Ticks) {
			xp = xp * 2;
		}

		return xp;
	}

	// TODO Refactor the following code (use a template method to clean this up)

	/**
	 * Performs the action with the given context (dungeon and champion) and
	 * returns whether the operation succeeded.
	 *
	 * @param dungeon
	 *            the dungeon where the action is used. Can't be null.
	 * @param champion
	 *            the champion using the action. Can be null depending on the
	 *            action.
	 * @return whether the action was successfully performed or if the action
	 *         failed.
	 */
	public boolean perform(Dungeon dungeon, Champion champion) {
		// The champion can be null depending on the action
		Validate.notNull(dungeon, "The given dungeon is null");

		if (champion != null) {
			// If there's a champion, it must be alive
			Validate.isTrue(champion.isAlive(), "The given champion is dead");
		}

		// This method returns early if the action isn't relevant. This prevents
		// consuming the champion's stamina, etc when irrelevant

		// What's the item that triggered this action ? It's in the champion's
		// weapon hand by definition
		final Item item = champion.getBody().getWeaponHand().getItem();

		if (item == null) {
			// Can't happen
			throw new IllegalStateException("The given champion " + champion.getName()
					+ " doesn't hold any item in its weapon hand");
		}

		// Whether the action succeeded
		final boolean success;

		switch(this) {
		case CLIMB_DOWN: {
			// Retrieve the element facing the party
			final Element facingElement = dungeon.getElement(dungeon.getParty().getFacingPosition());

			if (!(facingElement instanceof Pit)) {
				// The party can only go down into a pit
				return false;
			}

			final Pit pit = (Pit) facingElement;

			if (pit.isIllusion() || pit.isClosed()) {
				// The pit is a fake or is closed, the action fails
				return false;
			}

			// Set the state to CLIMBING_DOWN to distinguish from a plain fall
			champion.getParty().setState(Party.State.CLIMBING_DOWN);

			// Move forward to the pit. The pit will teleport the party to the
			// level below as the party's not falling. If there are several
			// "stacked" pits, this use case is managed by method
			// Dungeon.teleportParty()
			dungeon.teleportParty(dungeon.getParty().getFacingPosition(), dungeon.getParty().getLookDirection(), true);

			// Restore the initial state
			champion.getParty().setState(Party.State.NORMAL);

			success = true;
			break;
		}
		case THROW: {
			champion.throwItem(item);

			// Remove the item from the champion's hand
			champion.getBody().getWeaponHand().takeOff();

			success = true;
			break;
		}
		case HEAL: {
			// This action heals the whole party
			final int healPoints = Utils.random(10, 30);

			// Only living champions can be healed. Dead champions have to be
			// resurrected first
			for (final Champion aChampion : dungeon.getParty().getChampions(false)) {
				aChampion.getStats().getHealth().inc(healPoints);
			}

			success = true;
			break;
		}
		case FLUX_CAGE: {
			// Create a flux cage on the neighbor position in front of the party
			final Element element = dungeon.getElement(dungeon.getParty().getFacingPosition());

			if (!element.isFluxCageAllowed()) {
				return false;
			}

			// TODO JUNIT: Test the elements where a flux cage can be created
			// Ensure the target element can contain a flux cage !
			element.createFluxCage();

			success = true;
			break;
		}
		case BASH:
		case BERZERK:
		case CHOP:
		case CLEAVE:
		case HACK:
		case HIT:
		case JAB:
		case MELEE:
		case KICK:
		case PARRY:
		case PUNCH:
		case SLASH:
		case STAB:
		case SWING:
		case THRUST: {
			// Those are all the attack actions. Did the action succeed ?
			success = isSuccess();

			if (success) {
				// TODO Compute the damage points and the target
			}
			break;
		}
		case BLOCK: {
			// Did the action succeed ?
			success = isSuccess();

			if (success) {
				// The block succeeds, the champion's shield increases for a
				// short period
				champion.getStats().getShield().incBoost(shieldModifier, fatigue);
			}

			break;
		}
		case LIGHT: {
			// TODO Amount of light generated ? At first glance, 5 casts required to get the full light
			// The light action always succeeds
			success = true;

			champion.getSpells().getLight().inc(Constants.MAX_LIGHT / 5);
			break;
		}
		default:
			throw new UnsupportedOperationException("Method unsupported for action " + this);
		}

		// FIXME Identify the remaining actions to implement

		// TODO Play a sound

		if (stamina > 0) {
			// The champion consumed some stamina
			champion.getStats().getStamina().dec(stamina);
		}
		if (fatigue > 0) {
			// TODO JUNIT: Test that the weapon hand is unavailable for an action
			// The weapon hand is unavailable for a short time corresponding to
			// the item's fatigue
			champion.getBody().getWeaponHand().disable(fatigue);
		}
		if ((shieldModifier != 0) && !equals(BLOCK)) {
			// TODO JUNIT: Test that the defense is temporarily decreased
			// The champion's shield stat is temporarily affected by this action
			// (the modifier is negative for most actions)
			champion.getStats().getShield().incBoost(shieldModifier, fatigue);
		}

		if (success) {
			// The champion gained some experience (the points can be zero !)
			final int xp = computeEarnedExperience(dungeon);

			if (xp > 0) {
				// The method gainExperience() throws an exception if the xp is zero
				champion.gainExperience(skill, xp);
			}
		}

		return success;
	}

	public Attack getAttack() {
		// TODO Implement Action.getAttack()
		// See Attack.cpp
		// The attack depends on:
		// - the level.experienceMultiplier = level.difficulty
		// - the weapon used
		// - the creature materiality
		// - the champion's quickness
		// - the creature type
		// -> decrement of stamina = 2 + random(1)
		throw new UnsupportedOperationException("Method not yet implemented");
	}
}