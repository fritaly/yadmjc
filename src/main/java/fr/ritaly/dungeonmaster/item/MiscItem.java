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
public class MiscItem extends Item {

	public MiscItem(Type type) {
		super(type);

		Validate.isTrue(Item.Category.MISCELLANEOUS.getTypes().contains(type)
				&& !isFood(), "The given item type " + type
				+ " isn't a misc item");
	}

	@Override
	protected BodyPart.Type getActivationBodyPart() {
		switch (getType()) {
		case GEM_OF_AGES:
		case EKKHARD_CROSS:
		case MOONSTONE:
		case THE_HELLION:
		case PENDANT_FERAL:
		case CHOKER:
		case JEWEL_SYMAL:
		case ILLUMULET:
			return BodyPart.Type.NECK;
		case MAGICAL_BOX_BLUE:
		case MAGICAL_BOX_GREEN:
		case ROPE:
		case LOCK_PICKS:
		case WATER_SKIN:
			return BodyPart.Type.WEAPON_HAND;
		case MIRROR_OF_DAWN:
		case RABBIT_FOOT:
		case CORBAMITE:
		case MAGNIFIER:
		case ZOKATHRA_SPELL:
		case BONES:
		case COMPASS:
		case ASHES:
		case COPPER_COIN:
		case SILVER_COIN:
		case GOLD_COIN:
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
		case BOULDER:
		case BLUE_GEM:
		case ORANGE_GEM:
		case GREEN_GEM:
			return null;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public int getFireShield() {
		return 0;
	}

	@Override
	public int getShield() {
		return 0;
	}
}