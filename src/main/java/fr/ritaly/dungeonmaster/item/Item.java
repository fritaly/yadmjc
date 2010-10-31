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
 * Un objet.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public abstract class Item implements ChangeEventSource {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	/**
	 * Enumération des catégories d'objets du jeu.
	 */
	public static enum Category {
		SCROLL(
			EnumSet.of(Type.SCROLL)),
		CONTAINER(
			EnumSet.of(Type.CHEST)),
		POTION(
			EnumSet.range(Type.MON_POTION, Type.EMPTY_FLASK)),
		WEAPON(
			EnumSet.range(Type.EYE_OF_TIME, Type.THE_FIRESTAFF_COMPLETE)),
		CLOTH(
			EnumSet.range(Type.CAPE, Type.HALTER)),
		MISCELLANEOUS(
			EnumSet.range(Type.COMPASS, Type.ZOKATHRA_SPELL));

		private static final EnumSet<Item.Type> FOOD_TYPES = EnumSet.range(
				Item.Type.APPLE, Item.Type.DRAGON_STEAK);

		/**
		 * {@link EnumSet} de {@link Type} contenant le type des objets associés
		 * à cette {@link Category}.
		 */
		private final EnumSet<Type> types;

		private Category(EnumSet<Type> types) {
			Validate.notNull(types, "The given enum set is null");

			this.types = types;
		}

		public EnumSet<Type> getTypes() {
			return types;
		}

		/**
		 * Retourne le {@link Type} des objets de type nourriture (un
		 * sous-ensemble de la category {@link #MISCELLANEOUS}).
		 * 
		 * @return un {@link EnumSet} de {@link Type}.
		 */
		public static EnumSet<Type> getFoodItems() {
			return FOOD_TYPES;
		}
	}

	/**
	 * Enumération des types d'objets. ATTENTION !! L'ordre des énumérations est
	 * importante car il détermine la {@link Category} de chaque type d'objet.
	 * Le nombre d'objets dans chaque {@link Category} n'est pas le même que
	 * dans la spécification de dmweb.free.fr car certains objets partagent les
	 * mêmes caractéristiques mais pas les mêmes images, du coup il y a deux
	 * valeurs distinctes d'enumération pour ces objets-là.
	 */
	public static enum Type {
		SCROLL(
			0.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_POUCH),
		// --- Chest --- //
		CHEST(
			5.0f,
			Combo.COMBO_0,
			CarryLocations.HANDS_BACKPACK),
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
		MAGIC_SHIELD_POTION(
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
			CarryLocations.QUIVER1), // Idem précédent
		FURY(
			4.7f,
			55, 20, 0, 0, 
			Combo.COMBO_10,
			CarryLocations.QUIVER1),
		RA_BLADE(
			4.7f,
			55, 20, 0, 0, 
			Combo.COMBO_10,
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.QUIVER1), // Idem précédent
		SNAKE_STAFF(
			2.1f,
			0, 3, 0, 3,
			Combo.COMBO_32,
			CarryLocations.QUIVER1),
		CROSS_OF_NETA(
			2.1f,
			0, 3, 0, 3,
			Combo.COMBO_32,
			CarryLocations.QUIVER1), // Idem précédent
		THE_CONDUIT(
			3.3f,
			0, 7, 0, 8,
			Combo.COMBO_33,
			CarryLocations.QUIVER1),
		SERPENT_STAFF(
			3.3f,
			0, 7, 0, 8,
			Combo.COMBO_33,
			CarryLocations.QUIVER1), // Idem précédent
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
			CarryLocations.CHEST_LEGS), // Idem précédent
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
			CarryLocations.CHEST_TORSO), // Idem précédent
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
			CarryLocations.CHEST), // Idem précédent
		HIDE_SHIELD(
			1.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		CRYSTAL_SHIELD(
			1.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Idem précédent
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
			CarryLocations.HANDS_BACKPACK), // Idem précédent
		HELM_OF_LYTE(
			1.7f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		HELM_OF_RA(
			2.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD), // Idem précédent
		PLATE_OF_LYTE(
			10.8f,
			Combo.COMBO_0,
			CarryLocations.TORSO),
		PLATE_OF_RA(
			12.1f,
			Combo.COMBO_0,
			CarryLocations.TORSO), // Idem précédent
		POLEYN_OF_LYTE(
			7.2f,
			Combo.COMBO_0,
			CarryLocations.LEGS),
		POLEYN_OF_RA(
			8.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS), // Idem précédent
		GREAVE_OF_LYTE(
			2.4f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		GREAVE_OF_RA(
			2.8f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET), // Idem précédent
		SHIELD_OF_LYTE(
			3.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		SHIELD_OF_RA(
			3.4f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Idem précédent
		HELM_OF_DARC(
			3.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD),
		DRAGON_HELM(
			3.5f,
			Combo.COMBO_0,
			CarryLocations.CHEST_HEAD), // Idem précédent
		PLATE_OF_DARC(
			14.1f,
			Combo.COMBO_0,
			CarryLocations.TORSO),
		DRAGON_PLATE(
			14.1f,
			Combo.COMBO_0,
			CarryLocations.TORSO), // Idem précédent
		POLEYN_OF_DARC(
			9.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS),
		DRAGON_POLEYN(
			9.0f,
			Combo.COMBO_0,
			CarryLocations.LEGS), // Idem précédent
		GREAVE_OF_DARC(
			3.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET),
		DRAGON_GREAVE(
			3.1f,
			Combo.COMBO_0,
			CarryLocations.CHEST_FEET), // Idem précédent
		SHIELD_OF_DARC(
			4.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK),
		DRAGON_SHIELD(
			4.0f,
			Combo.COMBO_41,
			CarryLocations.HANDS_BACKPACK), // Idem précédent
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
			CarryLocations.CHEST_POUCH_CONSUMABLE), // Idem précédent
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
			CarryLocations.CHEST_POUCH), // Idem précédent
		SILVER_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH), // Idem précédent
		GOLD_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH),
		GOR_COIN(
			0.1f,
			Combo.COMBO_37,
			CarryLocations.CHEST_POUCH), // Idem précédent
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
		 * Le nombre de base utilisé pour calculer combien de points de dommage
		 * une attaque cause. Pertinent uniquement pour une objet de type arme
		 * auquel cas la valeur est positive ou nulle autrement vaut -1.
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
		 * The amount of damage associated with fired projectiles. Valeur dans 
		 * l'intervalle [0-255].
		 */
		private final int shootDamage;

		/**
		 * Delta energy lost for each room the projectile travels.
		 */
		private final int deltaEnergy;

		private final CarryLocations carryLocations;

		private final float weight;

		// http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
		// FIXME Prendre en compte distance, shootDamage, deltaEnergy
		// Constructeur spécial pour les objets de type arme
		private Type(float weight, int damage, int distance, int shootDamage, 
				int deltaEnergy, Combo combo, CarryLocations carryLocations) {

			Validate.isTrue(weight >= 0.0f, "The given weight " + weight
					+ " must be positive or zero");
			Validate.isTrue(damage >= 0, "The given damage " + damage
					+ " must be positive or zero");
			Validate.isTrue(distance >= 0, "The given distance " + distance
					+ " must be positive or zero");
			Validate.isTrue(shootDamage >= 0, "The given shoot damage " + shootDamage
					+ " must be positive or zero");
			Validate.isTrue(deltaEnergy >= 0, "The given delta energy " + deltaEnergy
					+ " must be positive or zero");
			Validate.notNull(combo, "The given combo is null");
			Validate.notNull(carryLocations,
					"The given carry locations is null");

			this.weight = weight;
			this.damage = damage;
			this.distance = distance;
			this.shootDamage = shootDamage;
			this.deltaEnergy = deltaEnergy;
			this.combo = combo;
			this.carryLocations = carryLocations;
		}

		// Constructeur spécial pour les objets n'étant pas des armes
		private Type(float weight, Combo combo, CarryLocations carryLocations) {
			Validate.isTrue(weight >= 0.0f, "The given weight " + weight
					+ " must be positive or zero");
			Validate.notNull(combo, "The given combo is null");
			Validate.notNull(carryLocations,
					"The given carry locations is null");

			this.weight = weight;
			this.damage = -1;
			this.distance = -1;
			this.shootDamage = -1;
			this.deltaEnergy = -1;
			this.combo = combo;
			this.carryLocations = carryLocations;
		}

		/**
		 * Retourne la {@link Combo} associée au type d'item.
		 * 
		 * @return une instance de {@link Combo}. Ne retourne jamais null.
		 */
		public Combo getCombo() {
			return combo;
		}

		/**
		 * Retourne la {@link Category} associée à ce type d'objet.
		 * 
		 * @return une instance de {@link Category}. Ne retourne jamais null.
		 */
		public Category getCategory() {
			for (Category category : Category.values()) {
				if (category.getTypes().contains(this)) {
					return category;
				}
			}

			throw new UnsupportedOperationException();
		}

		public CarryLocations getCarryLocations() {
			return carryLocations;
		}

		public float getWeight() {
			return weight;
		}

		/**
		 * Retourne la liste des {@link Effect}s causés sur un {@link Champion}
		 * quand ce type d'objet est activé.
		 * 
		 * @return une {@link List} de {@link Effect}s. Ne retourne jamais null.
		 */
		public List<Effect> getEffects() {
			switch (this) {
			case STAFF_OF_CLAWS:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +4));
			case THE_FIRESTAFF:
				return Arrays.asList(new Effect(AffectedStatistic.ALL_SKILLS,
						+1));
			case DELTA:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +1));
			case VORPAL_BLADE:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +4));
			case THE_INQUISITOR:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +2));
			case MACE_OF_ORDER:
				return Arrays
						.asList(new Effect(AffectedStatistic.STRENGTH, +5));
			case STAFF:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +2));
			case WAND:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +1));
			case TEOWAND:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +6));
			case YEW_STAFF:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +4));
			case STAFF_OF_MANAR:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +10));
			case SNAKE_STAFF:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +8));
			case THE_CONDUIT:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +17));
			case DRAGON_SPIT:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +7));
			case SCEPTRE_OF_LYT:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +5),
						new Effect(AffectedStatistic.HEAL_SKILL, +1));
			case THE_FIRESTAFF_COMPLETE:
				return Arrays.asList(new Effect(AffectedStatistic.ALL_SKILLS,
						+2));
			case CLOAK_OF_NIGHT:
				return Arrays
						.asList(new Effect(AffectedStatistic.DEXTERITY, +8));
				// case ELVEN_BOOTS:
				// FIXME +1-14 Load
				// throw new UnsupportedOperationException();
			case CROWN_OF_NERRA:
				return Arrays.asList(new Effect(AffectedStatistic.WISDOM, +10));
			case DEXHELM:
				return Arrays.asList(new Effect(AffectedStatistic.DEXTERITY,
						+10));
			case FLAMEBAIN:
				return Arrays.asList(new Effect(AffectedStatistic.ANTI_FIRE,
						+12));
			case POWERTOWERS:
				return Arrays
						.asList(new Effect(AffectedStatistic.STRENGTH, +10));
			case GEM_OF_AGES:
				return Arrays.asList(new Effect(AffectedStatistic.HEAL_SKILL,
						+1));
			case EKKHARD_CROSS:
				return Arrays.asList(new Effect(AffectedStatistic.DEFEND_SKILL,
						+1));
			case MOONSTONE:
				return Arrays.asList(new Effect(AffectedStatistic.MANA, +3),
						new Effect(AffectedStatistic.INFLUENCE_SKILL, +3));
			case PENDANT_FERAL:
				return Arrays.asList(new Effect(AffectedStatistic.WIZARD_LEVEL,
						+1));
			case RABBIT_FOOT:
				return Arrays.asList(new Effect(AffectedStatistic.LUCK, +10));
			case JEWEL_SYMAL:
				return Arrays.asList(new Effect(AffectedStatistic.ANTI_MAGIC,
						+15));
			default:
				return Collections.emptyList();
			}
		}

		/**
		 * Retourne le nombre de base utilisé pour calculer combien de points de
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
		 * Indique si ce type d'objet est une clé.
		 * 
		 * @return si ce type d'objet est une clé.
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
			return distance;
		}

		public int getShootDamage() {
			return shootDamage;
		}

		public int getDeltaEnergy() {
			return deltaEnergy;
		}
	}

	/**
	 * Enumération des "statistiques" qui peuvent être affectées positivement ou
	 * négativement par un effet d'objet. Liste déduite de la liste des objets
	 * de Dungeon Master.
	 */
	public static enum AffectedStatistic {
		MANA,
		STRENGTH,
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
	 * Un effet d'objet qui agit sur une statistique de {@link Champion} de
	 * manière positive ou négative.
	 */
	public static final class Effect {

		private final AffectedStatistic affectedStatistic;

		private final int value;

		public Effect(AffectedStatistic affectedStatistic, int value) {
			Validate.notNull(affectedStatistic);
			Validate.isTrue(value != 0);

			this.affectedStatistic = affectedStatistic;
			this.value = value;
		}

		public AffectedStatistic getAffectedStatistic() {
			return affectedStatistic;
		}

		public int getValue() {
			return value;
		}

		/**
		 * Applique l'effet sur le {@link Champion} donné.
		 * 
		 * @param champion
		 *            un {@link Champion} sur lequel appliquer l'effet.
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
				stats.getMana().inc(value);
				break;
			case STRENGTH:
				stats.getStrength().incMax(value);
				stats.getStrength().inc(value);
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
				throw new UnsupportedOperationException();
			}
		}

		/**
		 * Annule l'effet sur le {@link Champion} donné.
		 * 
		 * @param champion
		 *            un {@link Champion} sur lequel annuler l'effet.
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
				break;
			case STRENGTH:
				stats.getStrength().dec(value);
				stats.getStrength().decMax(value);
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
				throw new UnsupportedOperationException();
			}
		}
	}

	protected Item(Type type) {
		if (type == null) {
			throw new IllegalArgumentException("The given item type is null");
		}

		this.type = type;

		// Mémoriser le nombre de charges associées à l'objet pour diminuer
		// celui-ci quand il est utilisé
		Map<Action, Integer> map = null;

		for (Combo.Entry entry : type.getCombo().getEntries()) {
			if (entry.useCharges()) {
				// Cette action est limitée par le nombre de charges
				if (map == null) {
					// Instancier la Map uniquement si c'est nécessaire
					map = new EnumMap<Action, Integer>(Action.class);
				}

				// FIXME Comment savoir le nombre de charges associées à chaque
				// action ? Cela dépend de chaque type d'objet...
				map.put(entry.getAction(), 3);
			}
		}

		if ((map != null) && !map.isEmpty()) {
			this.charges = map;
		} else {
			this.charges = null;
		}

		// Mémoriser les effets de l'objet
		this.effects = type.getEffects();
	}

	private final Type type;

	private final int id = SEQUENCE.incrementAndGet();

	/**
	 * {@link Map} contenant le nombre de charges restantes pour l'action donnée
	 * de l'objet. Vaut null si aucune charge autorisée.
	 */
	private final Map<Action, Integer> charges;

	/**
	 * L'envoûtement associé à l'objet.
	 */
	private Curse curse;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * La force du poison de l'objet.
	 */
	private PowerRune poisonStrength;

	/**
	 * Les effets générés par un {@link Item} (s'il y a lieu).
	 */
	private final List<Effect> effects;

	public final Type getType() {
		return type;
	}

	/**
	 * Indique si l'item peut être consommé (mangé ou bu).
	 * 
	 * @return si l'item peut être consommé (mangé ou bu).
	 */
	public final boolean isConsumable() {
		return getCarryLocations().contains(CarryLocation.CONSUMABLE);
	}

	/**
	 * Retourne le poids de l'objet.
	 * 
	 * @return le poids de l'objet.
	 */
	public float getWeight() {
		// L'implémentation par défaut retourne le "poids de base" de l'objet.
		// Si celui-ci varie en fonction de certaines conditions (cf fioles
		// vides / remplies), c'est à la sous-classe de surcharger cette méthode
		// pour calculer le poids réel de l'objet en fonction de son état !
		return getType().getWeight();
	}

	/**
	 * Retourne la partie du corps du {@link Champion} sur lequel l'objet doit
	 * être placé afin d'être "activé". Peut retourner null s'il n'y en a
	 * aucune. Exemple: retourne NECK pour une amulette, WEAPON_HAND pour une
	 * arme, etc.
	 * 
	 * @return une instance de
	 *         {@link fr.ritaly.dungeonmaster.champion.body.BodyPart.Type} ou
	 *         null.
	 */
	protected abstract BodyPart.Type getActivationBodyPart();

	/**
	 * Indique si cet {@link Item} est activé par la partie du corps donnée.
	 * 
	 * @param bodyPart
	 *            une instance de {@link BodyPart}.
	 * @return si cet {@link Item} est activé par la partie du corps donnée.
	 */
	public final boolean isActivatedBy(BodyPart bodyPart) {
		Validate.notNull(bodyPart, "The given body part is null");

		// Le pied de lapin est activé par les deux mains ou l'inventaire !!!
		return bodyPart.getType().equals(getActivationBodyPart())
				|| (Item.Type.RABBIT_FOOT.equals(getType()) && (bodyPart
						.getType().equals(BodyPart.Type.WEAPON_HAND) || bodyPart
						.getType().equals(BodyPart.Type.SHIELD_HAND)));
	}

	/**
	 * Retourne l'ensemble de {@link CarryLocation} associées à cet {@link Item}
	 * .
	 * 
	 * @return un {@link EnumSet} de {@link CarryLocation}. Ne retourne jamais
	 *         null.
	 */
	public final EnumSet<CarryLocation> getCarryLocations() {
		return getType().getCarryLocations().getLocations();
	}

	/**
	 * Indique si l'objet est envoûté.
	 * 
	 * @return si l'objet est envoûté.
	 */
	public boolean isCursed() {
		return (curse != null) && curse.isActive();
	}

	/**
	 * Indique si l'envoûtement de l'objet a été détecté. Ne retourne true que
	 * si l'objet est actuellement envoûté.
	 * 
	 * @return si l'envoûtement de l'objet a été détecté.
	 */
	public boolean isCurseDetected() {
		return isCursed() && curse.isDetected();
	}

	/**
	 * Envoûte (ou renforce l'envoûtement de) l'objet avec la puissance donnée.
	 * 
	 * @param powerRune
	 *            un {@link PowerRune} représentant la force de l'envoûtement.
	 */
	public void curse(PowerRune powerRune) {
		Validate.isTrue(powerRune != null, "The given power rune is null");

		if (curse == null) {
			curse = new Curse();
		}

		final boolean wasActive = curse.isActive();

		curse.curse(powerRune.getPowerLevel());

		if (!wasActive && curse.isActive()) {
			// La chance du champion qui porte l'objet diminue de +3
			if (champion != null) {
				champion.getStats().getLuck().dec(3);
			}

			fireChangeEvent();
		}
	}

	/**
	 * Conjure l'envoûtement de l'objet avec la puissance donnée.
	 * 
	 * @param powerRune
	 *            un {@link PowerRune} représentant la force de conjuration.
	 */
	public void conjure(PowerRune powerRune) {
		Validate.isTrue(powerRune != null, "The given power rune is null");

		if (curse != null) {
			final boolean wasActive = curse.isActive();

			curse.conjure(powerRune);

			if (wasActive && !curse.isActive()) {
				// La chance du champion qui porte l'objet remonte de +3
				if (champion != null) {
					champion.getStats().getLuck().inc(3);
				}

				fireChangeEvent();

				curse = null;
			}
		}
	}

	/**
	 * Indique si l'objet quand il est porté (revêtu par un champion) peut être
	 * retiré. La méthode pourrait aussi s'appeler isRemovable(). La méthode ne
	 * tente pas de retirer l'objet juste de savoir si c'est possible.
	 * 
	 * @return si l'objet quand il est porté (revêtu par un champion) peut être
	 *         retiré.
	 */
	public boolean tryRemove() {
		if (isCursed()) {
			// On ne peut retirer un objet envouté
			if (!curse.isDetected()) {
				curse.setDetected(true);

				fireChangeEvent();
			}

			return false;
		}

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

	/**
	 * Notifie les {@link ChangeListener} que l'objet vient de changer d'état
	 * afin de permettre de le redessiner.
	 */
	protected final void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	/**
	 * Le champion qui tient l'item (s'il y en a un).
	 */
	private Champion champion;

	/**
	 * La partie du corps sur lequel l'objet est porté (s'il y en a une).
	 */
	private BodyPart bodyPart;

	/**
	 * Retourne le {@link Champion} porteur de l'objet.
	 * 
	 * @return un {@link Champion} ou null.
	 */
	protected final Champion getCarrier() {
		return champion;
	}

	/**
	 * Retourne la partie du corps qui porte l'objet.
	 * 
	 * @return une {@link BodyPart} ou null.
	 */
	protected final BodyPart getBodyPart() {
		return bodyPart;
	}

	/**
	 * Notifie l'objet qu'il vient d'être placé / activé sur la partie du corps
	 * passée en paramètre. Permet d'activer l'objet et de mémoriser la
	 * {@link BodyPart} ainsi que le {@link Champion} associé.
	 * 
	 * @param bodyPart
	 *            une {@link BodyPart} sur laquelle l'objet vient d'être placé.
	 */
	public final void itemPutOn(BodyPart bodyPart) {
		Validate.isTrue(bodyPart != null, "The given body part is null");
		if (this.bodyPart != null) {
			// L'objet ne doit pas déjà être portée par un champion
			throw new IllegalStateException(this + " is already worn by "
					+ this.bodyPart);
		}
		if (this.champion != null) {
			// L'objet ne doit pas déjà être portée par un champion
			throw new IllegalStateException(this + " is already worn by "
					+ this.champion.getName());
		}

		// Mémoriser le champion qui porte l'objet
		this.champion = bodyPart.getBody().getChampion();
		this.bodyPart = bodyPart;

		if (this instanceof DirectionChangeListener) {
			// Ecouter les changements de direction
			this.champion.getParty().addDirectionChangeListener(
					(DirectionChangeListener) this);
		}

		// Si l'objet est ensorcelé, la chance diminue de 3 points
		if (isCursed()) {
			champion.getStats().getLuck().dec(3);
		}

		if (hasEffects() && (bodyPart != null) && isActivatedBy(bodyPart)) {
			// Appliquer les effets de l'objet sur le champion !
			for (Effect effect : effects) {
				effect.affect(champion);
			}
		}

		// Callback
		putOn();
	}

	public boolean hasEffects() {
		return !effects.isEmpty();
	}

	/**
	 * Méthode de callback à surcharger dans les sous-classes pour implémenter
	 * un comportement spécial quand l'objet est porté / "activé".
	 */
	protected void putOn() {
		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + "." + bodyPart.getType().getLabel()
					+ ".Item: [+] " + this);
			// log.debug("Item " + this + " put on " + champion.getName() +
			// "'s "
			// + bodyPart.getType());
		}
	}

	/**
	 * Notifie l'objet qu'il vient d'être retiré / désactivé de la partie du
	 * corps passée en paramètre.
	 */
	public final void itemTakenOff() {
		// Callback
		takeOff();

		if (hasEffects() && (bodyPart != null) && isActivatedBy(bodyPart)) {
			// Annuler les effets de l'objet sur le champion !
			for (Effect effect : effects) {
				effect.unaffect(champion);
			}
		}

		// Si l'objet est ensorcelé, la chance remonte de 3 points
		if (isCursed()) {
			champion.getStats().getLuck().inc(3);
		}

		if (this instanceof DirectionChangeListener) {
			// Supprimer le listener
			this.champion.getParty().removeDirectionChangeListener(
					(DirectionChangeListener) this);
		}

		// Réinitialiser le champion qui porte l'objet
		this.champion = null;
		this.bodyPart = null;
	}

	/**
	 * Méthode de callback à surcharger dans les sous-classes pour implémenter
	 * un comportement spécial quand l'objet est retiré / "désactivé".
	 */
	protected void takeOff() {
		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + "." + bodyPart.getType().getLabel()
					+ ".Item: [-] " + this);
			// log.debug("Item " + this + " taken off " + champion.getName()
			// + "'s " + bodyPart.getType());
		}
	}

	/**
	 * Indique si l'objet est actuellement activé.
	 * 
	 * @return si l'objet est actuellement activé.
	 */
	public final boolean isActivated() {
		// On est objet est activé quand il est porté par un champion et sur la
		// bonne partie du corps !
		return (champion != null);
	}

	/**
	 * Retourne le bonus de résistance au feu conféré par l'objet.
	 * 
	 * @return un entier positif ou nul représentant un bonus de résistance au
	 *         feu.
	 */
	public abstract int getFireShield();

	/**
	 * Retourne le bonus de défense conféré par l'objet.
	 * 
	 * @return un entier positif ou nul représentant un bonus de défense.
	 */
	public abstract int getShield();

	@Override
	public String toString() {
		// Le nom de l'item est donné par son type
		return getType().name() + "[" + id + "]";
	}

	/**
	 * Indique si l'objet est empoisonné. Permet d'empoisonner de la nourriture
	 * par exemple (ce qui est différent du poison d'une flèche).
	 * 
	 * @return si l'objet est empoisonné.
	 */
	public boolean isPoisoned() {
		return (poisonStrength != null);
	}

	/**
	 * Retourne la force de l'empoisonnement induit par l'objet (s'il y en a
	 * un).
	 * 
	 * @return un {@link PowerRune} représentant la force du poison ou null si
	 *         l'objet n'est pas empoisonné.
	 */
	public PowerRune getPoisonStrength() {
		// Force de l'empoisonnement ? null si pas empoisonné
		return poisonStrength;
	}

	public void setPoisonStrength(PowerRune strength) {
		// Le paramètre peut être null
		this.poisonStrength = strength;
	}

	/**
	 * Notifie l'objet qu'il vient d'être consommé (mangé ou bu) par le
	 * {@link Champion} donné et retourne l'objet tel quel si l'opération a
	 * échoué ou si l'opération a réussi, l'objet dans lequel il s'est
	 * transformé ou null si l'objet a été détruit par l'opération.
	 * 
	 * @param champion
	 *            un {@link Champion} représentant le champion qui a consommé
	 *            l'objet.
	 * @return une instance de {@link Item} ou null.
	 */
	public final Item itemConsumed(Champion champion) {
		Validate.isTrue(champion != null, "The given champion is null");

		if (isConsumable()) {
			return consume(champion);
		}

		// Objet non consommable, retourné tel quel
		return this;
	}

	/**
	 * Méthode de callback à surcharger dans les classes filles si l'objet peut
	 * être consommé (mangé ou bu).
	 * 
	 * @param champion
	 *            le {@link Champion} qui consomme l'objet.
	 * @return l'objet retourné à l'issu de l'opération.
	 */
	protected Item consume(Champion champion) {
		Validate.notNull(champion, "The given champion is null");
		if (!isConsumable()) {
			throw new UnsupportedOperationException("Item " + getType().name()
					+ " isn't consumable");
		}

		// Tout item consommable doit surcharger cette méthode, s'il oublie de
		// le faire la levée de UOE nous le mettra en évidence
		throw new UnsupportedOperationException();
	}

	/**
	 * Utilise l'action donnée de l'objet. Celui-ci doit être actuellement porté
	 * par un {@link Champion}.
	 * 
	 * @param action
	 *            une {@link Action} à utiliser.
	 * @return si l'opération a réussi.
	 */
	public boolean perform(Action action) {
		Validate.isTrue(action != null, "The given action is null");

		if (champion == null) {
			throw new IllegalStateException(
					"There is no champion holding this item");
		}
		// L'objet doit être dans la bonne main du champion !
		if (!champion.getBody().getWeaponHand().getItem().equals(this)) {
			throw new UnsupportedOperationException("This item isn't held in "
					+ champion.getName() + "'s weapon hand");
		}

		for (Combo.Entry entry : getType().getCombo().getEntries()) {
			final Action curAction = entry.getAction();

			if (curAction.equals(action)) {
				if (!entry.isUsable(champion)) {
					// Le champion ne peut pas l'utiliser (pas assez fort)
					return false;
				}

				// Utiliser l'action (rend la main du champion indisponible)
				action.perform(champion.getParty().getDungeon(), champion);

				return true;
			}
		}

		throw new IllegalArgumentException("The given action <" + action
				+ "> isn't among this item's actions");
	}

	/**
	 * Retourne la liste des {@link Action}s autorisées par l'objet pour le
	 * champion donné. Cette liste dépend de la {@link Combo} qui lui est
	 * associée et des compétences du {@link Champion}.
	 * 
	 * @param champion
	 *            le champion qui veut utiliser les actions de l'objet.
	 * @return une List&lt;Action&gt;.
	 */
	public final List<Action> getActions(Champion champion) {
		Validate.isTrue(champion != null, "The given champion is null");

		final List<Action> actions = new ArrayList<Action>();

		// Ne retourner que les actions utilisables par le champion
		for (Entry entry : getType().getCombo().getEntries()) {
			if (!entry.isUsable(champion)) {
				// Champion pas assez compétent pour utiliser l'action
				continue;
			}

			// L'action est-elle limitée par un nombre de charges ?
			if (entry.useCharges()) {
				// Nombre de charges restantes
				final int chargeCount = charges.get(entry.getAction());

				if (chargeCount == 0) {
					// Plus de charges disponibles
					continue;
				}
			}

			// L'action peut être utilisée par le champion
			actions.add(entry.getAction());
		}

		return actions;
	}

	/**
	 * Retourne la {@link Category} à laquelle appartient ce type d'objet.
	 * 
	 * @return une instance de {@link Category}. Ne retourne jamais null.
	 */
	public final Category getCategory() {
		return getType().getCategory();
	}

	/**
	 * Retourne les effets générés par cet {@link Item}.
	 * 
	 * @return une {@link List} de {@link Effect}. Ne retourne jamais null.
	 */
	public List<Effect> getEffects() {
		// Recopie défensive
		return new ArrayList<Item.Effect>(effects);
	}

	/**
	 * Indique si cet {@link Item} représente de la nourriture.
	 * 
	 * @return si cet {@link Item} représente de la nourriture.
	 */
	public final boolean isFood() {
		return Category.getFoodItems().contains(getType());
	}
}