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
package fr.ritaly.dungeonmaster.magic;

import static fr.ritaly.dungeonmaster.magic.AlignmentRune.DAIN;
import static fr.ritaly.dungeonmaster.magic.AlignmentRune.KU;
import static fr.ritaly.dungeonmaster.magic.AlignmentRune.NETA;
import static fr.ritaly.dungeonmaster.magic.AlignmentRune.RA;
import static fr.ritaly.dungeonmaster.magic.AlignmentRune.ROS;
import static fr.ritaly.dungeonmaster.magic.AlignmentRune.SAR;
import static fr.ritaly.dungeonmaster.magic.ElementRune.DES;
import static fr.ritaly.dungeonmaster.magic.ElementRune.FUL;
import static fr.ritaly.dungeonmaster.magic.ElementRune.OH;
import static fr.ritaly.dungeonmaster.magic.ElementRune.VI;
import static fr.ritaly.dungeonmaster.magic.ElementRune.YA;
import static fr.ritaly.dungeonmaster.magic.ElementRune.ZO;
import static fr.ritaly.dungeonmaster.magic.FormRune.BRO;
import static fr.ritaly.dungeonmaster.magic.FormRune.EW;
import static fr.ritaly.dungeonmaster.magic.FormRune.GOR;
import static fr.ritaly.dungeonmaster.magic.FormRune.IR;
import static fr.ritaly.dungeonmaster.magic.FormRune.KATH;
import static fr.ritaly.dungeonmaster.magic.FormRune.VEN;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Level;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.Potion;
import fr.ritaly.dungeonmaster.projectile.Projectile;
import fr.ritaly.dungeonmaster.projectile.SpellProjectile;

/**
 * Un sort lanc� par un champion, une cr�ature ou un pi�ge. Un sort est cr�� en
 * invoquant des runes de type {@link PowerRune}, {@link ElementRune},
 * {@link FormRune} et {@link AlignmentRune}. Au minimum, le sort requiert un
 * {@link PowerRune} pour d�terminer sa puissance et un {@link ElementRune}. Le
 * sort peut aussi prendre un {@link FormRune} en plus (3 runes en tout) ou un
 * {@link FormRune} et un {@link AlignmentRune} en plus (4 runes en tout). C'est
 * la combinaison de {@link ElementRune}, {@link FormRune} et
 * {@link AlignmentRune} qui d�termine si le sort est valide: seules certaines
 * combinaisons sont valides. Si un sort n'est pas valide cela n'emp�che donc
 * <b>pas</b> de cr�er l'instance de {@link Spell}. Pour v�rifier la validit� du
 * sort, appeler la m�thode {@link #isValid()}. Pour identifier le sort, appeler
 * sa m�thode {@link #getType()}.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Spell {

	/**
	 * Enumerates the different types of spell.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Type {

		// --- Priest spells --- //

		HEALTH_POTION(VI, 32, 1),
		STAMINA_POTION(YA, 15, 2),
		MANA_POTION(ZO, BRO, RA, 63, 3),
		STRENGTH_POTION(FUL, BRO, KU, 15, 4),
		DEXTERITY_POTION(OH, BRO, ROS, 15, 4),
		WISDOM_POTION(YA, BRO, DAIN, 15, 4),
		VITALITY_POTION(YA, BRO, NETA, 15, 4),
		ANTIDOTE_POTION(VI, BRO, 26, 1),
		SHIELD_POTION(YA, BRO, 25, 2),
		ANTI_MAGIC(FUL, BRO, NETA, 28, 4),

		/**
		 * Note: This spell is a party spell. The shield per champion is gained
		 * by drinking shield potions.
		 */
		SHIELD(YA, IR, 30, 2),
		DARKNESS(DES, IR, SAR, 12, 1),
		DISPELL_ILLUSION(OH, GOR, ROS, 0, 0),

		// --- Wizard spells --- //

		TORCH(FUL, 15, 1),
		LIGHT(OH, IR, RA, 22, 4),
		OPEN_DOOR(ZO, 15, 1),
		MAGIC_FOOTPRINTS(YA, BRO, ROS, 18, 1),
		SEE_THROUGH_WALLS(OH, EW, RA, 33, 3),
		INVISIBILITY(OH, EW, SAR, 45, 3),
		POISON_POTION(ZO, VEN, 30, 2),
		POISON_BOLT(DES, VEN, 16, 1),
		POISON_CLOUD(OH, VEN, 27, 3),
		WEAKEN_IMMATERIAL(DES, EW, 20, 1),
		FIREBALL(FUL, IR, 42, 3),
		LIGHTNING_BOLT(OH, KATH, RA, 30, 4),
		ZO_KATH_RA(ZO, KATH, RA, 15, 0);

		/**
		 * L'identifiant du sort calcul� � partir des runes qui sont n�cessaires
		 * pour invoquer le sort.
		 */
		private final int id;

		/**
		 * La dur�e d'effet du sort en 1/6 de seconde.
		 */
		private final int duration;

		/**
		 * La difficult� � invoquer le sort. Permet de d�terminer si un
		 * {@link Champion} est assez comp�tent pour invoquer un sort donn�.
		 */
		private final int difficulty;

		private final ElementRune elementRune;

		private final FormRune formRune;

		private final AlignmentRune alignmentRune;

		private Type(final ElementRune elementRune, int duration, int difficulty) {
			Validate.notNull(elementRune, "The given element rune is null");
			Validate.isTrue(duration >= 0, String.format("The given duration %d must be positive or zero", duration));
			Validate.isTrue(difficulty >= 0, String.format("The given difficulty %d must be positive or zero", difficulty));

			this.id = elementRune.getId();
			// secs -> ticks
			this.duration = duration * Clock.ONE_SECOND;
			this.difficulty = difficulty;

			this.elementRune = elementRune;
			this.formRune = null;
			this.alignmentRune = null;
		}

		private Type(final ElementRune elementRune, final FormRune formRune, int duration, int difficulty) {
			Validate.notNull(elementRune, "The given element rune is null");
			Validate.notNull(formRune, "The given form rune is null");
			Validate.isTrue(duration >= 0, String.format("The given duration %d must be positive or zero", duration));
			Validate.isTrue(difficulty >= 0, String.format("The given difficulty %d must be positive or zero", difficulty));

			this.id = elementRune.getId() * 10 + formRune.getId();
			this.duration = duration;
			this.difficulty = difficulty;

			this.elementRune = elementRune;
			this.formRune = formRune;
			this.alignmentRune = null;
		}

		private Type(final ElementRune elementRune, final FormRune formRune, final AlignmentRune alignmentRune, int duration,
				int difficulty) {

			Validate.notNull(elementRune, "The given element rune is null");
			Validate.notNull(formRune, "The given form rune is null");
			Validate.notNull(alignmentRune, "The given alignment rune is null");
			Validate.isTrue(duration >= 0, String.format("The given duration %d must be positive or zero", duration));
			Validate.isTrue(difficulty >= 0, String.format("The given difficulty %d must be positive or zero", difficulty));

			this.id = (elementRune.getId() * 10 + formRune.getId()) * 10
					+ alignmentRune.getId();
			this.duration = duration;
			this.difficulty = difficulty;

			this.elementRune = elementRune;
			this.formRune = formRune;
			this.alignmentRune = alignmentRune;
		}

		/**
		 * Tells whether this spell is a priest spell.
		 *
		 * @return whether this spell is a priest spell.
		 */
		public boolean isPriestSpell() {
			final int id = ordinal();

			return (HEALTH_POTION.ordinal() <= id) && (id <= DISPELL_ILLUSION.ordinal());
		}

		/**
		 * Tells whether this spell is a wizard spell.
		 *
		 * @return whether this spell is a wizard spell.
		 */
		public boolean isWizardSpell() {
			return !isPriestSpell();
		}

		/**
		 * Tells whether this spell is an attack spell.
		 *
		 * @return whether this spell is an attack spell.
		 */
		public boolean isAttackSpell() {
			switch(this) {
			case FIREBALL:
			case LIGHTNING_BOLT:
			case POISON_BOLT:
			case POISON_CLOUD:
			case WEAKEN_IMMATERIAL:
				return true;
			default:
				return false;
			}
		}

		/**
		 * Returns the number of runes to cast this spell.
		 *
		 * @return a number of runes. Value within [1, 3] because the power rune
		 *         doesn't identify a spell.
		 */
		public int getRuneCount() {
			// The element rune is always present
			int count = 1;

			if (formRune != null) {
				count++;

				if (alignmentRune != null) {
					count++;
				}
			}

			return count;
		}

		/**
		 * Retourne l'instance de {@link Type} associ�e � l'identifiant de sort
		 * donn�.
		 *
		 * @param id
		 *            un entier repr�sentant un identifiant de sort.
		 * @return une instance de {@link Type} ou null si l'identifiant donn�
		 *         est invalide.
		 */
		public static Type byValue(int id) {
			for (Type anId : values()) {
				if (anId.id == id) {
					return anId;
				}
			}

			return null;
		}

		/**
		 * Indique si ce type de sort produit un projectile (missile).
		 *
		 * @return si ce type de sort produit un projectile (missile).
		 */
		public boolean isProjectile() {
			switch (this) {
			case FIREBALL:
			case LIGHTNING_BOLT:
			case OPEN_DOOR:
			case POISON_BOLT:
			case POISON_CLOUD:
			case WEAKEN_IMMATERIAL:
				return true;
			default:
				return false;
			}
		}

		/**
		 * Indique si ce type de sort produit une potion.
		 *
		 * @return si ce type de sort produit une potion.
		 */
		public boolean isPotion() {
			switch (this) {
			case ANTIDOTE_POTION:
			case DEXTERITY_POTION:
			case HEALTH_POTION:
			case MANA_POTION:
			case POISON_POTION:
			case SHIELD_POTION:
			case STAMINA_POTION:
			case STRENGTH_POTION:
			case VITALITY_POTION:
			case WISDOM_POTION:
				return true;
			default:
				return false;
			}
		}

		/**
		 * Indique si l'identifiant de sort donn� est valide.
		 *
		 * @param id
		 *            un entier repr�sentant un identifiant de sort.
		 * @return si l'identifiant de sort donn� est valide.
		 */
		public static boolean isValid(int id) {
			return (byValue(id) != null);
		}

		/**
		 * Retourne le niveau requis dans la comp�tence du sort (cf
		 * {@link #getSkill()}) pour r�ussir celui-ci.
		 *
		 * @return une instance de {@link Level}. Ne retourne jamais null.
		 */
		public Champion.Level getRequiredLevel() {
			switch (this) {
			case HEALTH_POTION:
			case STAMINA_POTION:
			case STRENGTH_POTION:
			case DEXTERITY_POTION:
			case WISDOM_POTION:
			case VITALITY_POTION:
			case ANTIDOTE_POTION:
			case LIGHT:
			case OPEN_DOOR:
			case INVISIBILITY:
			case LIGHTNING_BOLT:
				// Niveau 3
				return Champion.Level.APPRENTICE;
			case MANA_POTION:
			case ZO_KATH_RA:
				// Niveau 1
				return Champion.Level.NEOPHYTE;
			case SHIELD_POTION:
			case ANTI_MAGIC:
			case SHIELD:
			case DARKNESS:
			case SEE_THROUGH_WALLS:
			case POISON_POTION:
			case POISON_BOLT:
			case POISON_CLOUD:
				// Niveau 5
				return Champion.Level.CRAFTSMAN;
			case TORCH:
			case FIREBALL:
				// Niveau 2
				return Champion.Level.NOVICE;
			case MAGIC_FOOTPRINTS:
			case WEAKEN_IMMATERIAL:
				// Niveau 4
				return Champion.Level.JOURNEYMAN;
			default:
				// Niveau 0
				return Champion.Level.NONE;
			}
		}

		/**
		 * Retourne la comp�tence mise en oeuvre lors de l'invocation de ce
		 * sort. Permet de faire gagner de l'exp�rience au champion qui r�ussit
		 * le sort. Peut retourner null si aucune comp�tence particuli�re n'est
		 * requise.
		 *
		 * @return une instance de {@link Skill} ou null.
		 */
		public Skill getSkill() {
			switch (this) {
			case HEALTH_POTION:
			case STAMINA_POTION:
			case STRENGTH_POTION:
			case DEXTERITY_POTION:
			case WISDOM_POTION:
			case VITALITY_POTION:
			case ANTIDOTE_POTION:
				// Comp�tence #13
				return Skill.HEAL;
			case MANA_POTION:
				// Comp�tence #2
				return Skill.PRIEST;
			case SHIELD_POTION:
			case ANTI_MAGIC:
			case SHIELD:
			case DARKNESS:
			case SEE_THROUGH_WALLS:
				// Comp�tence #15
				return Skill.DEFEND;
			case TORCH:
			case FIREBALL:
				// Comp�tence #16
				return Skill.FIRE;
			case LIGHT:
			case OPEN_DOOR:
			case INVISIBILITY:
			case LIGHTNING_BOLT:
				// Comp�tence #17
				return Skill.AIR;
			case MAGIC_FOOTPRINTS:
			case WEAKEN_IMMATERIAL:
				// Comp�tence #18
				return Skill.EARTH;
			case POISON_POTION:
			case POISON_BOLT:
			case POISON_CLOUD:
				// Comp�tence #19
				return Skill.WATER;
			case ZO_KATH_RA:
				// Comp�tence #3
				return Skill.WIZARD;
			default:
				// Aucune comp�tence sp�ciale requise !
				return null;
			}
		}

		/**
		 * Retourne les {@link Rune}s composant le sort (n'inclut pas le rune de
		 * puissance).
		 *
		 * @return une {@link List} de {@link Rune}s. Ne retourne jamais null.
		 */
		public List<Rune> getRunes() {
			final List<Rune> runes = new ArrayList<Rune>(3);
			runes.add(elementRune);

			if (formRune != null) {
				runes.add(formRune);

				if (alignmentRune != null) {
					runes.add(alignmentRune);
				}
			}

			return runes;
		}

		/**
		 * Indique si l'invocation du sort requiert une fiole vide pour r�ussir.
		 *
		 * @return si l'invocation du sort requiert une fiole vide pour r�ussir.
		 */
		public boolean requiresEmptyFlask() {
			switch (this) {
			case ANTIDOTE_POTION:
			case DEXTERITY_POTION:
			case HEALTH_POTION:
			case MANA_POTION:
			case POISON_POTION:
			case SHIELD_POTION:
			case STAMINA_POTION:
			case STRENGTH_POTION:
			case VITALITY_POTION:
			case WISDOM_POTION:
				return true;
			default:
				return false;
			}
		}

		public int getDuration() {
			return duration;
		}

		public int getDifficulty() {
			return difficulty;
		}

		/**
		 * Retourne le nom du sort � partir des {@link Rune}s le composant.
		 * Exemple: "FUL", "FUL IR", etc.
		 *
		 * @return une {@link String} repr�sentant le nom complet du sort.
		 */
		public String getName() {
			final StringBuilder builder = new StringBuilder(32);

			builder.append(elementRune.name());

			if (formRune != null) {
				builder.append(' ');
				builder.append(formRune.name());

				if (alignmentRune != null) {
					builder.append(' ');
					builder.append(alignmentRune.name());
				}
			}

			return builder.toString();
		}

		public ElementRune getElementRune() {
			return elementRune;
		}

		public FormRune getFormRune() {
			return formRune;
		}

		public AlignmentRune getAlignmentRune() {
			return alignmentRune;
		}
	}

	/**
	 * Le {@link Rune} de puissance du sort. Forc�ment non null.
	 */
	private final PowerRune powerRune;

	/**
	 * Le {@link Rune} de type {@link ElementRune} du sort. Forc�ment non null.
	 */
	private final ElementRune elementRune;

	/**
	 * Le {@link Rune} de type {@link FormRune} du sort. Peut �tre null.
	 */
	private final FormRune formRune;

	/**
	 * Le {@link Rune} de type {@link AlignmentRune} du sort. Peut �tre null.
	 */
	private final AlignmentRune alignmentRune;

	/**
	 * L'identifiant du sort sous forme d'entier. Sa valeur est calcul�e �
	 * partir des {@link ElementRune}, {@link FormRune} et {@link AlignmentRune}
	 * du sort. Ne prend donc pas en compte le {@link Rune} de puissance du sort
	 * !
	 */
	private final int id;

	/**
	 * The cost (in mana points) to cast the spell.
	 */
	private final int cost;

	public Spell(PowerRune powerRune, ElementRune elementRune, FormRune formRune, AlignmentRune alignmentRune) {
		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(elementRune, "The given element rune is null");
		Validate.notNull(formRune, "The given form rune is null");
		Validate.notNull(alignmentRune, "The given alignment rune is null");

		this.powerRune = powerRune;
		this.elementRune = elementRune;
		this.formRune = formRune;
		this.alignmentRune = alignmentRune;
		this.id = computeId(elementRune, formRune, alignmentRune);
		this.cost = computeCost(powerRune, elementRune, formRune, alignmentRune);
	}

	public Spell(PowerRune powerRune, ElementRune elementRune, FormRune formRune) {
		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(elementRune, "The given element rune is null");
		Validate.notNull(formRune, "The given form rune is null");

		this.powerRune = powerRune;
		this.elementRune = elementRune;
		this.formRune = formRune;
		this.alignmentRune = null; // No alignment rune
		this.id = computeId(elementRune, formRune, alignmentRune);
		this.cost = computeCost(powerRune, elementRune, formRune, alignmentRune);
	}

	public Spell(PowerRune powerRune, ElementRune elementRune) {
		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(elementRune, "The given element rune is null");

		this.powerRune = powerRune;
		this.elementRune = elementRune;
		this.formRune = null; // No form rune
		this.alignmentRune = null; // No alignment rune
		this.id = computeId(elementRune, formRune, alignmentRune);
		this.cost = computeCost(powerRune, elementRune, formRune, alignmentRune);
	}

	// The spell created by this constructor is necessarily valid
	public Spell(PowerRune powerRune, Spell.Type type) {
		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(type, "The given spell type is null");

		this.powerRune = powerRune;
		this.elementRune = type.getElementRune();
		this.formRune = type.getFormRune(); // Can be null
		this.alignmentRune = type.getAlignmentRune(); // Can be null
		this.id = computeId(elementRune, formRune, alignmentRune);
		this.cost = computeCost(powerRune, elementRune, formRune, alignmentRune);
	}

	/**
	 * Computes and returns an id from the given runes as an integer. This id
	 * uniquely identifies the spell type. Examples: 64 (FUL IR).
	 *
	 * @param elementRune
	 *            an element rune. Can't be null.
	 * @param formRune
	 *            a form rune. Can be null.
	 * @param alignmentRune
	 *            an alignment rune. Can be null if
	 * @return a positive integer identifying the spell.
	 */
	private int computeId(ElementRune elementRune, FormRune formRune, AlignmentRune alignmentRune) {
		// The form and alignment runes can be null
		Validate.notNull(elementRune, "The given element rune is null");

		int id = elementRune.getId();

		if (formRune != null) {
			id = (id * 10) + formRune.getId();

			if (alignmentRune != null) {
				id = (id * 10) + alignmentRune.getId();
			}
		}

		return id;
	}

	/**
	 * Computes and returns the number of mana points necessary for casting the
	 * spell with given runes.
	 *
	 * @param powerRune
	 *            a power rune. Can't be null.
	 * @param elementRune
	 *            an element rune. Can't be null.
	 * @param formRune
	 *            a form rune. Can be null.
	 * @param alignmentRune
	 *            an alignment rune. Must be null if the form rune is null.
	 * @return a positive integer representing a number of mana points for
	 *         casting the spell.
	 */
	private int computeCost(PowerRune powerRune, ElementRune elementRune, FormRune formRune, AlignmentRune alignmentRune) {
		// The form and alignment runes can be null
		Validate.notNull(powerRune, "The given power rune is null");
		Validate.notNull(elementRune, "The given element rune is null");

		int cost = powerRune.getCost() + elementRune.getCost(powerRune);

		if (formRune != null) {
			cost += formRune.getCost(powerRune);

			if (alignmentRune != null) {
				cost += alignmentRune.getCost(powerRune);
			}
		}

		return cost;
	}

	/**
	 * Returns the spell name from the invoked runes. Examples: "LO FUL",
	 * "MON FUL IR", etc.
	 *
	 * @return a string representing the spell's full name.
	 */
	public String getName() {
		final StringBuilder builder = new StringBuilder(32);
		builder.append(powerRune.name());
		builder.append(' ');
		builder.append(elementRune.name());

		if (formRune != null) {
			builder.append(' ');
			builder.append(formRune.name());

			if (alignmentRune != null) {
				builder.append(' ');
				builder.append(alignmentRune.name());
			}
		}

		return builder.toString();
	}

	/**
	 * Returns this spell's power rune.
	 *
	 * @return a power rune.
	 */
	public PowerRune getPower() {
		return powerRune;
	}

	/**
	 * Returns the type of this spell if the spell is valid or null if the spell
	 * isn't valid.
	 *
	 * @return the spell type or null if the spell isn't valid.
	 */
	public Type getType() {
		// Returns null if the spell isn't valid
		return Type.byValue(id);
	}

	/**
	 * Returns the skill used when casting this spell. This skill is necessary
	 * to determine if a champion can cast a spell because some spells require a
	 * minimal level in a given skill to be cast. When successfully cast, the
	 * champion will gain some experience points in this skill. Not all spells
	 * require a specific skill to be cast, in this case the method returns
	 * null.
	 *
	 * @return the skill to cast this spell or null if the spell isn't valid or
	 *         if it requires no specific skill.
	 */
	public Skill getSkill() {
		final Type spellType = getType();

		return (spellType != null) ? spellType.getSkill() : null;
	}

	/**
	 * Returns the number of runes used for casting this spell (including the
	 * power rune).
	 *
	 * @return a positive integer within [2,4] representing a number of runes.
	 */
	public int getRuneCount() {
		// The power and element runes are necessarily present
		int count = 2;

		if (formRune != null) {
			count++;

			if (alignmentRune != null) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Returns the runes used for casting this spell (including the power rune).
	 * The returned list will contain at least 2 runes (power + element) and up
	 * to 4 runes (power + element + form + alignment).
	 *
	 * @return a list of runes. Never returns null.
	 */
	public List<Rune> getRunes() {
		final List<Rune> runes = new ArrayList<Rune>(4);
		runes.add(powerRune);
		runes.add(elementRune);

		if (formRune != null) {
			runes.add(formRune);

			if (alignmentRune != null) {
				runes.add(alignmentRune);
			}
		}

		return runes;
	}

	/**
	 * Returns the cost (in number of mana points) to cast this spell as an
	 * integer. This method never returns 0 even for an invalid spell as mana is
	 * still consumed when casting an invalid spell.
	 *
	 * @return a positive integer representing the number of mana points
	 *         necessary for casting this spell.
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * Returns a (random) integer value representing the number of experience
	 * points gained by a champion after successfully casting this spell. The
	 * gained experience depends on the spell's difficulty (see
	 * {@link #getDifficulty()}).
	 *
	 * @return a positive integer representing a number of experience points or
	 *         0 if the spell isn't valid.
	 */
	public int getEarnedExperience() {
		if (isValid()) {
			// The spell is valid
			// Note: the difficuly for the ZO_KATH_RA spell is zero
			final int difficulty = getDifficulty();

			// TODO Refine the following formulas
			if (difficulty > 0) {
				return 5 + RandomUtils.nextInt(difficulty);
			}

			return 5;
		}

		// The spell isn't valid, no experience gained
		return 0;
	}

	/**
	 * Returns the difficulty to cast this spell as an integer. The difficulty
	 * depends on the power rune (the more powerful, the more difficult) and
	 * also on the spell's base difficulty.
	 *
	 * @return a positive integer or -1 if the spell isn't valid.
	 */
	public int getDifficulty() {
		final Type spellType = getType();

		if (spellType != null) {
			return powerRune.getDifficultyMultiplier() * spellType.getDifficulty();
		}

		// The spell isn't valid
		return -1;
	}

	/**
	 * Returns how long (in number of clock ticks) the champion must wait before
	 * attacking again.
	 *
	 * @return a positive integer representing a number of clock ticks or -1 if
	 *         the spell isn't valid.
	 */
	public int getDuration() {
		final Type spellType = getType();

		return (spellType != null) ? spellType.getDuration() : -1;
	}

	/**
	 * Tells whether the cast spell is valid.
	 *
	 * @return whether the cast spell is valid.
	 */
	public boolean isValid() {
		return Type.isValid(id);
	}

	/**
	 * Tells whether the casting champion is skilled enough to cast this spell.
	 * The outcome depends on the champion's level in the spell's required skill
	 * and also on the spell's inherent difficulty.
	 *
	 * @param champion
	 *            the champion casting the spell.
	 * @return whether the casting champion is skilled enough to cast this
	 *         spell.
	 */
	public boolean canBeCastBy(Champion champion) {
		Validate.notNull(champion, "The given champion is null");
		Validate.isTrue(isValid(), "The spell isn't valid");

		final Skill skill = getSkill();

		if (skill == null) {
			// No skill required to cast this spell
			return true;
		}

		final Champion.Level requiredLevel = getType().getRequiredLevel();
		final Champion.Level actualLevel = champion.getLevel(skill);

		return (actualLevel.compareTo(requiredLevel) >= 0);
	}

	/**
	 * Fait agir le {@link Spell} sur le {@link Champion} donn�.
	 *
	 * @param champion
	 *            un {@link Champion} sur lequel faire agir le sort.
	 * @throws EmptyFlaskNeededException
	 *             si le sort requiert une flasque vide pour r�ussir.
	 * @throws EmptyHandNeededException
	 *             si le sort demande que l'une des mains du {@link Champion}
	 *             soit vide.
	 */
	public void actUpon(Champion champion) throws EmptyFlaskNeededException, EmptyHandNeededException {
		Validate.notNull(champion, "The given champion is null");
		Validate.isTrue(isValid(), "The spell isn't valid");

		switch (getType()) {
		case ANTIDOTE_POTION:
		case DEXTERITY_POTION:
		case HEALTH_POTION:
		case MANA_POTION:
		case POISON_POTION:
		case SHIELD_POTION:
		case STAMINA_POTION:
		case STRENGTH_POTION:
		case VITALITY_POTION:
		case WISDOM_POTION: {
			// Search for an empty flask in the champion's hands
			final Item emptyFlask;
			final BodyPart hand;

			final Item item1 = champion.getBody().getWeaponHand().getItem();

			if ((item1 != null) && item1.getType().equals(Item.Type.EMPTY_FLASK)) {
				emptyFlask = item1;
				hand = champion.getBody().getWeaponHand();
			} else {
				final Item item2 = champion.getBody().getShieldHand().getItem();

				if ((item2 != null) && item2.getType().equals(Item.Type.EMPTY_FLASK)) {
					emptyFlask = item2;
					hand = champion.getBody().getShieldHand();
				} else {
					emptyFlask = null;
					hand = null;
				}
			}

			if (getType().requiresEmptyFlask() && (emptyFlask == null)) {
				// SHouldn't happen as this condition has been checked before
				throw new EmptyFlaskNeededException();
			}

			// Replace the empty flask by the created potion
			hand.putOn(new Potion(this));
			break;
		}
		case TORCH:
			// Strengthen the light generated by the champion
			champion.getSpells().getLight().inc(Utils.random(20, 30) * getPower().getPowerLevel());
			break;
		case ZO_KATH_RA:
			// One of the champion's hands must be empty
			final boolean shieldHandEmpty = champion.getBody().getShieldHand().isEmpty();
			final boolean weaponHandEmpty = champion.getBody().getWeaponHand().isEmpty();

			if (!shieldHandEmpty && !weaponHandEmpty) {
				// Both hands are full, the spell can't succeed
				throw new EmptyHandNeededException();
			}

			// Put a ZO_KATH_RA item in the champion's empty hand
			final Item zokathra = ItemFactory.getFactory().newItem(Item.Type.ZOKATHRA_SPELL);

			if (shieldHandEmpty) {
				champion.getBody().getShieldHand().putOn(zokathra);
			} else {
				champion.getBody().getWeaponHand().putOn(zokathra);
			}

			// FIXME Handle the empty hand use case: KICK, CRY, PUNCH actions !!
			break;
		case LIGHT:
			// Strengthen the light generated by the champion
			champion.getSpells().getLight().inc(Utils.random(20, 30) * getPower().getPowerLevel());
			break;
		case OPEN_DOOR: {
			// FIXME Refactor the way a projectile spell is handled
			final Projectile projectile = new SpellProjectile(this, champion);
			break;
		}
		case DARKNESS:
			// FIXME Implement actUpon(Champion)
			throw new UnsupportedOperationException("Unsupported spell <" + getType() + ">");
		case DISPELL_ILLUSION:
			champion.getParty().getSpells().getDispellIllusion().inc(powerRune.getPowerLevel() * Utils.random(10, 15));
			break;
		case ANTI_MAGIC:
			champion.getParty().getSpells().getAntiMagic().inc(powerRune.getPowerLevel() * Utils.random(10, 15));
			break;
		case FIREBALL: {
			// Create a fire ball projectile
			// FIXME Refactor the way a projectile spell is handled
			final Projectile projectile = new SpellProjectile(this, champion);
			break;
		}
		case INVISIBILITY:
			champion.getParty().getSpells().getInvisibility().inc(powerRune.getPowerLevel() * Utils.random(10, 15));
			break;
		case LIGHTNING_BOLT: {
			// FIXME Refactor the way a projectile spell is handled
			final Projectile projectile = new SpellProjectile(this, champion);
			break;
		}
		case MAGIC_FOOTPRINTS:
			// FIXME Implement actUpon(Champion)
			throw new UnsupportedOperationException("Unsupported spell <" + getType() + ">");
		case POISON_BOLT: {
			// FIXME Refactor the way a projectile spell is handled
			final Projectile projectile = new SpellProjectile(this, champion);
			break;
		}
		case POISON_CLOUD: {
			// FIXME Refactor the way a projectile spell is handled
			final Projectile projectile = new SpellProjectile(this, champion);
			break;
		}
		case SEE_THROUGH_WALLS:
			champion.getParty().getSpells().getSeeThroughWalls().inc(powerRune.getPowerLevel() * Utils.random(10, 15));
			break;
		case SHIELD:
			champion.getParty().getSpells().getShield().inc(powerRune.getPowerLevel() * Utils.random(10, 15));
			break;
		case WEAKEN_IMMATERIAL: {
			// FIXME Refactor the way a projectile spell is handled
			final Projectile projectile = new SpellProjectile(this, champion);
			break;
		}
		default:
			throw new UnsupportedOperationException("Unsupported spell <" + getType() + ">");
		}
	}
}