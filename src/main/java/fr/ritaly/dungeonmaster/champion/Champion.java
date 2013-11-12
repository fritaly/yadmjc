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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Constants;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Location;
import fr.ritaly.dungeonmaster.Poison;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.Speed;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.body.Body;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.champion.inventory.BackPack;
import fr.ritaly.dungeonmaster.champion.inventory.Inventory;
import fr.ritaly.dungeonmaster.champion.inventory.Pouch;
import fr.ritaly.dungeonmaster.champion.inventory.Quiver;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Bones;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.magic.AlignmentRune;
import fr.ritaly.dungeonmaster.magic.ChampionMumblesNonsenseException;
import fr.ritaly.dungeonmaster.magic.ElementRune;
import fr.ritaly.dungeonmaster.magic.EmptyFlaskNeededException;
import fr.ritaly.dungeonmaster.magic.EmptyHandNeededException;
import fr.ritaly.dungeonmaster.magic.FormRune;
import fr.ritaly.dungeonmaster.magic.NotEnoughManaException;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Rune;
import fr.ritaly.dungeonmaster.magic.SkillTooLowException;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.magic.SpellCaster;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.projectile.ItemProjectile;
import fr.ritaly.dungeonmaster.stat.Stat;
import fr.ritaly.dungeonmaster.stat.Stats;

/**
 * A champion.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Champion implements ChangeEventSource, PropertyChangeListener, ClockListener {

	private final Log log = LogFactory.getLog(Champion.class);

	/**
	 * Enumerates the 24 Dungeon Master champions.<br>
	 * <br>
	 * Source: <a href="http://dmweb.free.fr/?q=node/199">Dungeon Master Encyclopaedia</a>
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Name {
		ALEX("Alex Ander"),
		AZIZI("Azizi Johari"),
		BORIS("Boris Wizard Of Baldor"),
		CHANI("Chani Sayyadina Sihaya"),
		DAROOU("Daroou"),
		ELIJA("Elija Lion Of Yaitopya"),
		GANDO("Gando Thurfoot"),
		GOTHMOG("Gothmog"),
		HALK("Halk The Barbarian"),
		HAWK("Hawk The Fearless"),
		HISSA("Hissssa Lizar Of Makan"),
		IAIDO("Iaido Ruyito Chiburi"),
		LEIF("Leif The Valiant"),
		LEYLA("Leyla Shadowseek"),
		LINFLAS("Linflas"),
		MOPHUS("Mophus The Healer"),
		NABI("Nabi The Prophet"),
		SONJA("Sonja She Devil"),
		STAMM("Stamm Bladecaster"),
		SYRA("Syra Child Of Nature"),
		TIGGY("Tiggy Tamal"),
		WUTSE("Wu Tse Son Of Heaven"),
		WUUF("Wuuf The Bika"),
		ZED("Zed Duke Of Banville");

		/**
		 * The champion's full name (that is, presentation name).
		 */
		private final String fullName;

		private Name(final String fullName) {
			Validate.isTrue(!StringUtils.isBlank(fullName), String.format("The given full name '%s' is blank", fullName));

			this.fullName = fullName;
		}

		/**
		 * Returns this champion's gender.
		 *
		 * @return the gender. Never returns null.
		 */
		public Gender getGender() {
			switch (this) {
			case IAIDO:
			case ZED:
			case HAWK:
			case BORIS:
			case ALEX:
			case NABI:
			case HISSA:
			case GOTHMOG:
			case MOPHUS:
			case STAMM:
			case LEIF:
			case DAROOU:
			case HALK:
			case GANDO:
			case LINFLAS:
			case ELIJA:
				return Gender.MALE;

			case CHANI:
			case SONJA:
			case LEYLA:
			case WUUF:
			case AZIZI:
			case TIGGY:
			case WUTSE:
			case SYRA:
				return Gender.FEMALE;

			default:
				throw new UnsupportedOperationException("Unsupported method for " + this);
			}
		}

		/**
		 * Returns the champion's full name (presentation name).
		 *
		 * @return a string representing the champion's full name.
		 */
		public String getFullName() {
			return fullName;
		}

		/**
		 * Returns the champion's skills and their associated levels.
		 *
		 * @return a map containing the level by skill for this champion. Never
		 *         returns null.
		 */
		public Map<Skill, Level> getSkills() {
			switch (this) {
			case IAIDO:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.APPRENTICE, 2, 3, 0, 2)
						.setSkills(Skill.PRIEST, Level.NOVICE, 1, 1, 1, 2)
						.getSkills();
			case ZED:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.NOVICE, 2, 1, 1, 2)
						.setSkills(Skill.NINJA, Level.NOVICE, 2, 1, 2, 1)
						.setSkills(Skill.PRIEST, Level.NOVICE, 1, 2, 1, 1)
						.setSkills(Skill.WIZARD, Level.NOVICE, 1, 2, 1, 1)
						.getSkills();
			case CHANI:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.NOVICE, 1, 3, 0, 2)
						.setSkills(Skill.NINJA, Level.NONE, 0, 0, 1, 0)
						.setSkills(Skill.WIZARD, Level.APPRENTICE, 3, 2, 3, 1)
						.getSkills();
			case HAWK:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.NOVICE, 2, 0, 0, 2)
						.setSkills(Skill.PRIEST, Level.APPRENTICE, 0, 3, 0, 3)
						.getSkills();
			case BORIS:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.NOVICE, 3, 2, 1, 0)
						.setSkills(Skill.WIZARD, Level.APPRENTICE, 2, 3, 3, 3)
						.getSkills();
			case ALEX:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.APPRENTICE, 3, 2, 3, 2)
						.setSkills(Skill.WIZARD, Level.NOVICE, 2, 2, 1, 2)
						.getSkills();
			case NABI:
				return new SkillMapBuilder()
						.setSkills(Skill.PRIEST, Level.APPRENTICE, 1, 1, 4, 2)
						.setSkills(Skill.WIZARD, Level.NOVICE, 1, 1, 1, 1)
						.getSkills();
			case HISSA:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.APPRENTICE, 4, 3, 0, 0)
						.setSkills(Skill.NINJA, Level.NOVICE, 0, 3, 1, 0)
						.getSkills();
			case GOTHMOG:
				return new SkillMapBuilder().setSkills(Skill.WIZARD, Level.JOURNEYMAN, 4, 3, 2, 2).getSkills();
			case SONJA:
				return new SkillMapBuilder().setSkills(Skill.FIGHTER, Level.JOURNEYMAN, 3, 4, 2, 3).getSkills();
			case LEYLA:
				return new SkillMapBuilder().setSkills(Skill.NINJA, Level.JOURNEYMAN, 3, 3, 3, 4).getSkills();
			case MOPHUS:
				return new SkillMapBuilder().setSkills(Skill.PRIEST, Level.JOURNEYMAN, 2, 4, 3, 2).getSkills();
			case WUUF:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.APPRENTICE, 1, 2, 3, 4)
						.setSkills(Skill.PRIEST, Level.NOVICE, 0, 3, 2, 1)
						.getSkills();
			case STAMM:
				return new SkillMapBuilder().setSkills(Skill.FIGHTER, Level.JOURNEYMAN, 3, 4, 2, 2).getSkills();
			case AZIZI:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.NOVICE, 2, 1, 3, 0)
						.setSkills(Skill.NINJA, Level.APPRENTICE, 2, 2, 3, 3)
						.getSkills();
			case LEIF:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.APPRENTICE, 3, 2, 2, 0)
						.setSkills(Skill.PRIEST, Level.NOVICE, 0, 2, 1, 1)
						.getSkills();
			case TIGGY:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.NOVICE, 1, 3, 1, 1)
						.setSkills(Skill.PRIEST, Level.NONE, 1, 0, 0, 0)
						.setSkills(Skill.WIZARD, Level.APPRENTICE, 2, 3, 3, 2)
						.getSkills();
			case WUTSE:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.NOVICE, 1, 2, 0, 3)
						.setSkills(Skill.PRIEST, Level.APPRENTICE, 2, 1, 4, 3)
						.getSkills();
			case DAROOU:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.APPRENTICE, 3, 0, 3, 0)
						.setSkills(Skill.WIZARD, Level.NEOPHYTE, 0, 0, 1, 1)
						.getSkills();
			case HALK:
				return new SkillMapBuilder().setSkills(Skill.FIGHTER, Level.JOURNEYMAN, 4, 0, 4, 0).getSkills();
			case SYRA:
				return new SkillMapBuilder()
						.setSkills(Skill.PRIEST, Level.NOVICE, 0, 3, 1, 1)
						.setSkills(Skill.WIZARD, Level.APPRENTICE, 0, 2, 3, 3)
						.getSkills();
			case GANDO:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.APPRENTICE, 3, 0, 2, 3)
						.setSkills(Skill.WIZARD, Level.NOVICE, 1, 2, 1, 2)
						.getSkills();
			case LINFLAS:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.APPRENTICE, 0, 1, 2, 4)
						.setSkills(Skill.NINJA, Level.NONE, 0, 0, 1, 0)
						.setSkills(Skill.PRIEST, Level.NONE, 0, 1, 0, 0)
						.setSkills(Skill.WIZARD, Level.NOVICE, 0, 1, 2, 2)
						.getSkills();
			case ELIJA:
				return new SkillMapBuilder()
						.setSkills(Skill.FIGHTER, Level.NOVICE, 1, 1, 2, 0)
						.setSkills(Skill.PRIEST, Level.APPRENTICE, 2, 1, 4, 2)
						.getSkills();
			default:
				throw new UnsupportedOperationException("Method unsupported for champion " + this);
			}
		}

		/**
		 * Generates some items for the given champion and adds them to the
		 * champion's inventory.
		 *
		 * @param champion
		 *            the champion that will be given the generated items (if
		 *            any). Can't be null.
		 */
		public void populateItems(final Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			final ItemFactory factory = ItemFactory.getFactory();

			final BodyPart torso = champion.getBody().getTorso();
			final BodyPart legs = champion.getBody().getLegs();
			final BodyPart feet = champion.getBody().getFeet();
			final BodyPart weaponHand = champion.getBody().getWeaponHand();
			final BodyPart shieldHand = champion.getBody().getShieldHand();
			final BodyPart neck = champion.getBody().getNeck();
			final BodyPart head = champion.getBody().getHead();

			final BackPack backPack = champion.getInventory().getBackPack();
			final Quiver quiver = champion.getInventory().getQuiver();
			final Pouch pouch = champion.getInventory().getPouch();

			switch (this) {
			case IAIDO:
				torso.putOn(factory.newItem(Item.Type.GHI));
				legs.putOn(factory.newItem(Item.Type.GHI_TROUSERS));
				weaponHand.putOn(factory.newItem(Item.Type.SAMURAI_SWORD));
				break;
			case ZED:
				torso.putOn(factory.newItem(Item.Type.MAIL_AKETON));
				legs.putOn(factory.newItem(Item.Type.BLUE_PANTS));
				feet.putOn(factory.newItem(Item.Type.HOSEN));
				backPack.add(factory.newItem(Item.Type.TORCH));
				break;
			case CHANI:
				torso.putOn(factory.newItem(Item.Type.SILK_SHIRT));
				legs.putOn(factory.newItem(Item.Type.GUNNA));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				neck.putOn(factory.newItem(Item.Type.MOONSTONE));
				break;
			case HAWK:
				torso.putOn(factory.newItem(Item.Type.LEATHER_JERKIN));
				legs.putOn(factory.newItem(Item.Type.LEATHER_PANTS));
				feet.putOn(factory.newItem(Item.Type.SUEDE_BOOTS));
				weaponHand.putOn(factory.newItem(Item.Type.ARROW));
				quiver.add(factory.newItem(Item.Type.ARROW));
				break;
			case BORIS:
				torso.putOn(factory.newItem(Item.Type.TUNIC));
				legs.putOn(factory.newItem(Item.Type.LEATHER_PANTS));
				feet.putOn(factory.newItem(Item.Type.LEATHER_BOOTS));
				pouch.add(factory.newItem(Item.Type.RABBIT_FOOT));
				break;
			case ALEX:
				torso.putOn(factory.newItem(Item.Type.LEATHER_JERKIN));
				legs.putOn(factory.newItem(Item.Type.LEATHER_PANTS));
				feet.putOn(factory.newItem(Item.Type.SUEDE_BOOTS));
				weaponHand.putOn(factory.newItem(Item.Type.SLING));
				break;
			case NABI:
				torso.putOn(factory.newItem(Item.Type.TUNIC));
				legs.putOn(factory.newItem(Item.Type.BLUE_PANTS));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				weaponHand.putOn(factory.newItem(Item.Type.STAFF));
				break;
			case HISSA:
				break;
			case GOTHMOG:
				neck.putOn(factory.newItem(Item.Type.CLOAK_OF_NIGHT));
				break;
			case SONJA:
				torso.putOn(factory.newItem(Item.Type.HALTER));
				legs.putOn(factory.newItem(Item.Type.GUNNA));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				neck.putOn(factory.newItem(Item.Type.CHOKER));
				weaponHand.putOn(factory.newItem(Item.Type.SWORD));
				break;
			case LEYLA:
				torso.putOn(factory.newItem(Item.Type.SILK_SHIRT));
				legs.putOn(factory.newItem(Item.Type.LEATHER_PANTS));
				feet.putOn(factory.newItem(Item.Type.LEATHER_BOOTS));
				weaponHand.putOn(factory.newItem(Item.Type.ROPE));
				break;
			case MOPHUS:
				torso.putOn(factory.newItem(Item.Type.ROBE_BODY));
				legs.putOn(factory.newItem(Item.Type.ROBE_LEGS));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				backPack.add(factory.newItem(Item.Type.BREAD));
				backPack.add(factory.newItem(Item.Type.CHEESE));
				backPack.add(factory.newItem(Item.Type.APPLE));
				break;
			case WUUF:
				torso.putOn(factory.newItem(Item.Type.LEATHER_JERKIN));
				weaponHand.putOn(factory.newItem(Item.Type.EMPTY_FLASK));
				break;
			case STAMM:
				torso.putOn(factory.newItem(Item.Type.TUNIC));
				legs.putOn(factory.newItem(Item.Type.LEATHER_PANTS));
				feet.putOn(factory.newItem(Item.Type.SUEDE_BOOTS));
				weaponHand.putOn(factory.newItem(Item.Type.AXE));
				break;
			case AZIZI:
				torso.putOn(factory.newItem(Item.Type.HALTER));
				legs.putOn(factory.newItem(Item.Type.BARBARIAN_HIDE));
				shieldHand.putOn(factory.newItem(Item.Type.HIDE_SHIELD));
				weaponHand.putOn(factory.newItem(Item.Type.DAGGER));
				quiver.add(factory.newItem(Item.Type.DAGGER));
				break;
			case LEIF:
				torso.putOn(factory.newItem(Item.Type.LEATHER_JERKIN));
				legs.putOn(factory.newItem(Item.Type.LEATHER_PANTS));
				feet.putOn(factory.newItem(Item.Type.LEATHER_BOOTS));
				break;
			case TIGGY:
				torso.putOn(factory.newItem(Item.Type.KIRTLE));
				legs.putOn(factory.newItem(Item.Type.GUNNA));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				weaponHand.putOn(factory.newItem(Item.Type.WAND));
				break;
			case WUTSE:
				torso.putOn(factory.newItem(Item.Type.SILK_SHIRT));
				legs.putOn(factory.newItem(Item.Type.TABARD));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				weaponHand.putOn(factory.newItem(Item.Type.THROWING_STAR));
				quiver.add(factory.newItem(Item.Type.THROWING_STAR));
				quiver.add(factory.newItem(Item.Type.THROWING_STAR));
				break;
			case DAROOU:
				break;
			case HALK:
				head.putOn(factory.newItem(Item.Type.BEZERKER_HELM));
				legs.putOn(factory.newItem(Item.Type.BARBARIAN_HIDE));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				weaponHand.putOn(factory.newItem(Item.Type.CLUB));
				break;
			case SYRA:
				torso.putOn(factory.newItem(Item.Type.ELVEN_DOUBLET));
				legs.putOn(factory.newItem(Item.Type.TABARD));
				backPack.add(factory.newItem(Item.Type.APPLE));
				break;
			case GANDO:
				torso.putOn(factory.newItem(Item.Type.LEATHER_JERKIN));
				legs.putOn(factory.newItem(Item.Type.BLUE_PANTS));
				feet.putOn(factory.newItem(Item.Type.LEATHER_BOOTS));
				weaponHand.putOn(factory.newItem(Item.Type.POISON_DART));
				quiver.add(factory.newItem(Item.Type.POISON_DART));
				break;
			case LINFLAS:
				torso.putOn(factory.newItem(Item.Type.ELVEN_DOUBLET));
				legs.putOn(factory.newItem(Item.Type.ELVEN_HUKE));
				feet.putOn(factory.newItem(Item.Type.ELVEN_BOOTS));
				weaponHand.putOn(factory.newItem(Item.Type.BOW));
				break;
			case ELIJA:
				torso.putOn(factory.newItem(Item.Type.ROBE_BODY));
				legs.putOn(factory.newItem(Item.Type.ROBE_LEGS));
				feet.putOn(factory.newItem(Item.Type.SANDALS));
				weaponHand.putOn(factory.newItem(Item.Type.MAGICAL_BOX_BLUE));
				break;
			default:
				throw new UnsupportedOperationException("Method unsupported for champion " + this);
			}
		}
	}

	/**
	 * Enumerates the 4 colors used for visually distinguishing the champions
	 * inside the party.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Color {
		RED,
		YELLOW,
		GREEN,
		BLUE;
	}

	/**
	 * Enumerates the possible levels of experience for a given skill. Each
	 * level is associated to a range of experience points. When the champion's
	 * experience points are inside this range, the champion has the associated
	 * level of experience. When experience points increase and are out of
	 * range, the level is promoted to the new level. There are 16 distinct
	 * levels.<br>
	 * <br>
	 * Sources: <a href="http://dmweb.free.fr/?q=node/692">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Experience and
	 * Training</a>, <a href="http://dmweb.free.fr/?q=node/691">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Skills and
	 * Statistics</a>
	 */
	public static enum Level {
		// Those levels are sorted from lowest to highest. Don't change the ordering !
		NONE,
		NEOPHYTE,
		NOVICE,
		APPRENTICE,
		JOURNEYMAN,
		CRAFTSMAN,
		ARTISAN,
		ADEPT,
		EXPERT,
		LO_MASTER,
		UM_MASTER,
		ON_MASTER,
		EE_MASTER,
		PAL_MASTER,
		MON_MASTER,
		ARCH_MASTER;

		/**
		 * The lower bound of the range of points associated to this experience level.
		 */
		private final int lowerBound;

		/**
		 * The upper bound of the range of points associated to this experience level.
		 */
		private final int upperBound;

		private Level() {
			// The span of the range of points doubles with every level
			this.lowerBound = 500 << (ordinal() - 1);
			this.upperBound = (500 << ordinal());
		}

		public int getLowerBound() {
			return lowerBound;
		}

		public int getUpperBound() {
			return upperBound;
		}

		/**
		 * Tells whether the given experience points are within the range of
		 * points associated to this level.
		 *
		 * @param experience
		 *            an integer representing a number of experience points.
		 * @return whether the given experience points corresponds to this
		 *         level.
		 */
		public boolean contains(int experience) {
			return (lowerBound <= experience) && (experience <= upperBound);
		}

		/**
		 * Returns the level associated to the given experience points. Throws
		 * an {@link IllegalArgumentException} if the level can't be found.
		 *
		 * @param points
		 *            an integer representing a number of experience points.
		 * @return the associated level. Never returns null.
		 */
		public static Level fromExperience(int points) {
			for (Level level : values()) {
				if (level.contains(points)) {
					return level;
				}
			}

			throw new IllegalArgumentException(String.format("Unable to find the level for experience points %d", points));
		}
	}

	/**
	 * The champion's name.
	 */
	private final String name;

	/**
	 * The champion's gender.
	 */
	private final Gender gender;

	/**
	 * The party this champion is attached to (if any).
	 */
	private Party party;

	/**
	 * The color used to represent the champion when member of a party. Can be
	 * null if not in a party.
	 */
	private Color color;

	/**
	 * The champion's body.
	 */
	private final Body body;

	/**
	 * The champion's inventory.
	 */
	private final Inventory inventory;

	/**
	 * Stores the identifier of the last clock tick when this champion was
	 * attacked or -1 if the champion has never been attacked.
	 */
	private int lastAttackTick = -1;

	/**
	 * The spell caster managing the spells cast by this champion.
	 */
	private final SpellCaster spellCaster = new SpellCaster(this);

	/**
	 * The champion's skills stored as a map. The key is the skill and the
	 * associated value represents the level in this skill.
	 */
	private final Map<Skill, Experience> skills;

	/**
	 * Support class to fire change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The champion's stats (health, mana, water, etc).
	 */
	private final Stats stats;

	/**
	 * The object managing the effect of poison when the champion is poisoned.
	 */
	private final Poison poison = new Poison();

	/**
	 * The object managing the effects of spells cast by this champion.
	 */
	private final ChampionSpells spells;

	/**
	 * The temporizer used to update the champion's spells, stats, etc.
	 */
	private final Temporizer temporizer;

	// TODO Enforce the below rule with a development time aspect
	// This constructor is only meant to be invoked from the ChampionFactory
	Champion(String name, Gender gender) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));
		Validate.notNull(gender, "The given gender is null");

		this.name = name;
		this.gender = gender;

		// The following objects must be created after setting the champion's
		// name
		this.inventory = new Inventory(this);

		this.stats = new Stats(this);
		this.stats.addPropertyChangeListener(this);

		this.body = new Body(this);

		this.spells = new ChampionSpells(this);

		// Trigger every 5 clock ticks
		this.temporizer = new Temporizer(name, 5);

		// Initialize the champion's skills to NONE
		final Map<Skill, Experience> map = new HashMap<Skill, Experience>();

		for (final Skill skill : Skill.values()) {
			map.put(skill, new Experience(this, skill, Level.NONE));
		}

		// Freeze the map of skills
		this.skills = Collections.unmodifiableMap(map);
	}

	/**
	 * Increases the experience associated to the given skill by the given
	 * amount of points. This method will automatically promote the champion to
	 * the next level if the experience is sufficient.
	 *
	 * @param skill
	 *            the skill whose experience is to be increased. Can't be null.
	 * @param points
	 *            a positive integer representing the number of experience
	 *            points to be added to the skill's associated experience.
	 */
	public void gainExperience(Skill skill, int points) {
		Validate.notNull(skill, "The given skill is null");
		Validate.isTrue(points > 0, String.format("The given points %d must be positive", points));
		assertAlive();

		if (skill.isBasic()) {
			// Improve the basic skill
			getExperience(skill).gain(points);
		} else {
			// Improve the hidden skill first
			getExperience(skill).gain(points);

			// ... then the associated basic skill
			getExperience(skill.getRelatedSkill()).gain(points);
		}
	}

	/**
	 * Returns the champion's skills as a list.
	 *
	 * @return a list containing the champion's skills. Never returns null.
	 */
	public List<Skill> getSkills() {
		// Defensive recopy
		return new ArrayList<Skill>(skills.keySet());
	}

	/**
	 * Defines the champion's level for the given skill.
	 *
	 * @param skill
	 *            the skill whose level is to be set. Can't be null.
	 * @param level
	 *            the level to set. Can't be null.
	 */
	public void setSkill(Skill skill, Level level) {
		Validate.notNull(skill, "The given skill is null");
		Validate.notNull(level, "The given level is null");

		// This map contains an entry for every possible skills and can't return null
		skills.get(skill).setLevel(level);
	}

	/**
	 * Returns the champion's experience for the given skill.
	 *
	 * @param skill
	 *            the skill whose associated experience is requested. Can't be
	 *            null.
	 * @return the experience for the given skill. Never returns null.
	 */
	public Experience getExperience(Skill skill) {
		Validate.notNull(skill, "The given skill is null");

		// Can't return null
		return skills.get(skill);
	}

	private void assertAlive() {
		if (isDead()) {
			throw new IllegalStateException("The champion is dead");
		}
	}

	/**
	 * Have this champion cast the given rune. This method will handle the
	 * consumption of mana, validate the rune to ensure its type is consistent
	 * with the runes already cast.
	 *
	 * @param rune
	 *            the rune to cast by the champion. Can't be null.
	 * @throws NotEnoughManaException
	 *             if the champion doesn't have enough mana to cast the given
	 *             rune.
	 */
	public void cast(Rune rune) throws NotEnoughManaException {
		Validate.notNull(rune, "The given rune is null");
		assertAlive();

		// Ensure the champion has enough mana to cast the rune
		final Stat mana = getStats().getMana();

		final int cost;

		// How costly is this rune in mana points ?
		if (Rune.Type.POWER.equals(rune.getType())) {
			cost = rune.getCost();
		} else {
			// Retrieve the power rune previously cast
			final PowerRune powerRune = spellCaster.getPowerRune();

			if (powerRune == null) {
				// Shouldn't happen
				throw new IllegalStateException("There is no previously cast power rune");
			}

			cost = rune.getCost(powerRune);
		}

		if (mana.value() < cost) {
			// Not enough mana to cast the rune
			throw new NotEnoughManaException();
		}

		// The champion has enough mana, cast the rune. The spell caster will
		// validate the sequence of runes (Power > Element > Form > Alignment)
		spellCaster.cast(rune);

		// Consume the mana
		mana.dec(cost);

		// At this point, the champion doesn't gain any experience. This happens
		// only when the champion casts the complete spell and the casting succeds
	}

	/**
	 * Have the champion cast the given spell type with the given power rune.
	 *
	 * @param powerRune
	 *            a power rune representing the power of the spell to cast.
	 *            Can't be null.
	 * @param spellType
	 *            the type of spell to cast. Can't be null.
	 * @throws NotEnoughManaException
	 *             if the champion doesn't have enough mana to cast this spell.
	 * @throws EmptyFlaskNeededException
	 *             if the spell required an empty flask and none can be found in
	 *             the champion's hands.
	 * @throws SkillTooLowException
	 *             if the champion isn't skilled enough to cast the spell.
	 */
	public void cast(PowerRune powerRune, Spell.Type spellType) throws NotEnoughManaException, EmptyFlaskNeededException,
			SkillTooLowException {

		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(spellType, "The given spell type is null");

		// Cast first the power rune
		cast(powerRune);

		// ... then the runes composing this spell
		for (final Rune rune : spellType.getRunes()) {
			cast(rune);
		}
	}

	public Spell cast(PowerRune powerRune, ElementRune elementRune) throws NotEnoughManaException,
			ChampionMumblesNonsenseException, EmptyFlaskNeededException, SkillTooLowException, EmptyHandNeededException {

		return castConcrete(powerRune, elementRune, null, null, 2);
	}

	public Spell cast(PowerRune powerRune, ElementRune elementRune, FormRune formRune) throws NotEnoughManaException,
			ChampionMumblesNonsenseException, EmptyFlaskNeededException, SkillTooLowException, EmptyHandNeededException {

		return castConcrete(powerRune, elementRune, formRune, null, 3);
	}

	public Spell cast(PowerRune powerRune, ElementRune elementRune, FormRune formRune, AlignmentRune alignmentRune)
			throws NotEnoughManaException, ChampionMumblesNonsenseException, EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		return castConcrete(powerRune, elementRune, formRune, alignmentRune, 4);
	}

	private Spell castConcrete(PowerRune powerRune, ElementRune elementRune, FormRune formRune, AlignmentRune alignmentRune,
			int count) throws NotEnoughManaException, ChampionMumblesNonsenseException, EmptyFlaskNeededException,
			SkillTooLowException, EmptyHandNeededException {

		// The power and element runes can't be null
		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(elementRune, "The given element rune is null");
		Validate.isTrue(count >= 2 && count <= 4, String.format("The given count (%d) must be within [2,4]", count));

		if (count == 2) {
			Validate.isTrue(formRune == null, "The given form rune must be null");
			Validate.isTrue(alignmentRune == null, "The given alignment rune must be null");
		} else if (count == 3) {
			Validate.notNull(formRune, "The given form rune is null");
			Validate.isTrue(alignmentRune == null, "The given alignment rune must be null");
		} else if (count == 4) {
			Validate.notNull(formRune, "The given form rune is null");
			Validate.notNull(alignmentRune, "The given alignment rune is null");
		}

		if (spellCaster.getRuneCount() > 0) {
			// There can't be any previously cast rune
			throw new IllegalStateException(String.format("There are %d rune(s) already invoked (expected: 0)",
					spellCaster.getRuneCount()));
		}

		// Cast the runes
		cast(powerRune);
		cast(elementRune);

		if (formRune != null) {
			cast(formRune);

			if (alignmentRune != null) {
				cast(alignmentRune);
			}
		}

		return castSpell();
	}

	/**
	 * Finalizes the spell currently being cast by the champion and returns the
	 * cast spell if the casting succeeds. This method will validate that the
	 * invoked runes form a valid spell. If successful, the champion's
	 * experience for the relevant skill will be automatically improved. Also
	 * the champion's weapon hand will become unavailable for a duration that
	 * depends on the spell cast.
	 *
	 * @return the cast spell if the operation succeeds. Never returns null.
	 * @throws ChampionMumblesNonsenseException
	 *             if the spell fails because the spell isn't valid.
	 * @throws EmptyFlaskNeededException
	 *             is the spell requires an empty flask and none can be found in
	 *             the champion's hands.
	 * @throws SkillTooLowException
	 *             if the champion isn't skilled enough to cast the spell.
	 * @throws EmptyHandNeededException
	 *             if the spell requires an empty hand and none is free.
	 */
	public Spell castSpell() throws ChampionMumblesNonsenseException, EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		assertAlive();

		// First create the spell without clearing the runes (special use case
		// when creating potions, see below)
		final Spell spell = spellCaster.cast(true);

		// Is the spell valid ?
		if (!spell.isValid()) {
			// No, clear the runes and throw an error
			spellCaster.clear();

			throw new ChampionMumblesNonsenseException();
		}

		// Is the champion skilled enough to cast this spell ?
		if (!spell.canBeCastBy(this)) {
			// No, clear the runes
			spellCaster.clear();

			throw new SkillTooLowException(String.format(
					"The champion's skill %s is too low to cast spell %s (actual: %s, minimum: %s)", spell.getSkill(),
					spell.getName(), getLevel(spell.getSkill()), spell.getType().getRequiredLevel()));
		}

		// Prerequisites for the spell to succeed ?
		if (spell.getType().requiresEmptyFlask()) {
			// The champion must hold an empty flask

			// Is an empty flask available ?
			final Item item1 = getBody().getWeaponHand().getItem();
			final Item item2 = getBody().getShieldHand().getItem();

			final boolean emptyFlask1 = (item1 != null) && item1.getType().equals(Item.Type.EMPTY_FLASK);
			final boolean emptyFlask2 = (item2 != null) && item2.getType().equals(Item.Type.EMPTY_FLASK);

			if (!emptyFlask1 && !emptyFlask2) {
				// No empty flask, throw an error WITHOUT clearing the runes
				throw new EmptyFlaskNeededException();
			}
		}

		// At this stage, the runes will always be cleared
		spellCaster.clear();

		// Skill involved for this spell (can be null) ?
		final Skill skill = spell.getSkill();

		if (skill != null) {
			// The spell succeeded so the champion gains some experience
			gainExperience(skill, spell.getEarnedExperience());
		}

		// How long will the weapon hand be unavailable ? Can return zero
		final int duration = spell.getDuration();

		if (duration > 0) {
			// The weapon hand becomes unavailable for a given duration
			body.getWeaponHand().disable(duration);
		}

		// Let the spell operate on the champion
		spell.actUpon(this);

		return spell;
	}

	public String getName() {
		return name;
	}

	public Gender getGender() {
		return gender;
	}

	/**
	 * Returns the party this champion belongs to (if any).
	 *
	 * @return a party or null if the champion isn't in a party.
	 */
	public Party getParty() {
		return party;
	}

	// TODO Enforce the rule below with a development time aspect
	// This method should only be called by the class Party
	void setParty(Party party) {
		this.party = party;

		if (party == null) {
			if (log.isDebugEnabled()) {
				log.debug(getName() + " left the party");
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug(getName() + " joined the party");
			}

			// Register this champion
			Clock.getInstance().register(this);
		}
	}

	public Color getColor() {
		return color;
	}

	void setColor(Color color) {
		// TODO Enforce the rule below with a development time aspect
		// The color argument can be null (when the champion leaves the party).
		// This method should only be called from the class Party
		this.color = color;
	}

	public Body getBody() {
		return body;
	}

	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Tells whether the champion is currently the leader of his / her group.
	 *
	 * @return whether the champion is currently the leader of his / her group.
	 */
	public boolean isLeader() {
		return (party != null) && (party.getLeader() == this);
	}

	/**
	 * Returns the maximum load the champion can carry.
	 *
	 * @return a float representing a number of Kg.
	 */
	public final float getMaxLoad() {
		return getStats().getActualMaxLoad();
	}

	/**
	 * Returns the champion's actual level for the given skill. Remainder: the
	 * level of a skill can be temporarily boosted. This method takes into
	 * account the possible boost.
	 *
	 * @param skill
	 *            the skill whose actual level is requested. Can't be null.
	 * @return a level. Never returns null.
	 */
	public Level getLevel(Skill skill) {
		Validate.notNull(skill, "The given skill is null");

		return skills.get(skill).getActualLevel();
	}

	/**
	 * Returns the load currently carried by the champion as a float. This
	 * method takes into account:
	 * <ul>
	 * <li>the items inside the champion's inventory.</li>
	 * <li>the items worn by the champion (armor, weapons, clothes, etc).</li>
	 * <li>if the champion is the leader of his / her group and the champion is
	 * holding an item</li>
	 * </ul>
	 *
	 * @return a float representing a number of Kg.
	 */
	public float getLoad() {
		float weight = inventory.getTotalWeight() + body.getTotalWeight();

		if ((party != null) && isLeader() && party.hasItem()) {
			// Take into account the leader's held item
			weight += getParty().getItem().getWeight();
		}

		return weight;
	}

	/**
	 * Returns the champion's move speed. The method takes into account:
	 * <ul>
	 * <li>if the champion is dead</li>
	 * <li>if the champion is overloaded</li>
	 * <li>if the champion is wounded at the feet or legs</li>
	 * <li>if the champion is wearing the "Boots of Speed"</li>
	 * </ul>
	 *
	 * @return the move speed for this champion. Never returns null.
	 */
	public Speed getMoveSpeed() {
		if (isDead()) {
			// For a dead champion, the speed is "undefined" (same as infinite)
			return Speed.UNDEFINED;
		}
		if (getLoad() >= getMaxLoad()) {
			// The overload slows down the champion
			return Speed.SLOW;
		}
		if (getBody().getFeet().isWounded() || getBody().getLegs().isWounded()) {
			// The champion is wounded at the feet or legs. A wound somewhere
			// else doesn't slow down the champion
			return Speed.SLOW;
		}
		if (getBody().getFeet().hasItem(Item.Type.BOOTS_OF_SPEED)) {
			// Those special boots boost the speed
			return Speed.FAST;
		}

		// FIXME Need a better handling of possible combinations (Boots of Speed + wounded ?)
		return Speed.NORMAL;
	}

	/**
	 * Make the champion die (if alive) and returns whether the operation succeeded.
	 *
	 * @return whether the champion was alive and just died.
	 */
	public boolean die() {
		if (isAlive()) {
			getStats().getHealth().baseValue(0);

			return true;
		}

		return false;
	}

	/**
	 * Tells whether the champion is alive.
	 *
	 * @return whether the champion is alive.
	 */
	public boolean isAlive() {
		return (getStats().getHealth().value() > 0);
	}

	/**
	 * Tells whether the champion is dead.
	 *
	 * @return whether the champion is dead.
	 */
	public final boolean isDead() {
		return !isAlive();
	}

	/**
	 * Returns the overall anti-magic bonus for this champion. The returned
	 * value depends on: the worn items (armor) and the active spells.
	 *
	 * @return an integer (positive or zero) representing the anti-magic bonus.
	 */
	public int getAntiMagic() {
		// Current anti-magic ? Includes the contribution from the worn items
		int antiMagic = getStats().getAntiMagic().value();

		if (party != null) {
			// ... and the party spells ?
			antiMagic += party.getSpells().getAntiMagic().value();
		}

		return antiMagic;
	}

	/**
	 * Returns the overall shield bonus for this champion. The returned
	 * value depends on: the worn items (armor) and the active spells.
	 *
	 * @return an integer (positive or zero) representing the shield bonus.
	 */
	public int getShield() {
		// Current shield ? Includes the contribution from the worn items
		int shield = getStats().getShield().value();

		// Take into account the champion's shield spell
		// shield += spells.getShield().actualValue();

		if (party != null) {
			// ... and the party spells ?
			shield += party.getSpells().getShield().value();
		}

		return shield;
	}

	/**
	 * Have the champion consume (that is, eat or drink) the given item and
	 * returns the new item (if any) resulting from the consumption. In most
	 * cases, consuming food destroys the item so the method returns null.
	 * However there are situations where consuming an item turns it into a new
	 * item (water flask -> empty flask). If the item isn't consumable, returns
	 * int input item.
	 *
	 * @param item
	 *            the item to consume. Can't be null.
	 * @return the input item if the operation failed or null if the item was
	 *         consumed and destroy in the process or another item representing
	 *         the item after consumption.
	 */
	public Item consume(Item item) {
		Validate.notNull(item, "The given item is null");

		return item.itemConsumed(this);
	}

	/**
	 * Restores the champion's health with the given "strength".
	 *
	 * @param powerRune
	 *            a power rune representing the strength of the healing. Can't
	 *            be null.
	 */
	public void heal(PowerRune powerRune) {
		Validate.notNull(powerRune, "The given power rune is null");
		assertAlive();

		// TODO Refine the formula for the number of health points restored
		getStats().getHealth().inc(powerRune.getPowerLevel() * Utils.random(7, 15));
	}

	/**
	 * Diminue les points de vie du {@link Champion} du nombre donn�.
	 *
	 * @param points
	 *            un entier positif ou nul repr�sentant un nombre de points de
	 *            vie.
	 */
	public void hit(int points) {
		if (points <= 0) {
			throw new IllegalArgumentException("The given hit points <"
					+ points + "> must be positive");
		}
		assertAlive();

		// D�cr�menter les points de vie (fait mourir le champion si plus de
		// points de vie)
		getStats().getHealth().dec(points);
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	protected void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	private void kill() {
		// On ne doit pas tester s'il est vivant en entr�e car cette m�thode est
		// appel�e au moment o� sa sant� passe � z�ro (quand il est d�j� mort)
		// assertAlive();

		// Jouer le son du cri
		SoundSystem.getInstance().play(getParty().getPosition(),
				AudioClip.CHAMPION_DIED);

		// L�cher tous ses objets sur le sol
		if (log.isDebugEnabled()) {
			log.debug(name + " is dropping its items ...");
		}

		// Vider l'inventaire
		final List<Item> items = inventory.empty();

		if (isLeader() && getParty().hasItem()) {
			// Ne pas oublier l'objet que le leader porte
			items.add(getParty().release());
		}

		// Ne pas oublier ce qu'il porte sur lui !
		items.addAll(body.removeAllItems());

		// Cr�er et ajouter les os du champion mort aux objets (pour pouvoir le
		// ressusciter)
		items.add(new Bones(this));

		// Emplacement du groupe ?
		final Element location = getParty().getElement();

		if (location != null) {
			// D�poser les objets au sol (au hasard) "devant" le groupe
			for (Item item : items) {
				location.addItem(item,
						Sector.randomVisible(party.getLookDirection()));
			}
		}

		// Ne pas supprimer le h�ros du groupe car quand il meurt il reste quand
		// m�me pr�sent mais � l'�tat "mort" dans le groupe
		// getParty().removeChampion(this);

		if (log.isInfoEnabled()) {
			log.info(getName() + " is dead");
		}

		// Lever un �v�nement. Permet entre autres de s�lectionner un nouveau
		// leader si l'actuel meurt !
		fireChangeEvent();
	}

	public final Stats getStats() {
		return stats;
	}

	/**
	 * R�ssuscite le {@link Champion}.
	 */
	public void resurrect() {
		if (isAlive()) {
			throw new IllegalStateException("The champion is alive");
		}

		if (log.isDebugEnabled()) {
			log.debug("Resurrecting " + name + " ...");
		}

		// Ajouter des points de vie au h�ros
		final Stat health = stats.getHealth();

		health.inc(Utils.random(1, health.maxValue()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() != stats) {
			// On ignore cet �v�nement
			return;
		}

		if (Stats.PROPERTY_HEALTH.equals(event.getPropertyName())) {
			final int oldHealth = ((Integer) event.getOldValue()).intValue();
			final int newHealth = ((Integer) event.getNewValue()).intValue();

			if ((oldHealth > 0) && (newHealth == 0)) {
				// Le h�ros vient de mourir
				if (log.isDebugEnabled()) {
					log.debug(name + " is dying ...");
				}

				kill();
			} else if ((oldHealth == 0) && (newHealth > 0)) {
				if (log.isInfoEnabled()) {
					// Le h�ros vient de ressusciter
					log.info(name + " has been resurrected ...");
				}

				// Inutile de le r�int�grer au groupe, il y est d�j� !
			}
		}
	}

	/**
	 * Indique si le {@link Champion} a faim.
	 *
	 * @return si le {@link Champion} a faim.
	 */
	public boolean isStarving() {
		// Le champion a faim si la stat "Food" est inf�rieure � 10%
		return getStats().getFood().isLow();
	}

	/**
	 * Indique si le {@link Champion} a soif.
	 *
	 * @return si le {@link Champion} a soif.
	 */
	public boolean isThirsty() {
		// Le champion a soif si la stat "Water" est inf�rieure � 10%
		return getStats().getWater().isLow();
	}

	/**
	 * Empoisonne le {@link Champion} avec un poison de force donn�e.
	 *
	 * @param powerRune
	 *            un {@link PowerRune} d�terminant la force de l'empoisonnement.
	 */
	public void poison(PowerRune powerRune) {
		if (powerRune == null) {
			throw new IllegalArgumentException("The given power rune is null");
		}

		poison.strengthen(powerRune);
	}

	/**
	 * Gu�rit le {@link Champion} avec un anti-dote (anti-poison) de force
	 * donn�e.
	 *
	 * @param powerRune
	 *            un {@link PowerRune} d�terminant la force de la gu�rison.
	 */
	public void cure(PowerRune powerRune) {
		if (powerRune == null) {
			throw new IllegalArgumentException("The given power rune is null");
		}

		poison.cure(powerRune);
	}

	/**
	 * Indique si le {@link Champion} est empoisonn�.
	 *
	 * @return si le {@link Champion} est empoisonn�.
	 */
	public boolean isPoisoned() {
		return poison.isActive();
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			// Dispatcher l'appel aux stats
			stats.clockTicked();

			// ... et aux sorts du champion
			spells.clockTicked();

			// ... et au poison
			poison.clockTicked();
		}

		// Continuer � animer le champion tant qu'il est dans le groupe et qu'il
		// est vivant
		return (party != null) && isAlive();
	}

	@Override
	public String toString() {
		return name;
	}

	public ChampionSpells getSpells() {
		return spells;
	}

	/**
	 * Retourne la lumi�re g�n�r�e par le {@link Champion}. Inclut les objets
	 * port�s par le {@link Champion}, les amulettes et les sorts.
	 *
	 * @return un entier positif ou nul dans l'intervalle [0-255].
	 */
	public int getLight() {
		// Lumi�re g�n�r�e par le sort FUL ?
		int light = spells.getLightValue();

		// Si le h�ros porte une torche, prendre en compte sa luminosit�
		final Item item1 = body.getWeaponHand().getItem();

		if (item1 instanceof Torch) {
			light += ((Torch) item1).getLight();
		}

		// Idem autre main
		final Item item2 = body.getShieldHand().getItem();

		if (item2 instanceof Torch) {
			light += ((Torch) item2).getLight();
		}

		// Si le h�ros porte l'amulette de lumi�re, la prendre en compte
		final Item item3 = body.getNeck().getItem();

		if ((item3 != null) && item3.getType().equals(Item.Type.ILLUMULET)) {
			light += 50;
		}

		// Retourner une valeur dans l'intervalle [0-255]
		return Utils.bind(light, 0, Constants.MAX_LIGHT);
	}

	/**
	 * Retourne les objets port�s par le {@link Champion}.
	 *
	 * @return une {@link List} de {@link Item}. Ne retourne jamais null.
	 */
	public List<Item> getItems() {
		final List<Item> items = new ArrayList<Item>(32);
		items.addAll(body.getItems());
		items.addAll(inventory.getItems());

		if (isLeader() && getParty().hasItem()) {
			items.add(getParty().getItem());
		}

		return items;
	}

	/**
	 * Notifie le {@link Champion} qu'il vient d'�tre attaqu�. Permet de
	 * m�moriser quand la derni�re attaque a eu lieu.
	 */
	public void championAttacked() {
		// M�moriser le num�ro de tic courant
		this.lastAttackTick = Clock.getInstance().getTickId();
	}

	int getLastAttackTick() {
		return this.lastAttackTick;
	}

	/**
	 * Retourne l'emplacement auquel est situ� ce {@link Champion} dans son
	 * groupe ou null s'il n'appartient � aucun groupe.
	 *
	 * @return une instance de {@link Location} ou null.
	 */
	public Location getLocation() {
		return (party != null) ? party.getLocation(this) : null;
	}

	public Sector getSector() {
		return (party != null) ? getLocation().toSector(getParty().getDirection()) : null;
	}

	/**
	 * Throws the given item.
	 *
	 * @param item
	 *            the item to throw. Can't be null.
	 */
	public void throwItem(Item item) {
		Validate.notNull(item, "The given item is null");

		if (party == null) {
			throw new IllegalStateException("The champion isn't inside a party");
		}
		if (party.getDungeon() == null) {
			throw new IllegalStateException("The champion's party isn't inside a dungeon");
		}

		final Direction throwDirection = getParty().getDirection();

		final Sector sector;

		// Determine the sector where the thrown item will appear on the
		// neighbour position
		switch (throwDirection) {
		case EAST:
			sector = getSector().isNorthern() ? Sector.NORTH_WEST : Sector.SOUTH_WEST;
			break;
		case NORTH:
			sector = getSector().isEastern() ? Sector.SOUTH_EAST : Sector.SOUTH_WEST;
			break;
		case SOUTH:
			sector = getSector().isEastern() ? Sector.NORTH_EAST : Sector.NORTH_WEST;
			break;
		case WEST:
			sector = getSector().isNorthern() ? Sector.NORTH_EAST : Sector.SOUTH_EAST;
			break;
		default:
			throw new RuntimeException("Unsupported direction " + throwDirection);
		}

		// TODO Compute the projectile range
		// The projectile is created on the neighbor position
		new ItemProjectile(item, getParty().getDungeon(), getParty().getFacingPosition(), throwDirection, sector, 30);

		// TODO The champion gained some experience
	}
}