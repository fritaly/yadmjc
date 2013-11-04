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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.event.DirectionChangeListener;
import fr.ritaly.dungeonmaster.item.Combo.Entry;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.stat.Stats;

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
	 * Enumerates the possible categories of items.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Category {
		/**
		 * The scroll category contains only 1 item type (scroll).
		 */
		SCROLL(EnumSet.of(Type.SCROLL)),

		/**
		 * The container category contains only 1 item type (chest).
		 */
		CONTAINER(EnumSet.of(Type.CHEST)),

		/**
		 * The category of potion items.
		 */
		POTION(EnumSet.range(Type.MON_POTION, Type.EMPTY_FLASK)),

		/**
		 * The category of weapon items.
		 */
		WEAPON(EnumSet.range(Type.EYE_OF_TIME, Type.THE_FIRESTAFF_COMPLETE)),

		/**
		 * The category of cloth items.
		 */
		CLOTH(EnumSet.range(Type.CAPE, Type.HALTER)),

		/**
		 * The category of other miscellaneous items.
		 */
		MISCELLANEOUS(EnumSet.range(Type.COMPASS, Type.ZOKATHRA_SPELL));

		// TODO This is a sub-type of MISCELLANEOUS. Make this more consistent
		private static final EnumSet<Item.Type> FOOD_TYPES = EnumSet.range(Item.Type.APPLE, Item.Type.DRAGON_STEAK);

		/**
		 * Set containing the item types associated to this category.
		 */
		private final EnumSet<Type> types;

		private Category(EnumSet<Type> types) {
			Validate.notNull(types, "The given enum set is null");

			this.types = types;
		}

		/**
		 * Returns the item types associated to this category.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public EnumSet<Type> getTypes() {
			return types;
		}

		/**
		 * Returns the item types corresponding to the food items. Note: The
		 * food items are included in the {@link #MISCELLANEOUS} category.
		 *
		 * @return a set of item types. Never returns null.
		 */
		public static EnumSet<Type> getFoodItems() {
			return FOOD_TYPES;
		}
	}

	/**
	 * TODO Translate this javadoc comment to english
	 *
	 * Enum�ration des types d'objets. ATTENTION !! L'ordre des �num�rations est
	 * importante car il d�termine la {@link Category} de chaque type d'objet.
	 * Le nombre d'objets dans chaque {@link Category} n'est pas le m�me que
	 * dans la sp�cification de dmweb.free.fr car certains objets partagent les
	 * m�mes caract�ristiques mais pas les m�mes images, du coup il y a deux
	 * valeurs distinctes d'enum�ration pour ces objets-l�.<br>
	 * <br>
	 * Source: <a href="http://dmweb.free.fr/?q=node/886">Technical
	 * Documentation - Dungeon Master and Chaos Strikes Back Items
	 * properties</a>
	 */
	public static enum Type {
		SCROLL(0.5f, Combo.COMBO_0, CarryLocations.CHEST_POUCH),

		// --- Chest --- //

		CHEST(5.0f, Combo.COMBO_0, CarryLocations.HANDS_BACKPACK),

		// --- Potions --- //

		MON_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		UM_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		DES_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		POISON_POTION(
			0.3f,
			Combo.COMBO_42,
			CarryLocations.CHEST_POUCH),
		SAR_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		ZO_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		DEXTERITY_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		STRENGTH_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		WISDOM_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		VITALITY_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		ANTIDOTE_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		STAMINA_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		ANTI_MAGIC_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		MANA_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		HEALTH_POTION(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		WATER_FLASK(
			0.4f, // Fiole vide (0.1f) + eau (0.3f)
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		KATH_BOMB(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		PEW_BOMB(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		RA_BOMB(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH), // Not fully implemented
		FUL_BOMB(
			0.3f,
			Combo.COMBO_42,
			CarryLocations.CHEST_POUCH),
		EMPTY_FLASK(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),

		// --- Weapons --- //

		EYE_OF_TIME(
			0.1f,
			2, 0, 0, 2,
			Combo.COMBO_43,
			CarryLocations.CHEST_POUCH),
		STORMRING(
			0.1f,
			2, 0, 0, 3,
			Combo.COMBO_7,
			CarryLocations.CHEST_POUCH),
		TORCH(
			1.1f,
			8, 2, 0, 0,
			Combo.COMBO_5,
			CarryLocations.CHEST),
		FLAMITT(
			1.2f,
			10, 80, 30, 0,
			Combo.COMBO_6,
			CarryLocations.CHEST),
		STAFF_OF_CLAWS(
			0.9f,
			16, 7, 0, 1,
			Combo.COMBO_8,
			CarryLocations.QUIVER1),
		BOLT_BLADE(
			3.0f,
			49, 110, 66, 1,
			Combo.COMBO_9,
			CarryLocations.QUIVER1),
		STORM(
			3.0f,
			49, 110, 66, 1,
			Combo.COMBO_9,
			CarryLocations.QUIVER1), // Same as previous
		FURY(
			4.7f,
			55, 20, 0, 0,
			Combo.COMBO_10,
			CarryLocations.QUIVER1),
		RA_BLADE(
			4.7f,
			55, 20, 0, 0,
			Combo.COMBO_10,
			CarryLocations.QUIVER1), // Same as previous
		THE_FIRESTAFF(
			2.4f,
			25, 1, 255, 15,
			Combo.COMBO_11,
			CarryLocations.QUIVER1),
		DAGGER(
			0.5f,
			10, 19, 0, 2,
			Combo.COMBO_12,
			CarryLocations.CHEST_POUCH_QUIVER2),
		FALCHION(
			3.3f,
			30, 8, 0, 0,
			Combo.COMBO_13,
			CarryLocations.QUIVER1),
		SWORD(
			3.2f,
			34, 10, 0, 0,
			Combo.COMBO_13,
			CarryLocations.QUIVER1),
		RAPIER(
			3.6f,
			38, 10, 0, 0,
			Combo.COMBO_14,
			CarryLocations.QUIVER1),
		SABRE(
			3.5f,
			42, 11, 0, 0,
			Combo.COMBO_15,
			CarryLocations.QUIVER1),
		BITER(
			3.5f,
			42, 11, 0, 0,
			Combo.COMBO_15,
			CarryLocations.QUIVER1), // Same as previous
		SAMURAI_SWORD(
			3.6f,
			46, 12, 0, 0,
			Combo.COMBO_15,
			CarryLocations.QUIVER1),
		DELTA(
			3.3f,
			50, 14, 0, 0,
			Combo.COMBO_16,
			CarryLocations.QUIVER1),
		SIDE_SPLITTER(
			3.3f,
			50, 14, 0, 0,
			Combo.COMBO_16,
			CarryLocations.QUIVER1), // Same as previous
		DIAMOND_EDGE(
			3.7f,
			62, 14, 0, 0,
			Combo.COMBO_17,
			CarryLocations.QUIVER1),
		VORPAL_BLADE(
			3.9f,
			48, 13, 0, 0,
			Combo.COMBO_18,
			CarryLocations.QUIVER1),
		THE_INQUISITOR(
			3.9f,
			58, 15, 0, 0,
			Combo.COMBO_19,
			CarryLocations.QUIVER1),
		DRAGON_FANG(
			3.4f,
			58, 15, 0, 0,
			Combo.COMBO_19,
			CarryLocations.QUIVER1), // Same as previous
		AXE(
			4.3f,
			49, 33, 0, 2,
			Combo.COMBO_20,
			CarryLocations.QUIVER1),
		HARD_CLEAVE(
			5.7f,
			70, 44, 0, 2,
			Combo.COMBO_21,
			CarryLocations.QUIVER1),
		EXECUTIONER(
			6.5f,
			70, 44, 0, 2,
			Combo.COMBO_21,
			CarryLocations.QUIVER1), // Same as previous
		MACE(
			3.1f,
			32, 10, 0, 0,
			Combo.COMBO_22,
			CarryLocations.QUIVER1),
		MACE_OF_ORDER(
			4.1f,
			42, 13, 0, 0,
			Combo.COMBO_22,
			CarryLocations.QUIVER1),
		MORNING_STAR(
			5.0f,
			60, 15, 0, 0,
			Combo.COMBO_23,
			CarryLocations.CHEST_QUIVER1),
		CLUB(
			3.6f,
			19, 10, 0, 0,
			Combo.COMBO_24,
			CarryLocations.QUIVER1),
		STONE_CLUB(
			11.0f,
			44, 22, 0, 0,
			Combo.COMBO_24,
			CarryLocations.QUIVER1),
		BOW(
			1.5f,
			1, 50, 50, 4,
			Combo.COMBO_27,
			CarryLocations.QUIVER1),
		CLAW_BOW(
			2.0f,
			1, 50, 50, 4,
			Combo.COMBO_27,
			CarryLocations.QUIVER1), // Same as previous
		CROSSBOW(
			2.8f,
			1, 180, 120, 14,
			Combo.COMBO_27,
			CarryLocations.QUIVER1),
		ARROW(
			0.2f,
			2, 10, 0, 10,
			Combo.COMBO_26,
			CarryLocations.CHEST_POUCH_QUIVER2),
		SLAYER(
			0.2f,
			2, 28, 0, 10,
			Combo.COMBO_26,
			CarryLocations.CHEST_POUCH_QUIVER2),
		SLING(
			1.9f,
			5, 20, 50, 7,
			Combo.COMBO_27,
			CarryLocations.CHEST_QUIVER1),
		ROCK(
			1.0f,
			6, 18, 0, 11,
			Combo.COMBO_42,
			CarryLocations.CHEST_POUCH_QUIVER2),
		POISON_DART(
			0.3f,
			7, 23, 0, 12,
			Combo.COMBO_40,
			CarryLocations.CHEST_POUCH_QUIVER2),
		THROWING_STAR(
			0.1f,
			3, 19, 0, 1,
			Combo.COMBO_42,
			CarryLocations.CHEST_POUCH_QUIVER2),
		STICK(
			0.2f,
			4, 4, 0, 0,
			Combo.COMBO_5,
			CarryLocations.QUIVER1),
		STAFF(
			2.6f,
			12, 4, 0, 1,
			Combo.COMBO_5,
			CarryLocations.QUIVER1),
		WAND(
			0.1f,
			0, 0, 0, 2,
			Combo.COMBO_28,
			CarryLocations.CHEST_POUCH_QUIVER1),
		TEOWAND(
			0.2f,
			1, 20, 0, 12,
			Combo.COMBO_29,
			CarryLocations.CHEST_POUCH_QUIVER1),
		YEW_STAFF(
			3.5f,
			18, 6, 0, 0,
			Combo.COMBO_30,
			CarryLocations.QUIVER1),
		STAFF_OF_MANAR(
			2.9f,
			0, 4, 0, 15,
			Combo.COMBO_31,
			CarryLocations.QUIVER1),
		STAFF_OF_IRRA(
			2.9f,
			0, 4, 0, 15,
			Combo.COMBO_31,
			CarryLocations.QUIVER1), // Same as previous
		SNAKE_STAFF(
			2.1f,
			0, 3, 0, 3,
			Combo.COMBO_32,
			CarryLocations.QUIVER1),
		CROSS_OF_NETA(
			2.1f,
			0, 3, 0, 3,
			Combo.COMBO_32,
			CarryLocations.QUIVER1), // Same as previous
		THE_CONDUIT(
			3.3f,
			0, 7, 0, 8,
			Combo.COMBO_33,
			CarryLocations.QUIVER1),
		SERPENT_STAFF(
			3.3f,
			0, 7, 0, 8,
			Combo.COMBO_33,
			CarryLocations.QUIVER1), // Same as previous
		DRAGON_SPIT(
			0.8f,
			3, 1, 0, 4,
			Combo.COMBO_5,
			CarryLocations.CHEST_QUIVER1),
		SCEPTRE_OF_LYT(
			1.8f,
			9, 4, 0, 3,
			Combo.COMBO_35,
			CarryLocations.QUIVER1),
		HORN_OF_FEAR(
			0.8f,
			1, 1, 0, 0,
			Combo.COMBO_36,
			CarryLocations.CHEST_QUIVER1),
		SPEED_BOW(
			3.0f,
			1, 220, 125, 10,
			Combo.COMBO_27,
			CarryLocations.QUIVER1),
		THE_FIRESTAFF_COMPLETE(
			3.6f,
			100, 50, 255, 15,
			Combo.COMBO_1,
			CarryLocations.QUIVER1),

		// --- Clothes --- //

			CAPE(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_NECK_TORSO),
		CLOAK_OF_NIGHT(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_NECK_TORSO),
		BARBARIAN_HIDE(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		TATTERED_PANTS(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS), // Same as previous
		SANDALS(
			0.6f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		LEATHER_BOOTS(
			1.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		ROBE_BODY(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		TATTERED_SHIRT(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO), // Same as previous
		ROBE_LEGS(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		FINE_ROBE_BODY(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		FINE_ROBE_LEGS(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		KIRTLE(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		SILK_SHIRT(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		TABARD(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		GUNNA(
			0.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		ELVEN_DOUBLET(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		ELVEN_HUKE(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		ELVEN_BOOTS(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		LEATHER_JERKIN(
			0.6f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		LEATHER_PANTS(
			0.8f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		SUEDE_BOOTS(
			1.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		BLUE_PANTS(
			0.6f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		TUNIC(
			0.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		GHI(
			0.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		GHI_TROUSERS(
			0.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		CALISTA(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		CROWN_OF_NERRA(
			0.6f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		BEZERKER_HELM(
			1.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		HELMET(
			1.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		BASINET(
			1.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		BUCKLER(
			1.1f,
			Combo.COMBO_41,
			CarryLocations.CHEST),
		NETA_SHIELD(
			1.1f,
			Combo.COMBO_41,
			CarryLocations.CHEST), // Same as previous
		HIDE_SHIELD(
			1.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		CRYSTAL_SHIELD(
			1.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Same as previous
		WOODEN_SHIELD(
			1.4f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		SMALL_SHIELD(
			2.1f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		MAIL_AKETON(
			6.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		LEG_MAIL(
			5.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		MITHRAL_AKETON(
			4.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		MITHRAL_MAIL(
			3.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_LEGS),
		CASQUE_N_COIF(
			1.6f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		HOSEN(
			0.9f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		ARMET(
			1.9f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		TORSO_PLATE(
			12.0f,
			Combo.COMBO_0,
			CarryLocations.TORSO),
		LEG_PLATE(
			8.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS),
		FOOT_PLATE(
			2.8f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		LARGE_SHIELD(
			3.4f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		SAR_SHIELD(
			5.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Same as previous
		HELM_OF_LYTE(
			1.7f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		HELM_OF_RA(
			2.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD), // Same as previous
		PLATE_OF_LYTE(
			10.8f,
			Combo.COMBO_0,
			CarryLocations.TORSO),
		PLATE_OF_RA(
			12.1f,
			Combo.COMBO_0,
			CarryLocations.TORSO), // Same as previous
		POLEYN_OF_LYTE(
			7.2f,
			Combo.COMBO_0,
			CarryLocations.LEGS),
		POLEYN_OF_RA(
			8.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS), // Same as previous
		GREAVE_OF_LYTE(
			2.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		GREAVE_OF_RA(
			2.8f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET), // Same as previous
		SHIELD_OF_LYTE(
			3.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		SHIELD_OF_RA(
			3.4f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Same as previous
		HELM_OF_DARC(
			3.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		DRAGON_HELM(
			3.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD), // Same as previous
		PLATE_OF_DARC(
			14.1f,
			Combo.COMBO_0,
			CarryLocations.TORSO),
		DRAGON_PLATE(
			14.1f,
			Combo.COMBO_0,
			CarryLocations.TORSO), // Same as previous
		POLEYN_OF_DARC(
			9.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS),
		DRAGON_POLEYN(
			9.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS), // Same as previous
		GREAVE_OF_DARC(
			3.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		DRAGON_GREAVE(
			3.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET), // Same as previous
		SHIELD_OF_DARC(
			4.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		DRAGON_SHIELD(
			4.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Same as previous
		DEXHELM(
			1.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		FLAMEBAIN(
			5.7f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),
		POWERTOWERS(
			8.2f,
			Combo.COMBO_0,
			CarryLocations.LEGS),
		BOOTS_OF_SPEED(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		HALTER(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_TORSO),

		// --- Miscellaneous --- //

		COMPASS(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		WATER_SKIN(
			1.2f, // Outre vide (0.3f) + 3 rations d'eau (3 * 0.3f)
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE), // Same as previous
		JEWEL_SYMAL(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		ILLUMULET(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		ASHES(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		BONES(
			0.8f,
			Combo.COMBO_0,
			CarryLocations.CHEST),
		COPPER_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH),
		SAR_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH), // Same as previous
		SILVER_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH), // Same as previous
		GOLD_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH),
		GOR_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH), // Same as previous
		IRON_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		KEY_OF_B(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		SOLID_KEY(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		SQUARE_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		TOURQUOISE_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		CROSS_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		ONYX_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		SKELETON_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		GOLD_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		WINGED_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		TOPAZ_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		SAPPHIRE_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		EMERALD_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		RUBY_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		RA_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		MASTER_KEY(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		BOULDER(
			8.1f,
			Combo.COMBO_0,
			CarryLocations.HANDS_BACKPACK),
		BLUE_GEM(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		ORANGE_GEM(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		GREEN_GEM(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		APPLE(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		CORN(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		BREAD(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		CHEESE(
			0.8f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		SCREAMER_SLICE(
			0.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		WORM_ROUND(
			1.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		DRUMSTICK(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		SHANK(
			0.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		DRAGON_STEAK(
			0.6f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_CONSUMABLE),
		GEM_OF_AGES(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		EKKHARD_CROSS(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		MOONSTONE(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		THE_HELLION(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		PENDANT_FERAL(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		MAGICAL_BOX_BLUE(
			0.6f,
			Combo.COMBO_38,
			CarryLocations.CHEST_POUCH),
		MAGICAL_BOX_GREEN(
			0.9f,
			Combo.COMBO_38,
			CarryLocations.CHEST_POUCH),
		MIRROR_OF_DAWN(
			0.3f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		ROPE(
			1.0f,
			Combo.COMBO_39,
			CarryLocations.CHEST),
		RABBIT_FOOT(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		CORBAMITE(
			0.0f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		CORBUM(
			0.0f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		CHOKER(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH_NECK),
		LOCK_PICKS(
			0.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		MAGNIFIER(
			0.2f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		ZOKATHRA_SPELL(
			0.0f,
			Combo.COMBO_0,
			CarryLocations.NONE);

		private final Combo combo;

		/**
		 * The base value used for computing the damages caused by an attack
		 * with this item. Relevant only for a weapon item for which the value
		 * is positive. For other items, the value is set to -1.
		 */
		private final int damage;

		/**
		 * This value determines how far the item will go when thrown. If a
		 * weapon is used to "Shoot", this will be a part of how far the item
		 * being shot will travel. The farther the projectile goes, the more
		 * damage it does (Damage is decreased as it flies). Valeur dans
		 * l'intervalle [0-255].
		 */
		private final int distance;

		/**
		 * The amount of damage associated with fired projectiles. Value within
		 * [0,255].
		 */
		private final int shootDamage;

		/**
		 * The amount of energy lost by the projectile every time it moves.
		 */
		private final int deltaEnergy;

		/**
		 * The carry locations where this item can be stored.
		 */
		private final CarryLocations carryLocations;

		/**
		 * The item's weight (in Kg) as a float.
		 */
		private final float weight;

		// FIXME Take into account distance, shootDamage, deltaEnergy

		// Special constructor for weapon items
		private Type(float weight, int damage, int distance, int shootDamage, int deltaEnergy, Combo combo,
				CarryLocations carryLocations) {

			Validate.isTrue(weight >= 0.0f, "The given weight " + weight + " must be positive or zero");
			Validate.isTrue(damage >= 0, "The given damage " + damage + " must be positive or zero");
			Validate.isTrue(distance >= 0, "The given distance " + distance + " must be positive or zero");
			Validate.isTrue(shootDamage >= 0, "The given shoot damage " + shootDamage + " must be positive or zero");
			Validate.isTrue(deltaEnergy >= 0, "The given delta energy " + deltaEnergy + " must be positive or zero");
			Validate.notNull(combo, "The given combo is null");
			Validate.notNull(carryLocations, "The given carry locations is null");

			this.weight = weight;
			this.damage = damage;
			this.distance = distance;
			this.shootDamage = shootDamage;
			this.deltaEnergy = deltaEnergy;
			this.combo = combo;
			this.carryLocations = carryLocations;
		}

		// Special constructor for non-weapon items
		private Type(float weight, Combo combo, CarryLocations carryLocations) {
			Validate.isTrue(weight >= 0.0f, "The given weight " + weight + " must be positive or zero");
			Validate.notNull(combo, "The given combo is null");
			Validate.notNull(carryLocations, "The given carry locations is null");

			this.weight = weight;
			this.damage = -1;
			this.distance = -1;
			this.shootDamage = -1;
			this.deltaEnergy = -1;
			this.combo = combo;
			this.carryLocations = carryLocations;
		}

		/**
		 * Returns the action combo associated to this item.
		 *
		 * @return an instance of {@link Combo}. Never returns null.
		 */
		public Combo getCombo() {
			return combo;
		}

		/**
		 * Returns the category this item belongs to.
		 *
		 * @return an item category. Never returns null.
		 */
		public Category getCategory() {
			for (Category category : Category.values()) {
				if (category.getTypes().contains(this)) {
					return category;
				}
			}

			throw new UnsupportedOperationException("Method unsupported for item " + this);
		}

		/**
		 * Returns the carry locations where this item can be stored.
		 *
		 * @return an instance of {@link CarryLocations}. Never returns null.
		 */
		public CarryLocations getCarryLocations() {
			return carryLocations;
		}

		/**
		 * Returns the item's weight as a float.
		 *
		 * @return a float representing a weight in Kg.
		 */
		public float getWeight() {
			return weight;
		}

		/**
		 * Returns the effects bestowed by this item to a champion when activated.
		 *
		 * @return a list of effects. Never returns null.
		 */
		public List<Effect> getEffects() {
			switch (this) {
			case STAFF_OF_CLAWS:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +4));
			case THE_FIRESTAFF:
				return Collections.singletonList(new Effect(AffectedStatistic.ALL_SKILLS, +1));
			case DELTA:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +1));
			case VORPAL_BLADE:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +4));
			case THE_INQUISITOR:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +2));
			case MACE_OF_ORDER:
				return Collections.singletonList(new Effect(AffectedStatistic.STRENGTH, +5));
			case STAFF:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +2));
			case WAND:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +1));
			case TEOWAND:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +6));
			case YEW_STAFF:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +4));
			case STAFF_OF_MANAR:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +10));
			case SNAKE_STAFF:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +8));
			case THE_CONDUIT:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +17));
			case DRAGON_SPIT:
				return Collections.singletonList(new Effect(AffectedStatistic.MANA, +7));
			case SCEPTRE_OF_LYT:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +5), new Effect(AffectedStatistic.HEAL_SKILL, +1));
			case THE_FIRESTAFF_COMPLETE:
				return Collections.singletonList(new Effect(AffectedStatistic.ALL_SKILLS, +2));
			case CLOAK_OF_NIGHT:
				return Collections.singletonList(new Effect(AffectedStatistic.DEXTERITY, +8));
			case ELVEN_BOOTS:
				// +14 Load (Should be actually +1-14)
				return Collections.singletonList(new Effect(AffectedStatistic.MAX_LOAD, 14));
			case CROWN_OF_NERRA:
				return Collections.singletonList(new Effect(AffectedStatistic.WISDOM, +10));
			case DEXHELM:
				return Collections.singletonList(new Effect(AffectedStatistic.DEXTERITY, +10));
			case FLAMEBAIN:
				return Collections.singletonList(new Effect(AffectedStatistic.ANTI_FIRE, +12));
			case POWERTOWERS:
				return Collections.singletonList(new Effect(AffectedStatistic.STRENGTH, +10));
			case GEM_OF_AGES:
				return Collections.singletonList(new Effect(AffectedStatistic.HEAL_SKILL, +1));
			case EKKHARD_CROSS:
				return Collections.singletonList(new Effect(AffectedStatistic.DEFEND_SKILL, +1));
			case MOONSTONE:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +3), new Effect(AffectedStatistic.INFLUENCE_SKILL, +3));
			case PENDANT_FERAL:
				return Collections.singletonList(new Effect(AffectedStatistic.WIZARD_LEVEL, +1));
			case RABBIT_FOOT:
				return Collections.singletonList(new Effect(AffectedStatistic.LUCK, +10));
			case JEWEL_SYMAL:
				return Collections.singletonList(new Effect(AffectedStatistic.ANTI_MAGIC, +15));
			default:
				return Collections.emptyList();
			}
		}

		/**
		 * TODO Translate this javadoc comment to english
		 *
		 * Retourne le nombre de base utilis� pour calculer combien de points de
		 * dommage une attaque cause. Pertinent uniquement pour une objet de
		 * type arme (cf {@link #getCategory()}) auquel cas la valeur est
		 * positive ou nulle autrement vaut -1.
		 *
		 * @return un entier positif ou nul si l'objet est une arme autrement
		 *         -1.
		 */
		public int getDamage() {
			return damage;
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

		/**
		 * TODO Javadoc this method
		 *
		 * @return
		 */
		public int getDistance() {
			return distance;
		}

		/**
		 * TODO Javadoc this method
		 *
		 * @return
		 */
		public int getShootDamage() {
			return shootDamage;
		}

		/**
		 * TODO Javadoc this method
		 *
		 * @return
		 */
		public int getDeltaEnergy() {
			return deltaEnergy;
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

	/**
	 * An item effect can provide a bonus or a malus to a champion's stat.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static final class Effect {

		/**
		 * The statistic affected by this effect.
		 */
		private final AffectedStatistic affectedStatistic;

		/**
		 * The strength of this effect.
		 */
		private final int value;

		public Effect(AffectedStatistic affectedStatistic, int value) {
			Validate.notNull(affectedStatistic, "The given affected statistic is null");
			Validate.isTrue(value != 0, "The given effect strength is zero");

			this.affectedStatistic = affectedStatistic;
			this.value = value;
		}

		/**
		 * Returns the statistic affected by this effect.
		 *
		 * @return the affected statistic. Never returns null.
		 */
		public AffectedStatistic getAffectedStatistic() {
			return affectedStatistic;
		}

		/**
		 * Returns the strength of this effect as an integer.
		 *
		 * @return an integer value representing the effect's strength. Can be
		 *         positive or negative.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Applies the effect to the given champion
		 *
		 * @param champion
		 *            the champion to who apply the effect. Can't be null.
		 */
		public void affect(Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			final Stats stats = champion.getStats();

			switch (affectedStatistic) {
			case DEXTERITY:
				stats.getDexterity().incMax(value);
				stats.getDexterity().inc(value);
				break;
			case MANA:
				stats.getMana().incMax(value);
				stats.getMana().inc(value);
				break;
			case STRENGTH:
				stats.getStrength().incMax(value);
				stats.getStrength().inc(value);
				break;
			case MAX_LOAD:
				// MaxLoad is a special stat for which the boost is handled
				// separately from the base value (this value is computed
				// dynamically)
				stats.getMaxLoadBoost().inc(value);
				break;
			case WISDOM:
				stats.getWisdom().incMax(value);
				stats.getWisdom().inc(value);
				break;
			case ANTI_FIRE:
				stats.getAntiFire().incMax(value);
				stats.getAntiFire().inc(value);
				break;
			case ANTI_MAGIC:
				stats.getAntiMagic().incMax(value);
				stats.getAntiMagic().inc(value);
				break;
			case LUCK:
				stats.getLuck().incMax(value);
				stats.getLuck().inc(value);
				break;
			case WIZARD_LEVEL:
				champion.getExperience(Skill.WIZARD).incBoost(value);
				break;
			case HEAL_SKILL:
				champion.getExperience(Skill.HEAL).incBoost(value);
				break;
			case DEFEND_SKILL:
				champion.getExperience(Skill.DEFEND).incBoost(value);
				break;
			case INFLUENCE_SKILL:
				champion.getExperience(Skill.INFLUENCE).incBoost(value);
				break;
			case ALL_SKILLS:
				for (Skill skill : Skill.values()) {
					champion.getExperience(skill).incBoost(value);
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported statistic " + affectedStatistic);
			}
		}

		/**
		 * Neutralizes the effect for the given champion.
		 *
		 * @param champion
		 *            the champion to who neutralize the effect. Can't be null.
		 */
		public void unaffect(Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			final Stats stats = champion.getStats();

			switch (affectedStatistic) {
			case DEXTERITY:
				stats.getDexterity().dec(value);
				stats.getDexterity().decMax(value);
				break;
			case MANA:
				stats.getMana().dec(value);
				stats.getMana().decMax(value);
				break;
			case STRENGTH:
				stats.getStrength().dec(value);
				stats.getStrength().decMax(value);
				break;
			case MAX_LOAD:
				// MaxLoad is a special stat for which the boost is handled
				// separately from the base value (this value is computed
				// dynamically)
				stats.getMaxLoadBoost().dec(value);
				break;
			case WISDOM:
				stats.getWisdom().dec(value);
				stats.getWisdom().decMax(value);
				break;
			case ANTI_FIRE:
				stats.getAntiFire().dec(value);
				stats.getAntiFire().decMax(value);
				break;
			case ANTI_MAGIC:
				stats.getAntiMagic().dec(value);
				stats.getAntiMagic().decMax(value);
				break;
			case LUCK:
				stats.getLuck().dec(value);
				stats.getLuck().decMax(value);
				break;
			case WIZARD_LEVEL:
				champion.getExperience(Skill.WIZARD).decBoost(value);
				break;
			case HEAL_SKILL:
				champion.getExperience(Skill.HEAL).decBoost(value);
				break;
			case DEFEND_SKILL:
				champion.getExperience(Skill.DEFEND).decBoost(value);
				break;
			case INFLUENCE_SKILL:
				champion.getExperience(Skill.INFLUENCE).decBoost(value);
				break;
			case ALL_SKILLS:
				for (Skill skill : Skill.values()) {
					champion.getExperience(skill).decBoost(value);
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported statistic " + affectedStatistic);
			}
		}
	}

	protected Item(Type type) {
		Validate.notNull(type, "The given item type is null");

		this.type = type;

		// Store the number of charges associated to this item to decrease this
		// number when the item's used
		Map<Action, Integer> map = null;

		for (Combo.Entry entry : type.getCombo().getEntries()) {
			if (entry.useCharges()) {
				// This action is limited by a number of charges
				if (map == null) {
					map = new EnumMap<Action, Integer>(Action.class);
				}

				// FIXME How to determine the number of charges ? This differs from one object to another
				map.put(entry.getAction(), 3);
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
	protected abstract BodyPart.Type getActivationBodyPart();

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
	public final EnumSet<CarryLocation> getCarryLocations() {
		return getType().getCarryLocations().getLocations();
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
			curse = new Curse();
		}

		final boolean wasActive = curse.isActive();

		curse.curse(powerRune.getPowerLevel());

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
	public abstract int getAntiMagic();

	/**
	 * Returns the defense bonus bestowed by this item when activated.
	 *
	 * @return an integer representing a defense bonus. Returns zero if the item
	 *         doesn't bestow any bonus.
	 */
	public abstract int getShield();

	@Override
	public String toString() {
		return String.format("%s[%s]", getType().name(), id);
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
		for (Combo.Entry entry : getType().getCombo().getEntries()) {
			final Action curAction = entry.getAction();

			if (curAction.equals(action)) {
				// Action found
				if (!entry.isUsable(champion)) {
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
		for (Entry entry : getType().getCombo().getEntries()) {
			if (!entry.isUsable(champion)) {
				// The champion isn't skilled enough to use this action
				continue;
			}

			// Is the action limited by a number of charges ?
			if (entry.useCharges()) {
				// What's the remaining charges ?
				final int chargeCount = charges.get(entry.getAction());

				if (chargeCount == 0) {
					// Charges depleted
					continue;
				}
			}

			// The champion can use this action
			result.add(entry.getAction());
		}

		return result;
	}

	/**
	 * Returns the category this item belongs to.
	 *
	 * @return the item category. Never returns null.
	 */
	public final Category getCategory() {
		return getType().getCategory();
	}

	/**
	 * Returns the effects caused by this item.
	 *
	 * @return a list of effects. Never returns null.
	 */
	public List<Effect> getEffects() {
		// Defensive recopy
		return new ArrayList<Item.Effect>(effects);
	}

	/**
	 * Tells whether this item is a food item.
	 *
	 * @return whether this item is a food item.
	 */
	public final boolean isFood() {
		return Category.getFoodItems().contains(getType());
	}
}