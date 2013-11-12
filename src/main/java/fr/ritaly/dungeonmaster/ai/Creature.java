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
package fr.ritaly.dungeonmaster.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.HasDirection;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.ai.astar.PathFinder;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Action;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.stat.Stat;

/**
 * A creature (or a monster).<br>
 * <br>
 * Source: <a href="http://dmweb.free.fr/?q=node/1363">Technical Documentation -
 * Dungeon Master and Chaos Strikes Back Creature Details</a>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Creature implements ChangeListener, ClockListener, HasDirection {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Enumerates the possible states of a {@link Creature}.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum State {
		/**
		 * State of an idle creature.
		 */
		IDLE,

		/**
		 * State of a creature patrolling a level.
		 */
		PATROLLING,

		/**
		 * State of a creature that detected a party and is tracking the
		 * champions to attack them.
		 */
		TRACKING,

		/**
		 * State of a creature attacking a party.
		 */
		ATTACKING;
		// DYING,
		// DEAD;
	}

	/**
	 * Defines the size of a {@link Creature} on the floor in terms of number of
	 * sectors occupied.<br>
	 * <br>
	 * Source: <a href="http://dmweb.free.fr/?q=node/1363">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Creature
	 * Details</a>
	 */
	public static enum Size {

		/**
		 * Size of a creature occupying one sector. There can be up to 4
		 * creatures per floor tile. Example: screamers.
		 */
		ONE,

		/**
		 * Size of a creature occupying two sectors. There can be up to 2
		 * creatures per floor tile. Example: worms.
		 */
		TWO,

		/**
		 * Size of a creature occupying four sectors. There can be only 1
		 * creature per floor tile. Example: dragons.
		 */
		FOUR;

		public int value() {
			switch (this) {
			case ONE:
				return 1;
			case TWO:
				return 2;
			case FOUR:
				return 4;
			default:
				throw new UnsupportedOperationException("Method unsupported for size " + this);
			}
		}
	}

	/**
	 * Enumerates the different types of {@link Creature}.
	 *
	 * @author francois_ritaly
	 */
	public static enum Type {
		MUMMY,
		SCREAMER,
		/** aka STONE_ROCK */
		ROCK_PILE,
		/** aka OGRE */
		TROLIN,
		/** aka WORM */
		MAGENTA_WORM,
		/** aka WASP */
		GIANT_WASP,
		GHOST,
		/** aka TENTACLE */
		SWAMP_SLIME,
		/** aka SNAKE */
		COUATL,
		/** aka EYE_BALL */
		WIZARD_EYE,
		SKELETON,
		STONE_GOLEM,
		GIGGLER,
		/** aka GIANT_RAT */
		PAIN_RAT,
		/** aka SORCERER */
		VEXIRK,
		RUSTER,
		/** aka SCORPION */
		GIANT_SCORPION,
		WATER_ELEMENTAL,
		/** aka KNIGHT or DEATH_KNIGHT */
		ANIMATED_ARMOR,
		/** aka SPIDER */
		OITU,
		/** aka MATERIALIZER */
		ZYTAZ,
		/** aka FIRE_ELEMENTAL */
		BLACK_FLAME,
		DEMON,
		/** aka DRAGON */
		RED_DRAGON,
		LORD_CHAOS,
		LORD_ORDER,
		GREY_LORD;

		private Type() {
		}

		CreatureDef getDefinition() {
			return CreatureDef.getDefinition(this);
		}

		public int getShield() {
			return getDefinition().getShield();
		}

		public int getExperienceMultiplier() {
			return getDefinition().getExperienceMultiplier();
		}

		public int getAttackAnimationDuration() {
			return getDefinition().getAttackAnimationDuration();
		}

		public int getAttackDuration() {
			return getDefinition().getAttackDuration();
		}

		public boolean isAbsorbsItems() {
			return getDefinition().isAbsorbsItems();
		}

		public boolean isImmuneToPoison() {
			return (15 == getPoisonResistance());
		}

		public boolean isImmuneToMagic() {
			return (15 == getAntiMagic());
		}

		public int getPoisonResistance() {
			return getDefinition().getPoisonResistance();
		}

		public int getAntiMagic() {
			return getDefinition().getAntiMagic();
		}

		public int getBravery() {
			return getDefinition().getBravery();
		}

		public Champion.Level getAttackSkill() {
			return getDefinition().getAttackSkill();
		}

		public int getAttackRange() {
			return getDefinition().getAttackRange();
		}

		/**
		 * Tells whether the creature is still (that is it can't move).
		 *
		 * @return whether the creature is still.
		 */
		public boolean isStill() {
			return (255 == getMoveDuration());
		}

		/**
		 * Tells whether the creature can move. Returns true for most creatures
		 * but {@link Type#WATER_ELEMENTAL} and {@link Type#BLACK_FLAME}.
		 *
		 * @return whether the creature can move.
		 */
		public boolean canMove() {
			return !isStill();
		}

		/**
		 * Tells whether the creature is invincible.
		 *
		 * @return whether the creature is invincible.
		 */
		public boolean isInvincible() {
			return (255 == getArmor());
		}

		/**
		 * Tells whether the creature levitates.
		 *
		 * @return whether the creature levitates.
		 */
		public boolean levitates() {
			return getDefinition().isLevitates();
		}

		public boolean isNightVision() {
			return getDefinition().isNightVision();
		}

		public boolean isArchenemy() {
			return getDefinition().isArchenemy();
		}

		public boolean isSeesInvisible() {
			return getDefinition().isSeesInvisible();
		}

		public boolean canStealItems() {
			// cf http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
			return equals(GIGGLER);
		}

		public boolean canTeleport() {
			// cf http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
			return equals(LORD_CHAOS);
		}

		public boolean canOnlyBeKilledWhenMaterialized() {
			// cf http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
			return equals(ZYTAZ);
		}

		public boolean hitByWeakenNonmaterialBeingsSpell() {
			// cf http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
			switch (this) {
			case MUMMY:
			case MAGENTA_WORM:
			case GIANT_WASP:
			case SWAMP_SLIME:
			case COUATL:
			case WIZARD_EYE:
			case SKELETON:
			case STONE_GOLEM:
			case VEXIRK:
			case RUSTER:
			case GIANT_SCORPION:
			case WATER_ELEMENTAL:
			case OITU:
			case BLACK_FLAME:
			case DEMON:
			case RED_DRAGON:
			case LORD_CHAOS:
			case LORD_ORDER:
			case GREY_LORD:
				return true;
			default:
				return false;
			}
		}

		public boolean hitByDisruptAttacks() {
			// cf http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
			switch (this) {
			case GHOST:
			case WATER_ELEMENTAL:
			case ZYTAZ:
			case BLACK_FLAME:
				return true;
			default:
				return false;
			}
		}

		Materiality getMateriality() {
			// If this bit is set to '1', the creature is non material. These
			// creatures ignore normal attacks but take damage from the
			// 'Disrupt' action of the Vorpal Blade. Fire damage is also
			// reduced by a half. All missiles except 'Weaken Non-material
			// Beings' pass through these creatures (this is hard coded).
			// These creatures can pass through all doors of any type.
			switch (this) {
			case BLACK_FLAME:
			case GHOST:
			case WATER_ELEMENTAL:
				return Materiality.IMMATERIAL;

			case ZYTAZ: // <--- Special case because the zytaz is both
				throw new UnsupportedOperationException("Method unsupported for type" + this);

			default:
				return Materiality.MATERIAL;
			}
		}

		/**
		 * Returns the spells the creature can cast. The returned list will
		 * contain attack and non-attack spells.
		 *
		 * @return a list of spell types. Never returns null.
		 */
		public Set<Spell.Type> getSpells() {
			return getDefinition().getSpells();
		}

		/**
		 * Returns the attack spells this creature can cast.
		 *
		 * @return a list of attack spells. Never returns null.
		 */
		public Set<Spell.Type> getAttackSpells() {
			final Set<Spell.Type> spells = getSpells();

			// Remove the possible non-attack spells (like OPEN_DOOR)
			for (final Iterator<Spell.Type> it = spells.iterator(); it.hasNext();) {
				final Spell.Type type = (Spell.Type) it.next();

				if (!type.isAttackSpell()) {
					it.remove();
				}
			}

			return spells;
		}

		public boolean canCastSpell() {
			return ! getSpells().isEmpty();
		}

		public Size getSize() {
			return getDefinition().getSize();
		}

		public Height getHeight() {
			return getDefinition().getHeight();
		}

		public int getArmor() {
			return getDefinition().getArmor();
		}

		public int computeDamagePoints(Champion champion, Item weapon, Action action) {
			Validate.notNull(champion, "The given champion is null");
			Validate.notNull(weapon, "The given weapon item is null");
			Validate.notNull(action, "The given action is null");

			// The damage points depends on:
			// 1) The weapon
			final int weaponDamage = weapon.getType().getDamage();

			// 2) The champion's strength
			final int strength = champion.getStats().getStrength().value();

			// 3) The creature's armor (or shield ?)
			final int vulnerability = 255 - getArmor();

			// 4) The action used for the attack
			final int actionDamage = action.getDamage();

			// FIXME Validate this formula
			return (weaponDamage + actionDamage) * vulnerability * strength;
		}

		public int getMoveDuration() {
			return getDefinition().getMoveDuration();
		}

		public int getBaseHealth() {
			return getDefinition().getBaseHealth();
		}

		/**
		 * Tells whether the attack of a {@link Creature} against a
		 * {@link Champion} succeeds.
		 *
		 * @return whether the attack of a {@link Creature} against a
		 *         {@link Champion} succeeds.
		 */
		public boolean hitsChampion() {
			return Utils.random(255) < getAttackProbability();
		}

		public int getAttackProbability() {
			return getDefinition().getAttackProbability();
		}

		public int getPoison() {
			return getDefinition().getPoison();
		}

		public int getAttackPower() {
			return getDefinition().getAttackPower();
		}

		public AttackType getAttackType() {
			return getDefinition().getAttackType();
		}

		public int getSightRange() {
			return getDefinition().getSightRange();
		}

		public int getAwareness() {
			return getDefinition().getAwareness();
		}

		public Set<Weakness> getWeaknesses() {
			return getDefinition().getWeaknesses();
		}

		public boolean isHurtByWeapon(Item weapon) {
			Validate.notNull(weapon, "The given weapon item is null");

			final Set<Weakness> weaknesses = getWeaknesses();

			if (weaknesses.isEmpty()) {
				return false;
			}
			for (Weakness weakness : weaknesses) {
				if (weakness.acceptsWeapon(weapon)) {
					return true;
				}
			}

			return false;
		}

		public boolean isHurtBySpell(Spell.Type spellType) {
			Validate.notNull(spellType, "The given spell type is null");

			final Set<Weakness> weaknesses = getWeaknesses();

			if (weaknesses.isEmpty()) {
				return false;
			}
			for (Weakness weakness : weaknesses) {
				if (weakness.acceptsSpell(spellType)) {
					return true;
				}
			}

			return false;
		}

		public boolean isHurtByPoisonCloud() {
			final Set<Weakness> weaknesses = getWeaknesses();

			// Optimisation
			return weaknesses.contains(Weakness.POISON_CLOUD);
		}

		/**
		 * Indique si la {@link Creature} peut attaquer m�me si elle ne fait pas
		 * face aux {@link Champion}s.
		 *
		 * @return si la {@link Creature} peut attaquer m�me si elle ne fait pas
		 *         face aux {@link Champion}s.
		 */
		public boolean isSideAttackAllowed() {
			return getDefinition().isSideAttack();
		}

		/**
		 * Indique si la {@link Creature} pr�f�re rester en arri�re-plan quand
		 * d'autres {@link Creature}s attaquent les {@link Champion}s.
		 *
		 * @return si la {@link Creature} pr�f�re rester en arri�re-plan quand
		 *         d'autres {@link Creature}s attaquent les {@link Champion}s.
		 */
		public boolean prefersBackRow() {
			// The creature will tend to stay in the back row while other
			// creatures will step up to the front row when the party is near
			// and they want to attack
			switch (this) {
			case SCREAMER:
			case VEXIRK:
			case WATER_ELEMENTAL:
			case LORD_CHAOS:
			case RED_DRAGON:
			case LORD_ORDER:
			case GREY_LORD:
				return true;

			default:
				return false;
			}
		}

		/**
		 * Indique si la {@link Creature} peut attaquer n'importe quel
		 * {@link Champion} du groupe, en particulier ceux situ�s derri�re dans
		 * le groupe.
		 *
		 * @return si la {@link Creature} peut attaquer n'importe quel
		 *         {@link Champion} du groupe, en particulier ceux situ�s
		 *         derri�re dans le groupe.
		 */
		public boolean canAttackAnyChampion() {
			// If this bit is set to '1', the creature can attack any champion
			// in the party, even the ones in the back. If both 'Prefer back
			// row' and 'Attack any champion' flags are set to '0', the
			// creature will move to the front row of its tile. In other cases
			// the creature has a 25% chance of moving to the front row
			switch (this) {
			case GIGGLER:
			case WIZARD_EYE:
			case VEXIRK:
			case WATER_ELEMENTAL:
				return true;
			default:
				return false;
			}
		}
	}

	/**
	 * Enumerates the possible creature heights. Mainly used to determine when a
	 * closing door hits the head of a creature under the door.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Height {
		// The values must be sorted from lowest to highest !
		UNDEFINED,
		// FIXME JUNIT: Fireballs fly over the head of small creatures !
		SMALL,
		MEDIUM,
		GIANT;
	}

	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	private final int id = SEQUENCE.incrementAndGet();

	/**
	 * The creature's type.
	 */
	private final Type type;

	// TODO The creature's health regenerates over time
	private final Stat health;

	/**
	 * The possible items thrown at the creature and absorbed.
	 */
	private final List<Item> absorbedItems = new ArrayList<Item>();

	/**
	 * The materializer managing how the creature materializes.
	 */
	private final Materializer materializer;

	/**
	 * The element where the creature is currently at. Can be null if the
	 * creature isn't inside a dungeon.
	 */
	private Element element;

	/**
	 * The creature's look direction. Can't be null.
	 */
	private Direction direction = Direction.NORTH;

	/**
	 * The creature's current state. Must be accessed via {@link #getState()}
	 * and {@link #setState(State)} to ensure synchronization.
	 */
	private State state = State.IDLE;

	/**
	 * The timer managing the creature moves. The creature can move when the
	 * timer reaches zero.
	 */
	private final AtomicInteger moveTimer = new AtomicInteger();

	/**
	 * The timer managing the creature attacks. The creature can attack when the
	 * timer reaches zero.
	 */
	private final AtomicInteger attackTimer = new AtomicInteger();

	// The parameter 'multiplier' can denote a health multiplier or a
	// "level experience multiplier"
	public Creature(Type type, int multiplier, Direction direction) {
		Validate.notNull(type, "The given creature type is null");
		Validate.isTrue(multiplier > 0, String.format("The given multiplier %d must be positive", multiplier));
		Validate.notNull(direction, "The given direction is null");

		this.type = type;
		this.direction = direction;

		// Formula excerpted from "Technical Documentation - Dungeon Master and
		// Chaos Strikes Back Creature Generators"
		final int healthPoints = (multiplier * getType().getBaseHealth()) + Utils.random(getType().getBaseHealth() / 4);

		this.health = new Stat(getId(), "Health", healthPoints, healthPoints);
		this.health.addChangeListener(this);

		if (Type.ZYTAZ.equals(getType())) {
			// Special use case for the zytaz
			this.materializer = new RandomMaterializer(this);
		} else {
			this.materializer = new StaticMaterializer(getType().getMateriality() == Materiality.MATERIAL);
		}

		this.moveTimer.set(getType().getMoveDuration());

		Clock.getInstance().register(this);
	}

	public Creature(Type type, int multiplier) {
		this(type, multiplier, Direction.NORTH);
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		if (this.direction != direction) {
			final Direction backup = this.direction;

			this.direction = direction;

			if (log.isDebugEnabled()) {
				log.debug(this + ".Direction: " + backup + " -> " + this.direction);
			}
		}
	}

	public final boolean isMaterial() {
		return materializer.isMaterial();
	}

	public final boolean isImmaterial() {
		return materializer.isImmaterial();
	}

	public final Height getHeight() {
		return getType().getHeight();
	}

	public final Type getType() {
		return type;
	}

	public final Size getSize() {
		return getType().getSize();
	}

	/**
	 * Tells whether the creature can take the stairs. Most creatures can't use
	 * stairs and can't stalk champions fleeing to another level.
	 *
	 * @return whether the creature can take the stairs.
	 */
	public final boolean canTakeStairs() {
		// TODO Confirm the list below
		switch (getType()) {
		case ZYTAZ:
		case GHOST:
		case WIZARD_EYE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tells whether the creature can attack the champions even if not looking
	 * towards them.
	 *
	 * @return whether the creature can attack the champions even if not looking
	 *         towards them.
	 */
	public final boolean isSideAttackAllowed() {
		return getType().isSideAttackAllowed();
	}

	/**
	 * Tells whether the creature prefers staying away from the party on its
	 * "back row".
	 *
	 * @return whether the creature prefers staying away from the party on its
	 *         "back row".
	 */
	public final boolean prefersBackRow() {
		return getType().prefersBackRow();
	}

	/**
	 * Tells whether the creature can attack any champion in the party or just
	 * those located on the first line. Most creatures can't attack the
	 * champions on the rear line.
	 *
	 * @return whether the creature can attack any champion in the party or just
	 *         those located on the first line.
	 */
	public final boolean canAttackAnyChampion() {
		return getType().canAttackAnyChampion();
	}

	/**
	 * Tells whether the creature absorbs the items thrown at it. The items
	 * absorbed by a creature are dropped when the creature dies.
	 *
	 * @return whether the creature absorbs the items thrown at it.
	 */
	public final boolean isAbsorbItems() {
		// TODO: JUNIT: Write a unit test to test this behavior
		return getType().isAbsorbsItems();
	}

	/**
	 * Tells whether the creature can see the champions when the "Invisibility"
	 * spell is active.
	 *
	 * @return whether the creature can see the champions when the
	 *         "Invisibility" spell is active.
	 */
	public final boolean isSeesInvisible() {
		// TODO JUNIT: Write a unit test to test this behavior
		return getType().isSeesInvisible();
	}

	/**
	 * Tells whether the creature can see in the darkness.
	 *
	 * @return whether the creature can see in the darkness.
	 */
	public final boolean isNightVision() {
		// TODO JUNIT: Write a unit test to test this behavior
		// TODO What's the detailed spec ? How does it related to the sight range feature ?
		return getType().isNightVision();
	}

	public final boolean isArchenemy() {
		// TODO JUNIT: Write a unit test to ensure an archenemy can move to a flux cage
		return getType().isArchenemy();
	}

	/**
	 * Returns how long it takes to the creature to move from one position to a
	 * neighbour position as a number of clock ticks.
	 *
	 * @return a positive integer representing a number of clock ticks.
	 */
	public final int getMoveDuration() {
		return getType().getMoveDuration();
	}

	/**
	 * Returns the creature's armor bonus.
	 *
	 * @return an integer representing the creature's armor bonus within
	 *         [0,255].
	 */
	public final int getArmor() {
		return getType().getArmor();
	}

	public final int getAttackPower() {
		return getType().getAttackPower();
	}

	public final int getPoison() {
		return getType().getPoison();
	}

	public final int getSightRange() {
		return getType().getSightRange();
	}

	public final int getSpellRange() {
		return getType().getAttackRange();
	}

	public final int getBravery() {
		return getType().getBravery();
	}

	// FIXME public abstract boolean isSuicidal();

	public final int getPoisonResistance() {
		return getType().getPoisonResistance();
	}

	public final boolean isAlive() {
		return health.value() > 0;
	}

	public final boolean isDead() {
		return !isAlive();
	}

	public int getHealth() {
		return health.value();
	}

	public int hit(AttackType attackType) {
		Validate.notNull(attackType, "The given attack type is null");

		if (getType().isInvincible()) {
			// The creature can't be hurt
			return 0;
		}

		final int backup = health.value();
		final int damage;

		// FIXME Refine the lower and upper bounds for the damage points below
		switch (attackType) {
		case CRITICAL:
			damage = Utils.random(1, 5) * 3;
			break;

		case FIRE:
		case NONE:
		case NORMAL:
		case PSYCHIC:
		case SHARP:
			damage = Utils.random(1, 5);
			break;

		case MAGIC:
			if (isImmuneToMagic()) {
				// The creature is immune to magic attack
				return 0;
			}

			damage = Utils.random(1, 5);
			break;

		default:
			throw new UnsupportedOperationException("Unsupported attack type " + attackType);
		}

		this.health.dec(damage);

		// The difference could be lesser than the damage variable if the
		// creature just died (the health can't be negative)
		return backup - health.value();
	}

	/**
	 * Returns the sound played when the creature attacks.
	 *
	 * @return an audio clip corresponding to the sound played when the creature
	 *         attacks.
	 */
	public AudioClip getSound() {
		// FIXME Implement method Creature.getSound()
		throw new UnsupportedOperationException("Method not yet implemented");
	}

	/**
	 * Returns the type of attack used by this creature.
	 *
	 * @return the attack type as an instance of {@link AttackType}. Never
	 *         returns null.
	 */
	public final AttackType getAttackType() {
		return getType().getAttackType();
	}

	public final int getAwareness() {
		return getType().getAwareness();
	}

	public final Champion.Level getAttackSkill() {
		return getType().getAttackSkill();
	}

	public final boolean canTeleport() {
		return getType().canTeleport();
	}

	public final int getAntiMagic() {
		return getType().getAntiMagic();
	}

	public final String getId() {
		return String.format("%s[%d]", type.name(), id);
	}

	public final boolean isImmuneToPoison() {
		return getType().isImmuneToPoison();
	}

	public final boolean isInvincible() {
		return getType().isInvincible();
	}

	public final boolean canMove() {
		return getType().canMove();
	}

	public final boolean isImmuneToMagic() {
		return getType().isImmuneToMagic();
	}

	public final boolean canStealItems() {
		return getType().canStealItems();
	}

	public final boolean canOnlyBeKilledWhenMaterialized() {
		return getType().canOnlyBeKilledWhenMaterialized();
	}

	public final Set<Spell.Type> getSpells() {
		return getType().getSpells();
	}

	/**
	 * Tells whether the creature absorbs the given item.
	 *
	 * @param item
	 *            an item to absorb. Can't be null.
	 * @return whether the creature absorbs the given item.
	 */
	public boolean absorbItem(Item item) {
		Validate.notNull(item, "The given item is null");

		if (isAbsorbItems()) {
			absorbedItems.add(item);

			return true;
		}

		return false;
	}

	/**
	 * Returns the items currently carried by the creature. The returned list
	 * will contained the creature's "own" items and (if relevant) the items it
	 * previously absorbed.
	 *
	 * @return a list of items. Never returns null.
	 */
	public final List<Item> getItems() {
		final List<Item> items = new ArrayList<Item>();

		// The creature's own items
		items.addAll(getType().getDefinition().getItems());

		// ... and the possible absorbed items
		items.addAll(absorbedItems);

		return items;
	}

	public final Set<Weakness> getWeaknesses() {
		return getType().getWeaknesses();
	}

	public final int getAttackDuration() {
		return getType().getAttackDuration();
	}

	public final int getAttackDisplayDuration() {
		return getType().getAttackAnimationDuration();
	}

	public final int getExperienceMultiplier() {
		return getType().getExperienceMultiplier();
	}

	public final int getShield() {
		return getType().getShield();
	}

	@Override
	public void onChangeEvent(ChangeEvent event) {
		if (event.getSource() == health) {
			if (health.value() == 0) {
				// The creature just died
				if (log.isDebugEnabled()) {
					log.debug(this + " just died");
				}

				// Are there items to drop ?
				final List<Item> list = getItems();

				if (!list.isEmpty()) {
					// FIXME The creature drops some items (own items + absorbed items if relevant)
				}

				this.health.removeChangeListener(this);
			}
		}
	}

	@Override
	public String toString() {
		return getId();
	}

	/**
	 * Tells whether the creature can see the given position.
	 *
	 * @param targetPosition
	 *            the position to see. Can't be null.
	 * @return whether the creature can see the given position.
	 */
	public boolean canSeePosition(Position targetPosition) {
		Validate.notNull(targetPosition, "The given position is null");

		if (getElement() == null) {
			// The creature isn't event inside a dungeon
			return false;
		}

		final Position currentPosition = getElement().getPosition();

		// Optimization: Ensure the current and target position are on the same
		// level
		if (targetPosition.z != currentPosition.z) {
			return false;
		}

		// FIXME Consider the transparency of doors and the possible elements between the 2 positions

		// What are the positions visible from the creature ?
		final List<Position> visiblePositions = currentPosition.getVisiblePositions(direction);

		// Convert the positions into elements
		final List<Element> visibleElements = getElement().getLevel().getElements(visiblePositions);

		for (Element element : visibleElements) {
			if (targetPosition.equals(element.getPosition())) {
				// The creature can see the position
				return true;
			}
		}

		// The creature can't see the position
		return false;
	}

	/**
	 * Tells whether the creature can hear a sound emitted from the given
	 * position.
	 *
	 * @param targetPosition
	 *            the position to hear. Can't be null.
	 * @return whether the creature can hear a sound emitted from the given
	 *         position.
	 */
	public boolean canHearPosition(Position targetPosition) {
		Validate.notNull(targetPosition, "The given position is null");

		if (getElement() == null) {
			// The creature isn't event inside a dungeon
			return false;
		}

		final Position currentPosition = getElement().getPosition();

		// Optimization: Ensure the current and target position are on the same
		// level
		if (targetPosition.z != currentPosition.z) {
			return false;
		}

		// FIXME Take into account the possible obstacles between the 2 positions !!

		// What are the positions that the creature can hear ? There are within
		// a range defined by the creature's awareness
		final List<Position> audiblePositions = currentPosition.getSurroundingPositions(getType().getAwareness());

		// Convert the positions into elements
		final List<Element> audibleElements = getElement().getLevel().getElements(audiblePositions);

		for (Element element : audibleElements) {
			if (targetPosition.equals(element.getPosition())) {
				// The position can be heard
				return true;
			}
		}

		// The creature can't hear a sound from this position
		return false;
	}

	/**
	 * Tells whether the creature can (directly) attack the given position.
	 *
	 * @param targetPosition
	 *            the target position to attack. Can't be null.
	 * @return whether the creature can (directly) attack the given position.
	 */
	public boolean canAttackPosition(Position targetPosition) {
		Validate.notNull(targetPosition, "The given position is null");

		if (getElement() == null) {
			// The creature isn't event inside a dungeon
			return false;
		}

		final Position currentPosition = getElement().getPosition();

		// Optimization: Ensure the current and target position are on the same
		// level
		if (targetPosition.z != currentPosition.z) {
			return false;
		}

		// FIXME Take into account the possible obstacles between the 2 positions !!

		final List<Position> attackablePositions;

		// Can the creature perform a remote attack (with an attack spell) ?
		if (!getType().getAttackSpells().isEmpty()) {
			// Yes. What are the positions attackable with a spall within the
			// (spell) range ?
			attackablePositions = currentPosition.getAttackablePositions(getSpellRange());
		} else {
			// No, the creature has to adjoin the party to perform a direct
			// attack
			attackablePositions = currentPosition.getAttackablePositions();
		}

		return attackablePositions.contains(targetPosition);
	}

	private boolean isMoveAllowed() {
		return (moveTimer.get() == 0);
	}

	private boolean isAttackAllowed() {
		return (attackTimer.get() == 0);
	}

	private void resetMoveTimer() {
		moveTimer.set(getType().getMoveDuration());
	}

	private void resetAttackTimer() {
		attackTimer.set(getType().getAttackDuration());
	}

	@Override
	public boolean clockTicked() {
		// Make the ZYTAZ "blink"
		this.materializer.clockTicked();

		// FIXME Handle creatures with a size of 1 or 2
		if (!Size.FOUR.equals(getSize())) {
			log.warn("Method Creature.clockTicked() doesn't support creatures whose size is " + getSize() + " (for the moment)");

			return true;
		}

		// TODO Is there a relationship between the move speed and the size of a creature ? For instance, does a dragon (size 4) moves twice faster than a worm (size 2) ?

		// Update the move and attack timers
		if (moveTimer.get() > 0) {
			moveTimer.decrementAndGet();
		}
		if (attackTimer.get() > 0) {
			attackTimer.decrementAndGet();
		}

		if (getElement() == null) {
			// Necessary for the unit tests
			return true;
		}

		final Party party = getElement().getLevel().getDungeon().getParty();

		if (isAttackAllowed()) {
			if ((party != null) && canAttackPosition(party.getPosition())) {
				// The creature is near a party but it's not looking towards it.
				// It has to turn before attacking
				final Direction directionTowardsParty = getElement().getPosition().getDirectionTowards(party.getPosition());

				if (directionTowardsParty != null) {
					// Direction identified
					if (!getDirection().equals(directionTowardsParty)) {
						// Turn the creature towards the party
						setDirection(directionTowardsParty);
					}
				}

				// Attack the party nearby
				attackParty(party);

				return true;
			}
		}

		if (isMoveAllowed()) {
			// FIXME Take into account the ambient light and whether the party is invisible to determine whether the detection succeeds

			if ((party != null) && (canSeePosition(party.getPosition()) || canHearPosition(party.getPosition()))) {

				// The creature detects (sees / hears) the party and stalks it
				if (moveTo(party.getPosition().x, party.getPosition().y)) {
					// If the creature can attack in the same turn, do it
					if (isAttackAllowed() && canAttackPosition(party.getPosition())) {
						// The creature is near a party but it's not looking towards it.
						// It has to turn before attacking
						final Direction directionTowardsParty = getElement().getPosition().getDirectionTowards(
								party.getPosition());

						if (directionTowardsParty != null) {
							// Direction identified
							if (!getDirection().equals(directionTowardsParty)) {
								// Turn the creature towards the party
								setDirection(directionTowardsParty);
							}
						}

						attackParty(party);
					}

					// The move can't succeed
					return true;
				}
			}

			// No party to attack, the creature wanders
			patrol();
		}

		// TODO Animate the creature
		return true;
	}

	private void attackParty(Party party) {
		// FIXME Implement method attackParty(Party)

		// The creature can't attack for a number of clock ticks
		resetAttackTimer();

		// Transition vers l'�tat ATTACKING
		setState(State.ATTACKING);
	}

	private boolean moveTo(int x, int y) {
		if (!getType().canMove()) {
			// The creature can't move
			return false;
		}

		final Element element = getElement();

		if (element == null) {
			// The creature isn't inside a dungeon
			return false;
		}

		// The creature's start position
		final Position startPosition = element.getPosition();

		// Find a path to reach the given target position
		final PathFinder pathFinder = new PathFinder(element.getLevel(), isMaterial() ? Materiality.MATERIAL : Materiality.IMMATERIAL);
		final List<Element> path = pathFinder.findBestPath(x, y, startPosition.x, startPosition.y);

		if (path == null) {
			// Unable to reach the target position, return
			return false;
		}

		if (log.isDebugEnabled()) {
			log.debug("Found path: " + path);
		}

		// Move to the next position (the second element in the returned path)
		final Element node = path.get(1);

		// The creature moves and changes its direction to reach the target
		// position
		final Direction directionTowardsTarget = getElement().getPosition().getDirectionTowards(
				new Position(node.getPosition().x, node.getPosition().y, startPosition.z));

		// The creature leaves the current position
		element.removeCreature(this);

		if (directionTowardsTarget != null) {
			if (!getDirection().equals(directionTowardsTarget)) {
				// Change the creature's direction and turn towards the target
				setDirection(directionTowardsTarget);
			}
		}

		final Element targetElement = element.getLevel().getElement(node.getPosition().x, node.getPosition().y);

		// The creature arrives on the target position
		targetElement.addCreature(this);

		// The creature can't move for a given number of clock ticks
		resetMoveTimer();

		// Switch to the TRACKING state
		setState(State.TRACKING);

		return true;
	}

	private void patrol() {
		if (!getType().canMove()) {
			// The creature can't move
			return;
		}

		// The creature can move. Where will it go ?

		// What are the candidate targets ?
		final List<Element> surroundingElements = getElement().getSurroundingElements();

		// Filter out the position already occupied
		for (Iterator<Element> it = surroundingElements.iterator(); it.hasNext();) {
			final Element element = it.next();

			if (!element.isTraversable(this)) {
				// The creature can't traverse this position, skip it
				it.remove();

				continue;
			}

			if (element.hasParty()) {
				// There are champions on this position, skip it
				it.remove();

				continue;
			}

			if (!element.canHost(this)) {
				// There's not enough room left on this element, skip it
				it.remove();

				continue;
			}

			if (Element.Type.STAIRS.equals(element.getType())) {
				if (!canTakeStairs()) {
					// This creature can't use stairs, skip this element
					it.remove();

					continue;
				}
			} else if (Element.Type.TELEPORTER.equals(element.getType())) {
				if (!canTeleport()) {
					// This creature can't use teleports, skip this element
					it.remove();

					continue;
				}
			} else if (Element.Type.PIT.equals(element.getType())) {
				// It's a pit, can the creature jump into it (if open) ?
				// FIXME Implement this use case
				throw new UnsupportedOperationException("Use case not yet implemented");
			}
		}

		if (surroundingElements.isEmpty()) {
			// The creature can't move as there are no more candidate positions left
			// FIXME Teleport the creature ?
			return;
		}

		// FIXME Randomly change the creature's direction. Prefer the direction pointing towards the party
		// FIXME Can the creature physically move to the identified target ? It could be blocked by another creature in front

		if (State.IDLE.equals(getState())) {
			// Switch to the PATROLLING state
			setState(State.PATROLLING);
		}

		// Toss a random position
		Collections.shuffle(surroundingElements);

		final Element startElement = getElement();
		final Element endElement = surroundingElements.iterator().next();

		// Identify the direction when moving from the start to the end element
		final Direction directionTowardsTarget = getElement().getPosition().getDirectionTowards(endElement.getPosition());

		// The creature leaves the current position (event fired)
		startElement.removeCreature(this);

		if (!getDirection().equals(directionTowardsTarget)) {
			// Change the creature's direction consistently with the move
			setDirection(directionTowardsTarget);
		}

		// The creature arrives on the end position
		endElement.addCreature(this);

		// The creature can't move for a given number of clock ticks
		resetMoveTimer();
	}

	public synchronized State getState() {
		return state;
	}

	private synchronized void setState(State state) {
		Validate.notNull(state, "The given state is null");

		if (this.state != state) {
			final State backup = this.state;

			this.state = state;

			if (log.isDebugEnabled()) {
				log.debug(this + ".State: " + backup + " -> " + this.state);
			}
		}
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		// The element can be null

		if (!ObjectUtils.equals(this.element, element)) {
			final Element backup = this.element;

			this.element = element;

			if (log.isDebugEnabled()) {
				log.debug(this + ".Element: " + backup + " -> " + this.element);
			}
		}
	}
}