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
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Pit;

/**
 * Une {@link Action} qu'un champion peut réaliser à l'aide d'un {@link Item}.
 * Les {@link Action}s ne sont jamais utilisées individuellement mais regroupées
 * au sein de {@link Combo} assignées à un item.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
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

	private final Skill improvedSkill;

	private final int experienceGain;

	private final int shieldModifier;

	private final int stamina;

	private final int hitProbability;

	private final int damage;

	private final int fatigue;

	private Action(Skill improvedSkill, int experienceGain, int shieldModifier,
			int stamina, int hitProbability, int damage, int fatigue) {

		Validate.isTrue(improvedSkill != null, "The given skill is null");

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
		// Calculer l'expérience gagnée
		int xp = experienceGain;

		final int currentTick = Clock.getInstance().getTickId();
		final int lastAttackTick = dungeon.getParty().getLastAttackTick();

		// Indique si le groupe a été attaqué dans les 150 derniers tics par un
		// monstre
		final boolean attackedDuringLast150Ticks = (currentTick - lastAttackTick) <= 150;

		// Indique si le groupe a été attaqué dans les 25 derniers tics par un
		// monstre
		final boolean attackedDuringLast25Ticks = (currentTick - lastAttackTick) <= 25;

		if (improvedSkill.isHidden()) {
			// Compétence associée à cette compétence cachée ?
			final Skill relatedSkill = improvedSkill.getRelatedSkill();

			if (Skill.FIGHTER.equals(relatedSkill)
					|| Skill.NINJA.equals(relatedSkill)) {

				if (!attackedDuringLast150Ticks) {
					xp = xp / 2;
				}
			}
		}

		// Multiplicateur d'expérience du niveau courant ?
		final int experienceMultiplier = dungeon.getCurrentLevel()
				.getExperienceMultiplier();

		if (experienceMultiplier != 0) {
			xp = xp * experienceMultiplier;
		}

		if (improvedSkill.isHidden() && attackedDuringLast25Ticks) {
			xp = xp * 2;
		}

		return xp;
	}

	// FIXME Implémenter les actions
	public boolean perform(Dungeon dungeon, Champion champion) {
		// Le champion peut être à null selon les actions
		Validate.isTrue(dungeon != null, "The given dungeon is null");

		if (Action.CLIMB_DOWN.equals(this)) {
			// Le groupe doit être face à une oubliette
			final Element current = dungeon.getCurrentElement();

			// Direction de regard du groupe ?
			final Direction lookDirection = dungeon.getParty()
					.getLookDirection();

			// Position cible visible ?
			final Position target = current.getPosition()
					.towards(lookDirection);

			// Element cible visible ?
			final Element facingElement = dungeon.getElement(target);

			if (!(facingElement instanceof Pit)) {
				// Le groupe ne peut descendre dans l'oubliette
				return false;
			}

			final Pit pit = (Pit) facingElement;

			// L'oubliette ne doit pas être une illusion
			if (pit.isIllusion()) {
				return false;
			}

			// L'oubliette doit être ouverte
			if (pit.isClosed()) {
				return false;
			}

			// Le groupe peut descendre (pas de son à jouer). Attention de bien
			// faire avancer de une position vers l'avant le groupe !!! Le cas
			// de plusieurs oubliettes "empilées" est traité car la méthode
			// Dungeon.teleportParty()
			dungeon.teleportParty(target.towards(Direction.DOWN),
					lookDirection, true);

			// Le champion gagne de l'expérience
			champion.gainExperience(improvedSkill,
					computeEarnedExperience(dungeon));
		} else if (Action.HEAL.equals(this)) {
			// Guérir le groupe
			final int healPoints = Utils.random(10, 30);
			
			// On ne peut soigner que les héros vivants (les morts doivent être 
			// ressuscités avant via un autel)
			for (Champion aChampion : dungeon.getParty().getChampions(false)) {
				aChampion.getStats().getHealth().inc(healPoints);
			}

			// Le champion gagne de l'expérience
			champion.gainExperience(improvedSkill,
					computeEarnedExperience(dungeon));
		} else {
			// TODO Calculer si le coup a porté
			final boolean success = true;

			if (success) {
				// TODO Calculer les points de dommage infligés

				// Le champion gagne de l'expérience
				champion.gainExperience(improvedSkill,
						computeEarnedExperience(dungeon));
			}

			// TODO La défense du champion est modifiée

			// TODO Son
		}

		// Le champion consomme de la stamina
		champion.getStats().getStamina().dec(stamina);

		// Main indisponible en fonction de la fatigue associée à l'action
		champion.getBody().getWeaponHand().disable(fatigue);

		return true;
	}
}