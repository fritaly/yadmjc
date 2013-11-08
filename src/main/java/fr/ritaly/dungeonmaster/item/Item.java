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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.event.DirectionChangeListener;
import fr.ritaly.dungeonmaster.item.ItemDef.ActionDef;
import fr.ritaly.dungeonmaster.item.ItemDef.Effect;
import fr.ritaly.dungeonmaster.magic.PowerRune;

/**
 * An item. There are different types of items in the game.<br>
 * <br>
 * Sources: <a href="http://dmweb.free.fr/?q=node/886">Technical Documentation -
 * Dungeon Master and Chaos Strikes Back Items properties</a>, <a
 * href="http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244"
 * >Nerthring's Dungeon Master Guide</a>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public abstract class Item implements ChangeEventSource {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Sequence used for assigning unique ids to items upon creation.
	 */
	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	/**
	 * TODO Translate this javadoc comment to english
	 *
	 * Enum�ration des types d'objets. ATTENTION !! L'ordre des �num�rations est
	 * importante car il d�termine la catégorie de chaque type d'objet.
	 * Le nombre d'objets dans chaque catégorie n'est pas le m�me que
	 * dans la sp�cification de dmweb.free.fr car certains objets partagent les
	 * m�mes caract�ristiques mais pas les m�mes images, du coup il y a deux
	 * valeurs distinctes d'enum�ration pour ces objets-l�.<br>
	 * <br>
	 * Source: <a href="http://dmweb.free.fr/?q=node/886">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Items
	 * properties</a>
	 */
	public static enum Type {

		// --- Potions --- //

		MON_POTION,
		UM_POTION,
		DES_POTION,
		POISON_POTION,
		SAR_POTION,
		ZO_POTION,
		DEXTERITY_POTION,
		STRENGTH_POTION,
		WISDOM_POTION,
		VITALITY_POTION,
		ANTIDOTE_POTION,
		STAMINA_POTION,
		ANTI_MAGIC_POTION,
		MANA_POTION,
		HEALTH_POTION,
		WATER_FLASK,
		KATH_BOMB,
		PEW_BOMB,
		RA_BOMB,
		FUL_BOMB,

		// --- Weapons --- //

		EYE_OF_TIME,
		STORMRING,
		TORCH,
		FLAMITT,
		STAFF_OF_CLAWS,
		BOLT_BLADE,
		STORM,
		FURY,
		RA_BLADE,
		THE_FIRESTAFF,
		DAGGER,
		FALCHION,
		SWORD,
		RAPIER,
		SABRE,
		BITER,
		SAMURAI_SWORD,
		DELTA,
		SIDE_SPLITTER,
		DIAMOND_EDGE,
		VORPAL_BLADE,
		THE_INQUISITOR,
		DRAGON_FANG,
		AXE,
		HARD_CLEAVE,
		EXECUTIONER,
		MACE,
		MACE_OF_ORDER,
		MORNING_STAR,
		CLUB,
		STONE_CLUB,
		BOW,
		CLAW_BOW,
		CROSSBOW,
		ARROW,
		SLAYER,
		SLING,
		ROCK,
		POISON_DART,
		THROWING_STAR,
		STICK,
		STAFF,
		WAND,
		TEOWAND,
		YEW_STAFF,
		STAFF_OF_MANAR,
		STAFF_OF_IRRA,
		SNAKE_STAFF,
		CROSS_OF_NETA,
		THE_CONDUIT,
		SERPENT_STAFF,
		DRAGON_SPIT,
		SCEPTRE_OF_LYT,
		HORN_OF_FEAR,
		SPEED_BOW,
		THE_FIRESTAFF_COMPLETE,

		// --- Clothes --- //

		CAPE,
		CLOAK_OF_NIGHT,
		BARBARIAN_HIDE,
		TATTERED_PANTS,
		SANDALS,
		LEATHER_BOOTS,
		ROBE_BODY,
		TATTERED_SHIRT,
		ROBE_LEGS,
		FINE_ROBE_BODY,
		FINE_ROBE_LEGS,
		KIRTLE,
		SILK_SHIRT,
		TABARD,
		GUNNA,
		ELVEN_DOUBLET,
		ELVEN_HUKE,
		ELVEN_BOOTS,
		LEATHER_JERKIN,
		LEATHER_PANTS,
		SUEDE_BOOTS,
		BLUE_PANTS,
		TUNIC,
		GHI,
		GHI_TROUSERS,
		CALISTA,
		CROWN_OF_NERRA,
		BEZERKER_HELM,
		HELMET,
		BASINET,
		BUCKLER,
		NETA_SHIELD,
		HIDE_SHIELD,
		CRYSTAL_SHIELD,
		WOODEN_SHIELD,
		SMALL_SHIELD,
		MAIL_AKETON,
		LEG_MAIL,
		MITHRAL_AKETON,
		MITHRAL_MAIL,
		CASQUE_N_COIF,
		HOSEN,
		ARMET,
		TORSO_PLATE,
		LEG_PLATE,
		FOOT_PLATE,
		LARGE_SHIELD,
		SAR_SHIELD,
		HELM_OF_LYTE,
		HELM_OF_RA,
		PLATE_OF_LYTE,
		PLATE_OF_RA,
		POLEYN_OF_LYTE,
		POLEYN_OF_RA,
		GREAVE_OF_LYTE,
		GREAVE_OF_RA,
		SHIELD_OF_LYTE,
		SHIELD_OF_RA,
		HELM_OF_DARC,
		DRAGON_HELM,
		PLATE_OF_DARC,
		DRAGON_PLATE,
		POLEYN_OF_DARC,
		DRAGON_POLEYN,
		GREAVE_OF_DARC,
		DRAGON_GREAVE,
		SHIELD_OF_DARC,
		DRAGON_SHIELD,
		DEXHELM,
		FLAMEBAIN,
		POWERTOWERS,
		BOOTS_OF_SPEED,
		HALTER,

		// --- Miscellaneous --- //

		EMPTY_FLASK,
		CHEST,
		SCROLL,
		COMPASS,
		WATER_SKIN,
		JEWEL_SYMAL,
		ILLUMULET,
		ASHES,
		BONES,
		COPPER_COIN,
		SAR_COIN,
		SILVER_COIN,
		GOLD_COIN,
		GOR_COIN,
		IRON_KEY,
		KEY_OF_B,
		SOLID_KEY,
		SQUARE_KEY,
		TOURQUOISE_KEY,
		CROSS_KEY,
		ONYX_KEY,
		SKELETON_KEY,
		GOLD_KEY,
		WINGED_KEY,
		TOPAZ_KEY,
		SAPPHIRE_KEY,
		EMERALD_KEY,
		RUBY_KEY,
		RA_KEY,
		MASTER_KEY,
		BOULDER,
		BLUE_GEM,
		ORANGE_GEM,
		GREEN_GEM,
		APPLE,
		CORN,
		BREAD,
		CHEESE,
		SCREAMER_SLICE,
		WORM_ROUND,
		DRUMSTICK,
		SHANK,
		DRAGON_STEAK,
		GEM_OF_AGES,
		EKKHARD_CROSS,
		MOONSTONE,
		THE_HELLION,
		PENDANT_FERAL,
		MAGICAL_BOX_BLUE,
		MAGICAL_BOX_GREEN,
		MIRROR_OF_DAWN,
		ROPE,
		RABBIT_FOOT,
		CORBAMITE,
		CORBUM,
		CHOKER,
		LOCK_PICKS,
		MAGNIFIER,
		ZOKATHRA_SPELL;

		// FIXME Take into account distance, shootDamage, deltaEnergy

		private Type() {
		}

		private ItemDef getDefinition() {
			return ItemDef.getDefinition(this);
		}

		/**
		 * Returns the actions associated to this item.
		 *
		 * @return a list of actions. Never returns null.
		 */
		public List<ItemDef.ActionDef> getActions() {
			return getDefinition().getActions();
		}

		/**
		 * Returns the carry locations where this item can be stored.
		 *
		 * @return a set of carry locations. Never returns null.
		 */
		public Set<CarryLocation> getCarryLocations() {
			return getDefinition().getCarryLocations();
		}

		/**
		 * Returns the item's weight as a float.
		 *
		 * @return a float representing a weight in Kg.
		 */
		public float getWeight() {
			return getDefinition().getWeight();
		}

		/**
		 * Returns the effects bestowed by this item to a champion when activated.
		 *
		 * @return a list of effects. Never returns null.
		 */
		public List<Effect> getEffects() {
			return getDefinition().getEffects();
		}

		public int getDamage() {
			return getDefinition().getDamage();
		}

		/**
		 * Tells whether this item is a key.
		 *
		 * @return whether this item is a key.
		 */
		public boolean isKey() {
			switch (this) {
			case IRON_KEY:
			case KEY_OF_B:
			case SOLID_KEY:
			case SQUARE_KEY:
			case TOURQUOISE_KEY:
			case CROSS_KEY:
			case ONYX_KEY:
			case SKELETON_KEY:
			case GOLD_KEY:
			case WINGED_KEY:
			case TOPAZ_KEY:
			case SAPPHIRE_KEY:
			case EMERALD_KEY:
			case RUBY_KEY:
			case RA_KEY:
			case MASTER_KEY:
				return true;

			default:
				return false;
			}
		}

		public int getDistance() {
			return getDefinition().getDistance();
		}

		public int getShootDamage() {
			return getDefinition().getShootDamage();
		}

		public int getDeltaEnergy() {
			return getDefinition().getDeltaEnergy();
		}

		/**
		 * Returns the types corresponding to potion items.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public static Set<Item.Type> getPotionTypes() {
			return EnumSet.range(Type.MON_POTION, Type.FUL_BOMB);
		}

		/**
		 * Returns the types corresponding to weapon items.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public static Set<Item.Type> getWeaponTypes() {
			return EnumSet.range(Type.EYE_OF_TIME, Type.THE_FIRESTAFF_COMPLETE);
		}

		/**
		 * Returns the types corresponding to cloth items.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public static Set<Item.Type> getClothTypes() {
			return EnumSet.range(Type.CAPE, Type.HALTER);
		}

		/**
		 * Returns the types corresponding to miscellaneous items.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public static Set<Item.Type> getMiscellaneousTypes() {
			return EnumSet.range(Type.EMPTY_FLASK, Type.ZOKATHRA_SPELL);
		}

		/**
		 * Returns the types corresponding to food items.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public static Set<Item.Type> getFoodTypes() {
			return EnumSet.range(Item.Type.APPLE, Item.Type.DRAGON_STEAK);
		}

		// Visibility package protected because items must be created via the
		// ItemFactory
		Item newItem() {
			// Consider the types for which there's a dedicated class first
			switch(this) {
			case CHEST:
				return new Chest();
			case TORCH:
				return new Torch();
			case COMPASS:
				return new Compass();
			case BONES:
				return new Bones();
			case SCROLL:
				return new Scroll();
			default:
				break;
			}

			if (getPotionTypes().contains(this)) {
				return new Potion(this);
			} else if (getWeaponTypes().contains(this)) {
				return new MiscItem(this);
			} else if (getFoodTypes().contains(this)) {
				return new Food(this);
			} else if (getMiscellaneousTypes().contains(this) || getClothTypes().contains(this)) {
				return new MiscItem(this);
			}

			throw new UnsupportedOperationException("Method unsupported for type " + this);
		}

		/**
		 * Returns the body part that activates this item (if any). Returns null if
		 * the item can't be activated. When activated an item provides (in general)
		 * a bonus to the champion (be it a defense bonus, a stat bonus, etc).
		 * Examples: This method returns {@link BodyPart.Type#NECK} for an amulet,
		 * {@link BodyPart.Type#WEAPON_HAND} for a weapon item.
		 *
		 * @return the body part which activates this item (if any) or null.
		 */
		public BodyPart.Type getActivationBodyPart() {
			return getDefinition().getActivationBodyPart();
		}

		/**
		 * Returns the shield bonus bestowed by this item when activated.
		 *
		 * @return an integer representing a shield bonus. Returns zero if the item
		 *         doesn't bestow any bonus.
		 */
		public final int getShield() {
			return getDefinition().getShield();
		}

		/**
		 * Returns the fire resistance bonus bestowed by this item when activated.
		 *
		 * @return an integer representing a fire resistance bonus. Returns zero if
		 *         the item doesn't bestow any bonus.
		 */
		public int getAntiMagic() {
			return getDefinition().getAntiMagic();
		}
	}

	/**
	 * Enumerates the champion statistics that can be affected (positively or
	 * negatively) by an item effect.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum AffectedStatistic {
		MANA,
		STRENGTH,
		MAX_LOAD,
		DEXTERITY,
		// INTELLIGENCE,
		WISDOM,
		WIZARD_LEVEL,
		ANTI_FIRE,
		LUCK,
		ANTI_MAGIC,
		HEAL_SKILL,
		DEFEND_SKILL,
		INFLUENCE_SKILL,
		ALL_SKILLS;
	}

	protected Item(Type type) {
		Validate.notNull(type, "The given item type is null");

		this.type = type;

		// Store the number of charges associated to this item to decrease this
		// number when the item's used
		Map<Action, Integer> map = null;

		for (ActionDef actionDef : type.getActions()) {
			if (actionDef.isUseCharges()) {
				// This action is limited by a number of charges
				if (map == null) {
					map = new EnumMap<Action, Integer>(Action.class);
				}

				// FIXME How to determine the number of charges ? This differs from one object to another
				map.put(actionDef.getAction(), 3);
			}
		}

		if ((map != null) && !map.isEmpty()) {
			this.charges = map;
		} else {
			this.charges = null;
		}

		this.effects = type.getEffects();
	}

	/**
	 * The item's type. Can't be null.
	 */
	private final Type type;

	/**
	 * Sequence used for generating unique ids for items.
	 */
	private final int id = SEQUENCE.incrementAndGet();

	/**
	 * Map storing the remaining charges per action. Set to null if no charges
	 * allowed.
	 */
	private final Map<Action, Integer> charges;

	/**
	 * The possible item curse.
	 */
	private Curse curse;

	/**
	 * Support class used for firing change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The strength of this (poisonous) item as a power rune.
	 */
	private PowerRune poisonStrength;

	/**
	 * The effects bestowed by this item (if any).
	 */
	private final List<Effect> effects;

	public final Type getType() {
		return type;
	}

	/**
	 * Tells whether this item can be consumed (that is, eaten or drunk).
	 *
	 * @return whether this item can be consumed (that is, eaten or drunk).
	 */
	public final boolean isConsumable() {
		return getCarryLocations().contains(CarryLocation.CONSUMABLE);
	}

	/**
	 * Returns the item's weight as a float.
	 *
	 * @return a float representing a weight in Kg.
	 */
	public float getWeight() {
		// The default implementation returns the base weight for the item.
		// In general this implementation is fine however some items have a
		// dynamic weight that depends on their state. For instance, the
		// weight of a flask (or water skin) depends on whether the item
		// contains water. In this case, the sub-class has to override
		// this default implementation to provide a relevant one
		return getType().getWeight();
	}

	/**
	 * Returns the body part that activates this item (if any). Returns null if
	 * the item can't be activated. When activated an item provides (in general)
	 * a bonus to the champion (be it a defense bonus, a stat bonus, etc).
	 * Examples: This method returns {@link BodyPart.Type#NECK} for an amulet,
	 * {@link BodyPart.Type#WEAPON_HAND} for a weapon item.
	 *
	 * @return the body part which activates this item (if any) or null.
	 */
	protected final BodyPart.Type getActivationBodyPart() {
		return getType().getActivationBodyPart();
	}

	/**
	 * Tells whether this item can be activated by the given body part.
	 *
	 * @param bodyPart
	 *            the body part to test. Can't be null.
	 * @return whether this item can be activated by the given body part.
	 */
	public final boolean isActivatedBy(BodyPart bodyPart) {
		Validate.notNull(bodyPart, "The given body part is null");

		if (bodyPart.getType().equals(getActivationBodyPart())) {
			return true;
		}

		// Special case: the rabbit foot can be activated by both hands and when
		// stored the inventory
		if (Item.Type.RABBIT_FOOT.equals(getType())) {
			return (bodyPart.getType().equals(BodyPart.Type.WEAPON_HAND) || bodyPart.getType().equals(BodyPart.Type.SHIELD_HAND));
		}

		return false;
	}

	/**
	 * Returns all the carry locations where this item can be stored.
	 *
	 * @return a set containing the carry locations compatible with this item.
	 *         Never returns null.
	 */
	public final Set<CarryLocation> getCarryLocations() {
		return getType().getCarryLocations();
	}

	/**
	 * Tells whether this item is cursed. Note: When grabbed, a cursed item
	 * can't be released unless conjured.
	 *
	 * @return whether this item is cursed.
	 */
	public boolean isCursed() {
		return (curse != null) && curse.isActive();
	}

	/**
	 * Tells whether this item is cursed <b>and</b> if the curse has been detected.
	 *
	 * @return whether this item is cursed <b>and</b> if the curse has been detected.
	 */
	public boolean isCurseDetected() {
		return isCursed() && curse.isDetected();
	}

	/**
	 * Curses this item (or strengthens the item's curse) with the given power.
	 *
	 * @param powerRune
	 *            the power rune representing the strength of the curse. Can't
	 *            be null.
	 */
	public void curse(final PowerRune powerRune) {
		Validate.notNull(powerRune, "The given power rune is null");

		if (curse == null) {
			// Create the curse only when strictly necessary
			curse = new Curse(this);
		}

		final boolean wasActive = curse.isActive();

		curse.curse(powerRune);

		if (!wasActive && curse.isActive()) {
			// When holding a cursed item, the champion's luck decreases by 3 points
			if (champion != null) {
				champion.getStats().getLuck().dec(3);
			}

			fireChangeEvent();
		}
	}

	/**
	 * Conjures the item's curse with the given power.
	 *
	 * @param powerRune
	 *            a power rune representing the strength of the conjuration.
	 */
	public void conjure(PowerRune powerRune) {
		Validate.notNull(powerRune, "The given power rune is null");

		if (curse != null) {
			final boolean wasActive = curse.isActive();

			curse.conjure(powerRune);

			if (wasActive && !curse.isActive()) {
				// Once conjured, the champion's luck increases by 3 points
				if (champion != null) {
					champion.getStats().getLuck().inc(3);
				}

				fireChangeEvent();

				curse = null;
			}
		} else {
			// The item isn't cursed, do nothing
		}
	}

	/**
	 * Tells whether this item (when put on / grabbed) can be removed from the
	 * champion's body part. The cursed items can't be removed.
	 *
	 * @return whether this item can be removed.
	 */
	public boolean isRemovable() {
		if (isCursed()) {
			if (!curse.isDetected()) {
				// The curse is now detected
				curse.setDetected(true);

				// Fire a change event to notify the detection
				fireChangeEvent();
			}

			// A curse item can't be removed
			return false;
		}

		// Any other item can be removed
		return true;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	protected final void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	/**
	 * The champion currently holding / wearing the item (if any).
	 */
	private Champion champion;

	/**
	 * The body part on which this item is currently worn (if any).
	 */
	private BodyPart bodyPart;

	/**
	 * Returns the body part on which this item is currently worn (if any).
	 *
	 * @return a body part or null.
	 */
	protected final BodyPart getBodyPart() {
		return bodyPart;
	}

	/**
	 * Notifies the item that it has just been taken / put on the given body
	 * part. This will activate the object (if possible).
	 *
	 * @param bodyPart
	 *            the body part on which the item was put. Can't be null.
	 */
	public final void itemPutOn(final BodyPart bodyPart) {
		Validate.notNull(bodyPart, "The given body part is null");

		if (this.bodyPart != null) {
			// The item is already carried by a champion
			throw new IllegalStateException(this + " is already worn by " + this.bodyPart);
		}
		if (this.champion != null) {
			// The item is already carried by a champion
			throw new IllegalStateException(this + " is already worn by " + this.champion.getName());
		}

		// Store the champion that carries the item
		this.champion = bodyPart.getBody().getChampion();
		this.bodyPart = bodyPart;

		if (this instanceof DirectionChangeListener) {
			// Listen to direction changes
			this.champion.getParty().addDirectionChangeListener((DirectionChangeListener) this);
		}

		// If the item is cursed, the luck decreases by 3 points
		if (isCursed()) {
			champion.getStats().getLuck().dec(3);
		}

		if (hasEffects() && (bodyPart != null) && isActivatedBy(bodyPart)) {
			// Apply the item effects to the champion
			for (Effect effect : effects) {
				effect.affect(champion);
			}
		}

		// Callback
		putOn();
	}

	/**
	 * Tells whether items has some effects when activated.
	 *
	 * @return whether items has some effects when activated.
	 */
	public boolean hasEffects() {
		return !effects.isEmpty();
	}

	/**
	 * Callback method to notify this item that it has been grabbed / put on.
	 * Meant to be overridden in subclasses to implement a custom behavior.
	 */
	protected void putOn() {
		if (log.isDebugEnabled()) {
			log.debug(String.format("%s.%s.Item: [+] %s", champion.getName(), bodyPart.getType().getLabel(), this));
		}
	}

	/**
	 * Notifies the item that it has just been taken off / removed.
	 */
	public final void itemTakenOff() {
		// Callback
		takeOff();

		if (hasEffects() && (bodyPart != null) && isActivatedBy(bodyPart)) {
			// Neutralize the item's effects
			for (Effect effect : effects) {
				effect.unaffect(champion);
			}
		}

		// If the item is cursed, luck increases by 3 points
		if (isCursed()) {
			champion.getStats().getLuck().inc(3);
		}

		if (this instanceof DirectionChangeListener) {
			// Unregister the listener
			this.champion.getParty().removeDirectionChangeListener((DirectionChangeListener) this);
		}

		// Reset the champion and body part
		this.champion = null;
		this.bodyPart = null;
	}

	/**
	 * Callback method to notify this item that it has been taken off. Meant to
	 * be overridden in subclasses to implement a custom behavior.
	 */
	protected void takeOff() {
		if (log.isDebugEnabled()) {
			log.debug(String.format("%s.%s.Item: [-] %s", champion.getName(), bodyPart.getType().getLabel(), this));
		}
	}

	/**
	 * Tells whether the item is currently activated.
	 *
	 * @return whether the item is currently activated.
	 */
	public final boolean isActivated() {
		// The item is activated when it's carried by a champion and on the
		// relevant (activating) body part
		return (champion != null);
	}

	/**
	 * Returns the fire resistance bonus bestowed by this item when activated.
	 *
	 * @return an integer representing a fire resistance bonus. Returns zero if
	 *         the item doesn't bestow any bonus.
	 */
	public final int getAntiMagic() {
		return getType().getAntiMagic();
	}

	/**
	 * Returns the shield bonus bestowed by this item when activated.
	 *
	 * @return an integer representing a shield bonus. Returns zero if the item
	 *         doesn't bestow any bonus.
	 */
	public final int getShield() {
		return getType().getShield();
	}

	@Override
	public String toString() {
		return String.format("%s#%d", getType().name(), id);
	}

	/**
	 * Returns whether this item is poisonous. In Dungeon Master, food can be poisonous.
	 *
	 * @return whether this item is poisonous.
	 */
	public boolean isPoisonous() {
		return (poisonStrength != null);
	}

	/**
	 * Returns the strength of poison induced by this object (if poisonous).
	 *
	 * @return a power rune representing the strength of poison for this item or
	 *         null it the item isn't poisonous.
	 */
	public PowerRune getPoisonStrength() {
		return poisonStrength;
	}

	/**
	 * Sets the strength of poison induced by this object (if poisonous) from
	 * the given power rune.
	 *
	 * @param powerRune
	 *            a power rune representing the strength of poison for this item
	 *            or null it the item isn't poisonous.
	 */
	public void setPoisonStrength(PowerRune powerRune) {
		// The strength can be null
		this.poisonStrength = powerRune;
	}

	/**
	 * Notifies this object that it has been consumed (eaten or drunk) by the
	 * given champion and returns the object itself (if the consumption failed),
	 * a new item (if the consumption succeeded and the item turned into a new
	 * one) or null (if the item was destroyed after consumption).
	 *
	 * @param champion
	 *            the champion that is consuming the item. Can't be null.
	 * @return an item (see above) or null.
	 */
	public final Item itemConsumed(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		if (isConsumable()) {
			return consume(champion);
		}

		// This object isn't consumable, return the item as is
		return this;
	}

	/**
	 * Callback method to be overriden by subclasses for a consumable item. Most
	 * items will simply disappear after being consumed. Some others will turn
	 * into a new object. This method can turn an object into another one after
	 * consumption. The default implementation will simply raise an exception.
	 *
	 * @param champion
	 *            the champion that consumed the item.
	 * @return an item corresponding to the consumed item.
	 */
	protected Item consume(Champion champion) {
		Validate.notNull(champion, "The given champion is null");
		if (!isConsumable()) {
			throw new UnsupportedOperationException(String.format("Item %s isn't consumable", getType().name()));
		}

		// Any consumable item must override this method
		throw new UnsupportedOperationException();
	}

	/**
	 * Uses the given action from this item and returns whether the operation
	 * succeeded. Note: The item must be carried / held by a champion.
	 *
	 * @param action
	 *            the action to perform. Can't be null.
	 * @return whether the action was successfully used.
	 */
	public boolean perform(Action action) {
		Validate.notNull(action, "The given action is null");

		if (champion == null) {
			throw new IllegalStateException("The item isn't currently held by a champion");
		}

		// The item must be in the proper (weapon) hand !
		if (!champion.getBody().getWeaponHand().getItem().equals(this)) {
			throw new UnsupportedOperationException(String.format("This item isn't held in %s's weapon hand", champion.getName()));
		}

		// Search for the action
		for (ItemDef.ActionDef actionDef : getType().getActions()) {
			final Action curAction = actionDef.getAction();

			if (curAction.equals(action)) {
				// Action found
				if (!actionDef.isUsable(champion)) {
					// The champion isn't skilled enough to use this action
					return false;
				}

				// The champion can use the action. Let it operate (this will
				// disable the weapon hand-
				return action.perform(champion.getParty().getDungeon(), champion);
			}
		}

		throw new IllegalArgumentException(String.format("The given action %s isn't among this item's actions", action));
	}

	/**
	 * Returns the actions available for this item by taking into account the
	 * skills of the given champion. In Dungeon Master, some advanced item
	 * actions can only be used by experienced champions.
	 *
	 * @param champion
	 *            the champion to test. Can't be null.
	 * @return a list of item actions. Never returns null.
	 */
	public final List<Action> getActions(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		final List<Action> result = new ArrayList<Action>();

		// Only return the actions available for this champion
		for (ItemDef.ActionDef actionDef : getType().getActions()) {
			if (!actionDef.isUsable(champion)) {
				// The champion isn't skilled enough to use this action
				continue;
			}

			// Is the action limited by a number of charges ?
			if (actionDef.isUseCharges()) {
				// What's the remaining charges ?
				final int chargeCount = charges.get(actionDef.getAction());

				if (chargeCount == 0) {
					// Charges depleted
					continue;
				}
			}

			// The champion can use this action
			result.add(actionDef.getAction());
		}

		return result;
	}

	/**
	 * Returns the effects caused by this item.
	 *
	 * @return a list of effects. Never returns null.
	 */
	public List<Effect> getEffects() {
		// Defensive recopy
		return new ArrayList<ItemDef.Effect>(effects);
	}

	/**
	 * Tells whether this item is a food item.
	 *
	 * @return whether this item is a food item.
	 */
	public final boolean isFood() {
		return Item.Type.getFoodTypes().contains(getType());
	}
}