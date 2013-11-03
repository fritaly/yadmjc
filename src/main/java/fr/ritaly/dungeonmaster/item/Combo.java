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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Level;

/**
 * Enumerates the possible combinations of item actions. Combinations are
 * associated to items.<br>
 * <br>
 * Source: <a href="http://dmweb.free.fr/?q=node/690">Technical Documentation -
 * Dungeon Master and Chaos Strikes Back Actions and Combos</a>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Combo {
	// TODO Rename those constants
	COMBO_0(),
	COMBO_1(
		new Entry(Action.INVOKE, false, 0),
		new Entry(Action.FUSE, false, 0),
		new Entry(Action.FLUX_CAGE, false, 0)),
	COMBO_2(
		new Entry(Action.PUNCH, false, 0),
		new Entry(Action.KICK, false, 0),
		new Entry(Action.WAR_CRY, false, 0)),
	COMBO_3(
		new Entry(Action.N, false, 0),
		new Entry(Action.N, false, 0),
		new Entry(Action.N, false, 0)),
	COMBO_4(
		new Entry(Action.N, false, 0),
		new Entry(Action.N, false, 0),
		new Entry(Action.N, false, 0)),
	COMBO_5(
		new Entry(Action.SWING, false, 0)),
	COMBO_6(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.FIREBALL, true, 7)),
	COMBO_7(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.LIGHTNING, true, 7)),
	COMBO_8(
		new Entry(Action.SLASH, false, 0),
		new Entry(Action.BRANDISH, false, 2),
		new Entry(Action.CONFUSE, true, 3)),
	COMBO_9(
		new Entry(Action.JAB, false, 0),
		new Entry(Action.CHOP, false, 0),
		new Entry(Action.LIGHTNING, true, 4)),
	COMBO_10(
		new Entry(Action.CHOP, false, 0),
		new Entry(Action.MELEE, false, 2),
		new Entry(Action.FIREBALL, true, 6)),
	COMBO_11(
		new Entry(Action.PARRY, false, 0),
		new Entry(Action.BRANDISH, false, 3),
		new Entry(Action.FIRE_SHIELD, false, 5)),
	COMBO_12(
		new Entry(Action.THROW, false, 0),
		new Entry(Action.STAB, false, 0),
		new Entry(Action.SLASH, false, 2)),
	COMBO_13(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.PARRY, false, 2),
		new Entry(Action.CHOP, false, 3)),
	COMBO_14(
		new Entry(Action.JAB, false, 0),
		new Entry(Action.PARRY, false, 1),
		new Entry(Action.THRUST, false, 5)),
	COMBO_15(
		new Entry(Action.SLASH, false, 0),
		new Entry(Action.PARRY, false, 1),
		new Entry(Action.MELEE, false, 5)),
	COMBO_16(
		new Entry(Action.CHOP, false, 0),
		new Entry(Action.MELEE, false, 5),
		new Entry(Action.THRUST, false, 6)),
	COMBO_17(
		new Entry(Action.STAB, false, 0),
		new Entry(Action.CHOP, false, 2),
		new Entry(Action.CLEAVE, false, 5)),
	COMBO_18(
		new Entry(Action.JAB, false, 0),
		new Entry(Action.CLEAVE, false, 2),
		new Entry(Action.DISRUPT, false, 4)),
	COMBO_19(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.THRUST, false, 5),
		new Entry(Action.BERZERK, false, 7)),
	COMBO_20(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.CHOP, false, 0),
		new Entry(Action.MELEE, false, 5)),
	COMBO_21(
		new Entry(Action.CHOP, false, 0),
		new Entry(Action.CLEAVE, false, 3),
		new Entry(Action.BERZERK, false, 8)),
	COMBO_22(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.BASH, false, 2),
		new Entry(Action.STUN, false, 4)),
	COMBO_23(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.STUN, false, 3),
		new Entry(Action.MELEE, false, 6)),
	COMBO_24(
		new Entry(Action.THROW, false, 0),
		new Entry(Action.BASH, false, 0)),
	COMBO_25(
		new Entry(Action.N, false, 0),
		new Entry(Action.N, false, 0),
		new Entry(Action.N, false, 0)),
	COMBO_26(
		new Entry(Action.THROW, false, 0),
		new Entry(Action.STAB, false, 0)),
	COMBO_27(
		new Entry(Action.SHOOT, false, 0)),
	COMBO_28(
		new Entry(Action.CALM, false, 0),
		new Entry(Action.SPELL_SHIELD, true, 2),
		new Entry(Action.HEAL, false, 3)),
	COMBO_29(
		new Entry(Action.CALM, false, 0),
		new Entry(Action.SPELL_SHIELD, true, 3),
		new Entry(Action.FIRE_SHIELD, true, 4)),
	COMBO_30(
		new Entry(Action.PARRY, false, 0),
		new Entry(Action.LIGHT, true, 0),
		new Entry(Action.DISPELL, true, 3)),
	COMBO_31(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.DISPELL, true, 3),
		new Entry(Action.FIRE_SHIELD, true, 4)),
	COMBO_32(
		new Entry(Action.HEAL, false, 0),
		new Entry(Action.CALM, false, 2),
		new Entry(Action.BRANDISH, false, 3)),
	COMBO_33(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.LIGHTNING, true, 2),
		new Entry(Action.WINDOW, true, 4)),
	COMBO_34(
		new Entry(Action.SWING, false, 0),
		new Entry(Action.PARRY, false, 0),
		new Entry(Action.SPIT, true, 3)),
	COMBO_35(
		new Entry(Action.PARRY, false, 0),
		new Entry(Action.HEAL, false, 3),
		new Entry(Action.LIGHT, true, 4)),
	COMBO_36(
		new Entry(Action.BLOW_HORN, false, 0)),
	COMBO_37(
		new Entry(Action.FLIP, false, 0)),
	COMBO_38(
		new Entry(Action.FREEZE_LIFE, false, 0)),
	COMBO_39(
		new Entry(Action.CLIMB_DOWN, false, 0)),
	COMBO_40(
		new Entry(Action.THROW, false, 0),
		new Entry(Action.STAB, false, 0)),
	COMBO_41(
		new Entry(Action.BLOCK, false, 0),
		new Entry(Action.HIT, false, 2)),
	COMBO_42(
		new Entry(Action.BLOCK, false, 0)),
	COMBO_43(
		new Entry(Action.PUNCH, false, 0),
		new Entry(Action.FREEZE_LIFE, true, 0));

	// TODO Document this class
	public static final class Entry {

		/**
		 * The item action this entry pertains to.
		 */
		private final Action action;

		/**
		 * Whether the use of this action is limited by a number of available
		 * charges.
		 */
		private final boolean useCharges;

		/**
		 * The minimum skill level necessary for using this action.
		 */
		private final int minimumSkillLevel;

		// FIXME Add a damage property ?
		private Entry(Action action, boolean useCharges, int minimumSkillLevel) {
			this.action = action;
			this.useCharges = useCharges;
			this.minimumSkillLevel = minimumSkillLevel;
		}

		public Action getAction() {
			return action;
		}

		public boolean useCharges() {
			return useCharges;
		}

		public Level getMinimumSkillLevel() {
			return Level.values()[minimumSkillLevel];
		}

		/**
		 * Tells whether the given champion can use this action.
		 *
		 * @param champion
		 *            the champion to test. Can't be null.
		 * @return whether the given champion can use this action.
		 */
		public boolean isUsable(Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			// What's the minimal level required to use this skill ?
			final Level level = getMinimumSkillLevel();

			if (Level.NONE.equals(level)) {
				// In most cases, there's no min skill level required
				return true;
			}

			// What's the skill involved ?
			final Skill skill = action.getImprovedSkill();

			// Is the champion skilled enough to use this action ?
			return (champion.getLevel(skill).compareTo(level) >= 0);
		}
	}

	// TODO Document this property
	private final List<Entry> entries;

	private Combo(Entry... entries) {
		this.entries = Arrays.asList(entries);
	}

	// Visibility package protected on purpose
	List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}
}