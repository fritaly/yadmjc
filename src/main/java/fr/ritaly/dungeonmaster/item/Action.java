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
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Pit;
import fr.ritaly.dungeonmaster.projectile.ItemProjectile;

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
	 * The skill attached to this action and improved every time the action is
	 * used.
	 */
	private final Skill improvedSkill;

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
	 * The base probability of hitting an enemy when using this action.
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

	private Action(Skill improvedSkill, int experienceGain, int shieldModifier, int stamina, int hitProbability, int damage, int fatigue) {
		Validate.notNull(improvedSkill, "The given skill is null");

		this.improvedSkill = improvedSkill;
		this.experienceGain = experienceGain;
		this.shieldModifier = shieldModifier;
		this.stamina = stamina;
		this.hitProbability = hitProbability;
		this.damage = damage;
		this.fatigue = fatigue;
	}

	public Skill getImprovedSkill() {
		return improvedSkill;
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

		if (improvedSkill.isHidden()) {
			// The improved skill is hidden, what's the related skill improved ?
			final Skill relatedSkill = improvedSkill.getRelatedSkill();

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
		if (improvedSkill.isHidden() && attackedDuringLast25Ticks) {
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

		// What's the item that triggered this action ? It's in the champion's
		// weapon hand by definition
		final Item item = champion.getBody().getWeaponHand().getItem();

		if (item == null) {
			// Can't happen
			throw new IllegalStateException("The given champion " + champion.getName()
					+ " doesn't hold any item in its weapon hand");
		}

		if (Action.CLIMB_DOWN.equals(this)) {
			// The party must be facing a pit. What's the position facing the
			// party ?
			final Position target = dungeon.getParty().getFacingPosition();

			// Retrieve the element at this position
			final Element facingElement = dungeon.getElement(target);

			if (!(facingElement instanceof Pit)) {
				// The party can't go down into the pit
				return false;
			}

			final Pit pit = (Pit) facingElement;

			if (pit.isIllusion()) {
				// The pit is a fake, the action fails
				return false;
			}

			if (pit.isClosed()) {
				// The pit must be open
				return false;
			}

			// Set the state to CLIMBING_DOWN to distinguish from a plain fall
			champion.getParty().setState(Party.State.CLIMBING_DOWN);

			// Move forward to the pit. The pit will teleport the party to the
			// level below as the party's not falling. If there are several
			// "stacked" pits, this use case is managed by method
			// Dungeon.teleportParty()
			dungeon.teleportParty(target, dungeon.getParty().getLookDirection(), true);

			// Restore the initial state
			champion.getParty().setState(Party.State.NORMAL);

			// The champion gained some experience
			champion.gainExperience(improvedSkill, computeEarnedExperience(dungeon));
		} else if (Action.HEAL.equals(this)) {
			// TODO Should we heal the whole party or just the champion ?
			final int healPoints = Utils.random(10, 30);

			// Only living champions can be healed. Dead champions have to be
			// resurrected first
			for (final Champion aChampion : dungeon.getParty().getChampions(false)) {
				aChampion.getStats().getHealth().inc(healPoints);
			}

			// The champion gained some experience (this experience can be null)
			final int xp = computeEarnedExperience(dungeon);

			if (xp > 0) {
				// The method gainExperience() throws an exception if the xp is
				// zero
				champion.gainExperience(improvedSkill, xp);
			}
		} else if (Action.THROW.equals(this)) {
			// TODO Implement the throwing of objects without resorting to this action (Clicking in the UI throws an object and casts a projectile)
			// TODO How far can the projectile go before dying ?

			// Determine the direction towards which the item is thrown
			final Direction direction = champion.getParty().getDirection();

			final SubCell subCell;

			switch (direction) {
			case EAST:
				if (champion.getSubCell().isTowardsNorth()) {
					subCell = SubCell.NORTH_WEST;
				} else {
					subCell = SubCell.SOUTH_WEST;
				}
				break;
			case NORTH:
				if (champion.getSubCell().isTowardsEast()) {
					subCell = SubCell.SOUTH_EAST;
				} else {
					subCell = SubCell.SOUTH_WEST;
				}
				break;
			case SOUTH:
				if (champion.getSubCell().isTowardsEast()) {
					subCell = SubCell.NORTH_EAST;
				} else {
					subCell = SubCell.NORTH_WEST;
				}
				break;
			case WEST:
				if (champion.getSubCell().isTowardsNorth()) {
					subCell = SubCell.NORTH_EAST;
				} else {
					subCell = SubCell.SOUTH_EAST;
				}
				break;
				default:
					throw new RuntimeException();
			}

			// TODO Refactor this code (that's ugly)
			// The projectile is created on the neighbor position
			new ItemProjectile(item, dungeon, champion.getParty().getFacingPosition(), direction, subCell, 30);
		} else if (Action.FLUX_CAGE.equals(this)) {
			// Create a flux cage on the neighbor position in front of the party
			final Element element = dungeon.getElement(dungeon.getParty().getFacingPosition());

			// TODO Ensure the target element can contain a flux cage !
			element.createFluxCage();

			// The champion gained some experience
			champion.gainExperience(improvedSkill, computeEarnedExperience(dungeon));
		} else {
			// FIXME Identify the remaining actions to implement

			// TODO Compute if the action succeeds
			final boolean success = true;

			if (success) {
				// TODO Compute the damage points

				// The champion gained some experience
				champion.gainExperience(improvedSkill, computeEarnedExperience(dungeon));
			}

			// TODO Change the champion's defense (he's vulnerable for a short time)
			// TODO Play a sound
		}

		// The champion consumed some stamina
		champion.getStats().getStamina().dec(stamina);

		if (fatigue > 0) {
			// The weapon hand is unavailable for a short time depending on the
			// item's fatigue
			champion.getBody().getWeaponHand().disable(fatigue);
		} else {
			// TODO What about actions whose fatigue is zero (ex: THROW) ?
		}

		return true;
	}
}