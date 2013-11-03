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

import java.util.EnumSet;

/**
 * Enumerates the possible combinations of {@link CarryLocation}s. Each item is
 * associated to one and only one combination which defines where the item can
 * be carried / stored / used.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum CarryLocations {
	/**
	 * The item can be stored in a chest or a pouch.
	 */
	CHEST_POUCH,

	/**
	 * The item can be stored in both hands and the back pack.
	 */
	HANDS_BACKPACK,

	/**
	 * The item can be stored in a chest, a pouch or eaten (consumable).
	 */
	CHEST_POUCH_CONSUMABLE,

	/**
	 * The item can be stored in a chest.
	 */
	CHEST,

	/**
	 * The item can be stored in the first slot of the quiver.
	 */
	QUIVER1,

	/**
	 * The item can be stored in a chest, a pouch or the first slot of the quiver.
	 */
	CHEST_POUCH_QUIVER1,

	/**
	 * The item can be stored in a chest, a pouch or the second slot of the quiver.
	 */
	CHEST_POUCH_QUIVER2,

	/**
	 * The item can be stored in a chest or the first slot of the quiver.
	 */
	CHEST_QUIVER1,

	/**
	 * The item can be stored in a chest, put on the neck or the torso.
	 */
	CHEST_NECK_TORSO,

	/**
	 * The item can be stored in a chest or put on the legs.
	 */
	CHEST_LEGS,

	/**
	 * The item can be stored in a chest or put on the feet.
	 */
	CHEST_FEET,

	/**
	 * The item can be stored in a chest or put on the torso.
	 */
	CHEST_TORSO,

	/**
	 * The item can be stored in a chest or put on the head.
	 */
	CHEST_HEAD,

	/**
	 * The item can be put on the torso.
	 */
	TORSO,

	/**
	 * The item can be put on the legs.
	 */
	LEGS,

	/**
	 * The item can be stored in a chest, a pouch or put on the neck.
	 */
	CHEST_POUCH_NECK,

	/**
	 * The item can't be stored.
	 */
	NONE;

	/**
	 * Returns all the carry locations mapped to this combination.
	 *
	 * @return a set of carry locations. Never returns null.
	 */
	public EnumSet<CarryLocation> getLocations() {
		switch (this) {
		case CHEST:
			return EnumSet.of(CarryLocation.CHEST);
		case CHEST_FEET:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.FEET);
		case CHEST_HEAD:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.HEAD);
		case CHEST_LEGS:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.LEGS);
		case CHEST_NECK_TORSO:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.NECK, CarryLocation.TORSO);
		case CHEST_POUCH:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH);
		case CHEST_POUCH_CONSUMABLE:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH, CarryLocation.CONSUMABLE);
		case CHEST_POUCH_NECK:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH, CarryLocation.NECK);
		case CHEST_POUCH_QUIVER1:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH, CarryLocation.QUIVER1);
		case CHEST_POUCH_QUIVER2:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH, CarryLocation.QUIVER2);
		case CHEST_QUIVER1:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.QUIVER1);
		case CHEST_TORSO:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.TORSO);
		case HANDS_BACKPACK:
			return EnumSet.of(CarryLocation.HANDS, CarryLocation.BACKPACK);
		case LEGS:
			return EnumSet.of(CarryLocation.LEGS);
		case NONE:
			return EnumSet.noneOf(CarryLocation.class);
		case QUIVER1:
			return EnumSet.of(CarryLocation.QUIVER1);
		case TORSO:
			return EnumSet.of(CarryLocation.TORSO);

		default:
			throw new UnsupportedOperationException();
		}
	}
}