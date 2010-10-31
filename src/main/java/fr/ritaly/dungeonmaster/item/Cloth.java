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

import fr.ritaly.dungeonmaster.champion.body.BodyPart;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Cloth extends Item {

	public Cloth(Type type) {
		super(type);

		Validate.isTrue(Item.Category.CLOTH.getTypes().contains(type),
				"The given item type " + type + " isn't a cloth");
	}

	@Override
	protected BodyPart.Type getActivationBodyPart() {
		switch (getType()) {
		case CAPE:
		case CLOAK_OF_NIGHT:
			return BodyPart.Type.NECK;
		case BARBARIAN_HIDE:
		case ROBE_LEGS:
		case FINE_ROBE_LEGS:
		case TABARD:
		case GUNNA:
		case ELVEN_HUKE:
		case LEATHER_PANTS:
		case BLUE_PANTS:
		case GHI_TROUSERS:
		case LEG_MAIL:
		case MITHRAL_MAIL:
		case LEG_PLATE:
		case POLEYN_OF_LYTE:
		case POLEYN_OF_DARC:
		case POWERTOWERS:
			return BodyPart.Type.LEGS;
		case SANDALS:
		case LEATHER_BOOTS:
		case ELVEN_BOOTS:
		case SUEDE_BOOTS:
		case HOSEN:
		case FOOT_PLATE:
		case GREAVE_OF_LYTE:
		case GREAVE_OF_DARC:
		case BOOTS_OF_SPEED:
			return BodyPart.Type.FEET;
		case ROBE_BODY:
		case FINE_ROBE_BODY:
		case KIRTLE:
		case SILK_SHIRT:
		case ELVEN_DOUBLET:
		case LEATHER_JERKIN:
		case TUNIC:
		case GHI:
		case MAIL_AKETON:
		case MITHRAL_AKETON:
		case TORSO_PLATE:
		case PLATE_OF_LYTE:
		case PLATE_OF_DARC:
		case FLAMEBAIN:
		case HALTER:
			return BodyPart.Type.TORSO;
		case CALISTA:
		case CROWN_OF_NERRA:
		case BEZERKER_HELM:
		case HELMET:
		case BASINET:
		case CASQUE_N_COIF:
		case ARMET:
		case HELM_OF_LYTE:
		case HELM_OF_DARC:
		case DEXHELM:
			return BodyPart.Type.HEAD;
		case BUCKLER:
		case HIDE_SHIELD:
		case WOODEN_SHIELD:
		case SMALL_SHIELD:
		case LARGE_SHIELD:
		case SHIELD_OF_LYTE:
		case SHIELD_OF_DARC:
			return BodyPart.Type.SHIELD_HAND;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int getFireShield() {
		switch (getType()) {
		case CAPE:
			return 1;
		case CLOAK_OF_NIGHT:
			return 1;
		case BARBARIAN_HIDE:
			return 1;
		case SANDALS:
			return 2;
		case LEATHER_BOOTS:
			return 4;
		case ROBE_BODY:
			return 0;
		case ROBE_LEGS:
			return 0;
		case FINE_ROBE_BODY:
			return 1;
		case FINE_ROBE_LEGS:
			return 1;
		case KIRTLE:
			return 1;
		case SILK_SHIRT:
			return 0;
		case TABARD:
			return 1;
		case GUNNA:
			return 1;
		case ELVEN_DOUBLET:
			return 2;
		case ELVEN_HUKE:
			return 2;
		case ELVEN_BOOTS:
			return 2;
		case LEATHER_JERKIN:
			return 3;
		case LEATHER_PANTS:
			return 3;
		case SUEDE_BOOTS:
			return 3;
		case BLUE_PANTS:
			return 2;
		case TUNIC:
			return 1;
		case GHI:
			return 1;
		case GHI_TROUSERS:
			return 1;
		case CALISTA:
			return 4;
		case CROWN_OF_NERRA:
			return 4;
		case BEZERKER_HELM:
			return 5;
		case HELMET:
			return 5;
		case BASINET:
			return 5;
		case BUCKLER:
			return 5;
		case HIDE_SHIELD:
			return 2;
		case WOODEN_SHIELD:
			return 3;
		case SMALL_SHIELD:
			return 4;
		case MAIL_AKETON:
			return 5;
		case LEG_MAIL:
			return 5;
		case MITHRAL_AKETON:
			return 7;
		case MITHRAL_MAIL:
			return 7;
		case CASQUE_N_COIF:
			return 6;
		case HOSEN:
			return 6;
		case ARMET:
			return 7;
		case TORSO_PLATE:
			return 4;
		case LEG_PLATE:
			return 4;
		case FOOT_PLATE:
			return 5;
		case LARGE_SHIELD:
			return 4;
		case HELM_OF_LYTE:
			return 5;
		case PLATE_OF_LYTE:
			return 4;
		case POLEYN_OF_LYTE:
			return 4;
		case GREAVE_OF_LYTE:
			return 5;
		case SHIELD_OF_LYTE:
			return 4;
		case HELM_OF_DARC:
			return 4;
		case PLATE_OF_DARC:
			return 4;
		case POLEYN_OF_DARC:
			return 4;
		case GREAVE_OF_DARC:
			return 4;
		case SHIELD_OF_DARC:
			return 6;
		case DEXHELM:
			return 6;
		case FLAMEBAIN:
			return 7;
		case POWERTOWERS:
			return 4;
		case BOOTS_OF_SPEED:
			return 2;
		case HALTER:
			return 3;
		default:
			return 0;
		}
	}

	@Override
	public int getShield() {
		switch (getType()) {
		case CAPE:
			return 5;
		case CLOAK_OF_NIGHT:
			return 10;
		case BARBARIAN_HIDE:
			return 4;
		case SANDALS:
			return 5;
		case LEATHER_BOOTS:
			return 25;
		case ROBE_BODY:
			return 5;
		case ROBE_LEGS:
			return 5;
		case FINE_ROBE_BODY:
			return 7;
		case FINE_ROBE_LEGS:
			return 7;
		case KIRTLE:
			return 6;
		case SILK_SHIRT:
			return 4;
		case TABARD:
			return 5;
		case GUNNA:
			return 7;
		case ELVEN_DOUBLET:
			return 11;
		case ELVEN_HUKE:
			return 13;
		case ELVEN_BOOTS:
			return 13;
		case LEATHER_JERKIN:
			return 17;
		case LEATHER_PANTS:
			return 20;
		case SUEDE_BOOTS:
			return 20;
		case BLUE_PANTS:
			return 12;
		case TUNIC:
			return 9;
		case GHI:
			return 8;
		case GHI_TROUSERS:
			return 9;
		case CALISTA:
			return 1;
		case CROWN_OF_NERRA:
			return 5;
		case BEZERKER_HELM:
			return 12;
		case HELMET:
			return 17;
		case BASINET:
			return 20;
		case BUCKLER:
			return 22;
		case HIDE_SHIELD:
			return 16;
		case WOODEN_SHIELD:
			return 20;
		case SMALL_SHIELD:
			return 35;
		case MAIL_AKETON:
			return 35;
		case LEG_MAIL:
			return 35;
		case MITHRAL_AKETON:
			return 70;
		case MITHRAL_MAIL:
			return 55;
		case CASQUE_N_COIF:
			return 25;
		case HOSEN:
			return 30;
		case ARMET:
			return 40;
		case TORSO_PLATE:
			return 65;
		case LEG_PLATE:
			return 56;
		case FOOT_PLATE:
			return 37;
		case LARGE_SHIELD:
			return 56;
		case HELM_OF_LYTE:
			return 62;
		case PLATE_OF_LYTE:
			return 125;
		case POLEYN_OF_LYTE:
			return 90;
		case GREAVE_OF_LYTE:
			return 50;
		case SHIELD_OF_LYTE:
			return 85;
		case HELM_OF_DARC:
			return 76;
		case PLATE_OF_DARC:
			return 160;
		case POLEYN_OF_DARC:
			return 101;
		case GREAVE_OF_DARC:
			return 60;
		case SHIELD_OF_DARC:
			return 54;
		case DEXHELM:
			return 54;
		case FLAMEBAIN:
			return 60;
		case POWERTOWERS:
			return 88;
		case BOOTS_OF_SPEED:
			return 16;
		case HALTER:
			return 3;
		default:
			return 0;
		}
	}
}