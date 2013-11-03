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
import fr.ritaly.dungeonmaster.Location;
import fr.ritaly.dungeonmaster.Poison;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.Speed;
import fr.ritaly.dungeonmaster.SubCell;
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
import fr.ritaly.dungeonmaster.stat.Stat;
import fr.ritaly.dungeonmaster.stat.Stats;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Champion implements ChangeEventSource, PropertyChangeListener,
		ClockListener {

	private final Log log = LogFactory.getLog(Champion.class);

	/**
	 * Enum�ration des {@link Champion}s de Dungeon Master.
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

		private final String fullName;

		private Name(String fullName) {
			Validate.isTrue(!StringUtils.isBlank(fullName),
					"The given full name <" + fullName + "> is blank");

			this.fullName = fullName;
		}

		/**
		 * Retourne le sexe du {@link Champion}.
		 *
		 * @return une instance de {@link Gender}. Ne retourne jamais null.
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
				throw new UnsupportedOperationException();
			}
		}

		public String getFullName() {
			return fullName;
		}

		/**
		 * Retourne les comp�tences du {@link Champion} sous forme d'une
		 * {@link Map}.
		 *
		 * @return une {@link Map} contenant les comp�tences et leur niveau
		 *         associ�. Ne retourne jamais null.
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
				return new SkillMapBuilder().setSkills(Skill.WIZARD,
						Level.JOURNEYMAN, 4, 3, 2, 2).getSkills();
			case SONJA:
				return new SkillMapBuilder().setSkills(Skill.FIGHTER,
						Level.JOURNEYMAN, 3, 4, 2, 3).getSkills();
			case LEYLA:
				return new SkillMapBuilder().setSkills(Skill.NINJA,
						Level.JOURNEYMAN, 3, 3, 3, 4).getSkills();
			case MOPHUS:
				return new SkillMapBuilder().setSkills(Skill.PRIEST,
						Level.JOURNEYMAN, 2, 4, 3, 2).getSkills();
			case WUUF:
				return new SkillMapBuilder()
						.setSkills(Skill.NINJA, Level.APPRENTICE, 1, 2, 3, 4)
						.setSkills(Skill.PRIEST, Level.NOVICE, 0, 3, 2, 1)
						.getSkills();
			case STAMM:
				return new SkillMapBuilder().setSkills(Skill.FIGHTER,
						Level.JOURNEYMAN, 3, 4, 2, 2).getSkills();
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
				return new SkillMapBuilder().setSkills(Skill.FIGHTER,
						Level.JOURNEYMAN, 4, 0, 4, 0).getSkills();
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
				throw new UnsupportedOperationException();
			}
		}

		public void populateItems(Champion champion) {
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
				throw new UnsupportedOperationException(
						"Unsupported champion <" + this + ">");
			}
		}
	}

	/**
	 * Enum�ration des couleurs attribu�es aux {@link Champion}s quand ils
	 * rejoignent une {@link Party}.
	 */
	public static enum Color {
		RED,
		YELLOW,
		GREEN,
		BLUE;
	}

	/**
	 * Niveau d'exp�rience d'un {@link Champion} dans une {@link Skill} donn�e.
	 * Chaque {@link Level} est associ� � un intervalle de points d'exp�rience.
	 * Quand l'exp�rience d�passe ce seuil, le {@link Champion} change
	 * automatiquent de {@link Level}.<br>
	 * <br>
	 * Sources: <a href="http://dmweb.free.fr/?q=node/692">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Experience and
	 * Training</a>, <a href="http://dmweb.free.fr/?q=node/691">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Skills and
	 * Statistics</a>
	 */
	public static enum Level {
		// Niveaux possibles class�s du plus faible au plus fort
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
		 * Valeur du d�but de l'intervalle d'exp�rience associ� au {@link Level}
		 */
		private final int startRange;

		/**
		 * Valeur de fin de l'intervalle d'exp�rience associ� au {@link Level}
		 */
		private final int endRange;

		private final int span;

		private Level() {
			// La largeur de l'intervalle double � chaque niveau
			this.startRange = 500 << (ordinal() - 1);
			this.endRange = (500 << ordinal());
			this.span = endRange - startRange;
		}

		public int getStartRange() {
			return startRange;
		}

		public int getEndRange() {
			return endRange;
		}

		/**
		 * Retourne la "largeur" de l'intervalle de points d'exp�rience associ�
		 * � ce niveau de comp�tence. Repr�sente le nombre de points
		 * d'exp�rience que le champion doit acqu�rir pour monter de niveau.
		 *
		 * @return un nombre de points d'exp�rience.
		 */
		public int getRangeSpan() {
			return span;
		}

		/**
		 * Indique si le nombre de points d'exp�rience donn�s est contenu dans
		 * l'intervalle de points associ� � ce {@link Level}.
		 *
		 * @param experience
		 *            un nombre de points d'exp�rience.
		 * @return si le nombre de points d'exp�rience donn�s est contenu dans
		 *         l'intervalle de points associ� � ce {@link Level}.
		 */
		public boolean contains(int experience) {
			return (startRange <= experience) && (experience <= endRange);
		}

		/**
		 * Retourne la valeur associ�e au {@link Level}. Correspond � la valeur
		 * ordinale de l'enum.
		 *
		 * @return un entier positif ou nul.
		 */
		public int getValue() {
			return ordinal();
		}

		/**
		 * Convertit le nombre de points d'exp�rience donn� en une instance de
		 * {@link Level}. L�ve une {@link IllegalArgumentException} si la
		 * conversion est impossible.
		 *
		 * @param points
		 *            un entier positif repr�sentant un nombre de points
		 *            d'exp�rience.
		 * @return une instance de {@link Level}. Ne retourne jamais null.
		 */
		public static Level fromExperience(int points) {
			for (Level level : values()) {
				if (level.contains(points)) {
					return level;
				}
			}

			throw new IllegalArgumentException(
					"Unable to find a level for experience points <" + points
							+ ">");
		}

		public static void main(String[] args) {
			for (Level level : values()) {
				System.out.println("Level " + level + ": "
						+ level.getStartRange() + " -> " + level.getEndRange()
						+ " [" + level.getRangeSpan() + "]");
			}
		}
	}

	private final String name;

	private Gender gender;

	/**
	 * Le groupe auquel est rattach� le {@link Champion}.
	 */
	private Party party;

	/**
	 * La {@link Color} assign�e au {@link Champion} lorsqu'il a rejoint le
	 * groupe.
	 */
	private Color color;

	private final Body body;

	private final Inventory inventory;

	/**
	 * L'identifiant du dernier num�ro de tic d'horloge pendant lequel le
	 * {@link Champion} a �t� attaqu� ou -1 si le {@link Champion} n'a jamais
	 * �t� attaqu�.
	 */
	private int lastAttackTick = -1;

	/**
	 * L'instance de {@link SpellCaster} associ�e au {@link Champion} et lui
	 * permettant de lancer des sorts.
	 */
	private final SpellCaster spellCaster = new SpellCaster(this);

	/**
	 * La Map contenant l'exp�rience du {@link Champion} dans chacune de ses
	 * {@link Skill}s.
	 */
	private final Map<Skill, Experience> skills;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	private final Stats stats;

	/**
	 * Permet de g�rer l'empoisonnement du {@link Champion}.
	 */
	private final Poison poison = new Poison();

	/**
	 * Les sorts lanc�s par le {@link Champion}.
	 */
	private final ChampionSpells spells;

	private final Temporizer temporizer;

	// Il faut passer par la fabrique ChampionFactory afin de cr�er des
	// champions "complets"
	Champion(String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("The given name <" + name
					+ "> is blank");
		}

		this.name = name;

		// L'inventaire doit �tre instanci� apr�s avoir m�moris� le nom du
		// champion
		this.inventory = new Inventory(this);

		// Les stats doivent �tre instanci�es apr�s avoir m�moris� le nom du
		// champion
		this.stats = new Stats(this);
		this.stats.addPropertyChangeListener(this);

		// Le corps doit �tre instanci� apr�s pour la m�me raison
		this.body = new Body(this);

		// Idem
		this.spells = new ChampionSpells(this);

		// Idem
		this.temporizer = new Temporizer(name, 5);

		// Initialiser toutes les skills du champion � NONE
		final Map<Skill, Experience> map = new HashMap<Skill, Experience>();

		for (Skill skill : Skill.values()) {
			map.put(skill, new Experience(this, skill, Level.NONE));
		}

		// Figer la map skills
		this.skills = Collections.unmodifiableMap(map);
	}

	/**
	 * Augmente du nombre de points d'exp�rience donn�s la comp�tence du
	 * {@link Champion}. Promeut automatiquement le {@link Champion} si sa
	 * comp�tence est suffisante.
	 *
	 * @param skill
	 *            la {@link Skill} dont l'exp�rience doit augmenter.
	 * @param points
	 *            un entier positif ou nul repr�sentant le nombre de points
	 *            d'exp�rience acquis par le {@link Champion}.
	 */
	public void gainExperience(Skill skill, int points) {
		if (skill == null) {
			throw new IllegalArgumentException("The given skill is null");
		}
		if (points <= 0) {
			throw new IllegalArgumentException("The given points " + points
					+ " must be positive");
		}
		assertAlive();

		if (skill.isBasic()) {
			// Traiter la comp�tence basique
			getExperience(skill).gain(points);
		} else {
			// Traiter la comp�tence cach�e
			getExperience(skill).gain(points);

			// puis la comp�tence basique associ�e
			getExperience(skill.getRelatedSkill()).gain(points);
		}
	}

	/**
	 * Retourne les comp�tences du {@link Champion} sous forme de {@link List}.
	 *
	 * @return une {@link List} de {@link Skill}. Ne retourne jamais null.
	 */
	public List<Skill> getSkills() {
		// Recopie d�fensive
		return new ArrayList<Skill>(skills.keySet());
	}

	/**
	 * D�finit le niveau de comp�tence <b>de base</b> du h�ros dans la
	 * {@link Skill} donn�e, c'est-�-dire le niveau ind�pendant du bonus de
	 * comp�tence.
	 *
	 * @param skill
	 *            la {@link Skill} dont le niveau doit �tre positionn�.
	 * @param level
	 *            le {@link Level} � d�finir pour la {@link Skill}.
	 */
	public void setSkill(Skill skill, Level level) {
		if (skill == null) {
			throw new IllegalArgumentException("The given skill is null");
		}
		if (level == null) {
			throw new IllegalArgumentException("The given level is null");
		}

		// Ne peut pas retourner null
		skills.get(skill).setLevel(level);
	}

	/**
	 * Retourne l'exp�rience associ�e � la {@link Skill} donn�e du
	 * {@link Champion}.
	 *
	 * @param skill
	 *            la {@link Skill} pour laquelle on demande l'exp�rience.
	 * @return une instance de {@link Experience}. Ne retourne jamais null.
	 */
	public Experience getExperience(Skill skill) {
		if (skill == null) {
			throw new IllegalArgumentException("The given skill is null");
		}

		// Ne peut pas retourner null
		return skills.get(skill);
	}

	private void assertAlive() {
		if (isDead()) {
			throw new IllegalStateException("The champion is dead");
		}
	}

	/**
	 * Fait invoquer le {@link Rune} donn� au {@link Champion}. Cette m�thode
	 * g�re la consommation de mana, la coh�rence de la s�quence de {@link Rune}
	 * s invoqu�s, etc.
	 *
	 * @param rune
	 *            le {@link Rune} que le {@link Champion} doit invoquer.
	 * @throws NotEnoughManaException
	 *             si le {@link Champion} ne dispose pas d'assez de mana pour
	 *             invoquer le {@link Rune}.
	 */
	public void cast(Rune rune) throws NotEnoughManaException {
		if (rune == null) {
			throw new IllegalArgumentException("The given rune is null");
		}
		assertAlive();

		// V�rifier que le champion a assez de mana pour invoquer ce rune
		final Stat mana = getStats().getMana();

		final int cost;

		// D�terminer le co�t d'invocation du rune
		if (Rune.Type.POWER.equals(rune.getType())) {
			cost = rune.getCost();
		} else {
			// R�cup�rer le power rune pr�c�demment invoqu�
			final PowerRune powerRune = spellCaster.getPowerRune();

			if (powerRune == null) {
				throw new IllegalStateException(
						"There is no defined power rune");
			}

			cost = rune.getCost(powerRune);
		}

		if (mana.actualValue() >= cost) {
			// Invoquer le rune (contr�le la s�quence d'appel des runes)
			spellCaster.cast(rune);

			// D�cr�menter la mana
			mana.dec(cost);

			// Le champion gagne de l'exp�rience uniquement si le sort r�ussit
			// pas au moment o� il invoque le rune
		} else {
			// Pas assez de mana, le signaler
			throw new NotEnoughManaException();
		}
	}

	public void cast(PowerRune powerRune, Spell.Type spellType)
			throws NotEnoughManaException, ChampionMumblesNonsenseException,
			EmptyFlaskNeededException, SkillTooLowException {

		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(spellType, "The given spell type is null");

		// Rune de puissance
		cast(powerRune);

		// Runes composant le sort (1 � 3)
		for (Rune rune : spellType.getRunes()) {
			cast(rune);
		}
	}

	public Spell cast(PowerRune powerRune, ElementRune elementRune)
			throws NotEnoughManaException, ChampionMumblesNonsenseException,
			EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		return castConcrete(powerRune, elementRune, null, null, 2);
	}

	public Spell cast(PowerRune powerRune, ElementRune elementRune,
			FormRune formRune) throws NotEnoughManaException,
			ChampionMumblesNonsenseException, EmptyFlaskNeededException,
			SkillTooLowException, EmptyHandNeededException {

		return castConcrete(powerRune, elementRune, formRune, null, 3);
	}

	public Spell cast(PowerRune powerRune, ElementRune elementRune,
			FormRune formRune, AlignmentRune alignmentRune)
			throws NotEnoughManaException, ChampionMumblesNonsenseException,
			EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		return castConcrete(powerRune, elementRune, formRune, alignmentRune, 4);
	}

	private Spell castConcrete(PowerRune powerRune, ElementRune elementRune,
			FormRune formRune, AlignmentRune alignmentRune, int count)
			throws NotEnoughManaException, ChampionMumblesNonsenseException,
			EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		Validate.isTrue(powerRune != null, "The given power rune is null");
		Validate.isTrue(elementRune != null, "The given element rune is null");

		if (count >= 3) {
			Validate.isTrue(formRune != null, "The given form rune is null");

			if (count == 4) {
				Validate.isTrue(alignmentRune != null,
						"The given alignment rune is null");
			}
		}

		if (spellCaster.getRuneCount() > 0) {
			// Il ne doit y avoir aucun rune d�j� invoqu�
			throw new IllegalStateException("There are "
					+ spellCaster.getRuneCount() + " already invoked");
		}

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
	 * Lance le sort en cours d'invocation par le {@link Champion}. L'exp�rience
	 * du {@link Champion} augmente si le sort r�ussit et sa main est rendue
	 * indisponible pendant un temps fonction du sort invoqu�.
	 *
	 * @return une instance de {@link Spell} repr�sentant le sort invoqu�.
	 * @throws ChampionMumblesNonsenseException
	 *             si le sort n'est pas valide.
	 * @throws EmptyFlaskNeededException
	 *             si le sort requiert une fiole vide qui n'est pas pr�sente.
	 * @throws SkillTooLowException
	 *             si le {@link Champion} n'est pas assez comp�tent pour
	 *             invoquer le sort.
	 * @throws EmptyHandNeededException
	 *             si le sort demande que l'une des mains du {@link Champion}
	 *             soit vide.
	 */
	public Spell castSpell() throws ChampionMumblesNonsenseException,
			EmptyFlaskNeededException, SkillTooLowException,
			EmptyHandNeededException {

		assertAlive();

		// On cr�e juste l'instance de Spell sans effacer les runes pour g�rer
		// le cas sp�cial de la cr�ation de potions (cf plus bas)
		final Spell spell = spellCaster.cast(true);

		// Le sort invoqu� est-il valide ?
		if (!spell.isValid()) {
			// Non. Supprimer les runes formul�s
			spellCaster.clear();

			throw new ChampionMumblesNonsenseException();
		}

		// Le champion a-t-il assez de comp�tence pour invoquer ce sort ?
		if (!spell.canBeCastBy(this)) {
			// Non. Supprimer les runes formul�s
			spellCaster.clear();

			throw new SkillTooLowException("The champion's skill "
					+ spell.getSkill() + " is too low to cast spell "
					+ spell.getName() + " (actual: "
					+ getLevel(spell.getSkill()) + ", minimum: "
					+ spell.getType().getRequiredLevel() + ")");
		}

		// Pr�requis pour que le sort fonctionne ?
		if (spell.getType().requiresEmptyFlask()) {
			// Le sort requiert une fiole vide pour �tre invoqu�

			// Le champion tient-il une fiole vide dans l'une de ses mains ?
			final Item item1 = getBody().getWeaponHand().getItem();
			final Item item2 = getBody().getShieldHand().getItem();

			final boolean emptyFlask1 = (item1 != null)
					&& item1.getType().equals(Item.Type.EMPTY_FLASK);
			final boolean emptyFlask2 = (item2 != null)
					&& item2.getType().equals(Item.Type.EMPTY_FLASK);

			if (!emptyFlask1 && !emptyFlask2) {
				// Aucune fiole vide, lever une erreur en conservant les runes
				// d�j� formul�s
				throw new EmptyFlaskNeededException();
			}
		}

		// Supprimer les runes formul�s
		spellCaster.clear();

		// Comp�tence mise en oeuvre par le sort (peut �tre nul !)
		final Skill skill = spell.getSkill();

		if (skill != null) {
			// Le champion gagne de l'exp�rience. Nombre de points gagn�s ?
			gainExperience(skill, spell.getEarnedExperience());
		}

		final int duration = spell.getDuration();

		if (duration > 0) {
			// Rendre la main du champion indisponible (uniquement si le sort a
			// une "dur�e" d'indisponibilit�)
			body.getWeaponHand().disable(duration);
		}

		// Laisser le sort "agir" sur le champion
		spell.actUpon(this);

		return spell;
	}

	public String getName() {
		return name;
	}

	public Gender getGender() {
		return gender;
	}

	void setGender(Gender gender) {
		if (this.gender != null) {
			// On ne peut positionner le sexe qu'une seule fois
			throw new IllegalStateException(
					"The champion's gender is already defined");
		}

		this.gender = gender;
	}

	public Party getParty() {
		return party;
	}

	// Cette m�thode ne doit �tre appel�e que depuis la classe Party
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

			// Enregistrer le champion
			Clock.getInstance().register(this);
		}
	}

	public Color getColor() {
		return color;
	}

	void setColor(Color color) {
		// color peut �tre null (quand le h�ros quitte le groupe). Cette m�thode
		// ne doit �tre appel�e que depuis la classe Party
		this.color = color;
	}

	public Body getBody() {
		return body;
	}

	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Indique si le {@link Champion} est le leader de son groupe.
	 *
	 * @return si le {@link Champion} est le leader de son groupe.
	 */
	public boolean isLeader() {
		return (party != null) && (party.getLeader() == this);
	}

	/**
	 * Retourne la charge maximale (en kilogrammes) que peut porter ce
	 * {@link Champion}.
	 *
	 * @return un float repr�sentant un nombre de kilogrammes.
	 */
	public final float getMaxLoad() {
		return getStats().getActualMaxLoad();
	}

	/**
	 * Retourne le niveau <b>r�el</b> du champion pour la {@link Skill} donn�e,
	 * c'est-�-dire que la valeur retourn�e comprend l'�ventuel bonus de
	 * comp�tence.
	 *
	 * @param skill
	 *            une instance de {@link Skill} dont le niveau associ� est
	 *            demand�.
	 * @return une instance de {@link Level}. Ne retourne jamais null.
	 */
	public Level getLevel(Skill skill) {
		if (skill == null) {
			throw new IllegalArgumentException("The given skill is null");
		}

		return skills.get(skill).getActualLevel();
	}

	/**
	 * Retourne la charge actuellement port�e par le {@link Champion}. S'il est
	 * leader de son groupe, cela inclut l'�ventuel {@link Item} que le leader
	 * porte en main.
	 *
	 * @return un float repr�sentant un nombre de kilogrammes.
	 */
	public float getLoad() {
		float weight = inventory.getTotalWeight() + body.getTotalWeight();

		if ((party != null) && isLeader() && party.hasItem()) {
			// Prendre en compte aussi l'objet qu'il porte s'il est leader du
			// groupe
			weight += getParty().getItem().getWeight();
		}

		return weight;
	}

	/**
	 * Retourne la vitesse de d�placement du champion compte tenu de son �tat
	 * actuel. Le retour de la m�thode d�pend de si le h�ros est bless�,
	 * surcharg�, etc.
	 *
	 * @return une instance de {@link Speed}.
	 */
	public Speed getMoveSpeed() {
		if (isDead()) {
			// Champion mort, vitesse non d�finie
			return Speed.UNDEFINED;
		}
		if (getLoad() >= getMaxLoad()) {
			// Champion en surcharge
			return Speed.SLOW;
		}
		if (getBody().getFeet().isWounded() || getBody().getLegs().isWounded()) {
			// Pieds ou jambes bless�(e)s. Une blessure ailleurs ne ralentit
			// pas le champion
			return Speed.SLOW;
		}
		if (getBody().getFeet().hasItem(Item.Type.BOOTS_OF_SPEED)) {
			// Bottes de vitesse
			return Speed.FAST;
		}

		// FIXME G�rer les combinatoires de vitesse: bottes de vitesse mais
		// bless�, etc.

		// Vitesse de d�placement normale
		return Speed.NORMAL;
	}

	/**
	 * Fait mourir le {@link Champion} (s'il est vivant) et retourne si
	 * l'op�ration a r�ussi.
	 *
	 * @return si le {@link Champion} �tait vivant et qu'il vient de mourir.
	 */
	public boolean die() {
		if (isAlive()) {
			getStats().getHealth().value(0);

			return true;
		}

		return false;
	}

	/**
	 * Indique si le {@link Champion} est vivant.
	 *
	 * @return si le {@link Champion} est vivant.
	 */
	public boolean isAlive() {
		return (getStats().getHealth().actualValue() > 0);
	}

	/**
	 * Indique si le {@link Champion} est mort.
	 *
	 * @return si le {@link Champion} est mort.
	 */
	public final boolean isDead() {
		return !isAlive();
	}

	/**
	 * Retourne le bonus de r�sistance au feu du {@link Champion} calcul� �
	 * partir des objets qu'il porte sur lui et des sorts actifs.
	 *
	 * @return un entier positif ou nul repr�sentant un bonus de r�sistance au
	 *         feu.
	 */
	public int getAntiMagic() {
		// Prendre en compte le bonus conf�r� par les objets port�s
		int antiMagic = body.getAntiMagic();

		if (party != null) {
			// ... et les sorts (du groupe)
			antiMagic += party.getSpells().getAntiMagic().actualValue();
		}

		return antiMagic;
	}

	/**
	 * Retourne le bonus de d�fense du {@link Champion} calcul� � partir des
	 * objets qu'il porte sur lui et des sorts actifs.
	 *
	 * @return un entier positif ou nul repr�sentant un bonus de d�fense.
	 */
	public int getShield() {
		// Prendre en compte le bonus conf�r� par les objets port�s
		int shield = body.getShield();

		// Prendre en compte le bonus de d�fense du sort associ� au champion
		// shield += spells.getShield().actualValue();

		if (party != null) {
			// ... et les sorts (du groupe)
			shield += party.getSpells().getShield().actualValue();
		}

		return shield;
	}

	/**
	 * Fait consommer l'objet donn� au {@link Champion} et retourne l'objet
	 * r�sultant de l'op�ration. Si l'objet n'est pas consommable, retourne le
	 * param�tre tel quel. Autrement consomme l'objet et retourne null si
	 * celui-ci est d�truit par l'op�ration ou retourne un autre objet dans
	 * lequel il s'est chang� (Ex: Vidage d'une fiole d'eau -> Fiole vide).
	 *
	 * @param item
	 *            un {@link Item} que le {@link Champion} doit consommer (boire
	 *            ou manger).
	 * @return l'objet dans lequel l'objet pass� en param�tre s'est chang� s'il
	 *         y a lieu, null selon les cas ou alors l'objet en param�tre
	 *         lui-m�me.
	 */
	public Item consume(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("The given item is null");
		}

		return item.itemConsumed(this);
	}

	/**
	 * Gu�rit le {@link Champion} avec la puissance du {@link PowerRune} donn�.
	 *
	 * @param powerRune
	 *            un {@link PowerRune} qui d�termine la puissance de la gu�rison
	 *            appliqu�e au {@link Champion}.
	 */
	public void heal(PowerRune powerRune) {
		if (powerRune == null) {
			throw new IllegalArgumentException("The given power rune is null");
		}
		assertAlive();

		// TODO D�terminer le nombre de points de sant� restaur�s
		getStats().getHealth().inc(
				powerRune.getPowerLevel() * Utils.random(7, 15));
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
				location.itemDroppedDown(item,
						SubCell.randomVisible(party.getLookDirection()));
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

	public SubCell getSubCell() {
		return (party != null) ? getLocation().toSubCell(
				getParty().getDirection()) : null;
	}
}