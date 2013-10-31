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
 * Enum�ration des diff�rentes combinaisons possible de {@link CarryLocation}.
 * Chaque item se voit associer une instance de {@link CarryLocations} ce qui
 * permet de d�terminer o� l'objet peut �tre utilis� / plac� / stock�.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum CarryLocations {
	CHEST_POUCH,
	HANDS_BACKPACK,
	CHEST_POUCH_CONSUMABLE,
	CHEST,
	QUIVER1,
	CHEST_POUCH_QUIVER1,
	CHEST_POUCH_QUIVER2,
	CHEST_QUIVER1,
	CHEST_NECK_TORSO,
	CHEST_LEGS,
	CHEST_FEET,
	CHEST_TORSO,
	CHEST_HEAD,
	TORSO,
	LEGS,
	CHEST_POUCH_NECK,
	NONE;

	/**
	 * Retourne l'ensemble des {@link CarryLocation} associ�es � cette instance
	 * de {@link CarryLocations}.
	 * 
	 * @return un {@link EnumSet} de {@link CarryLocation}. Ne retourne jamais
	 *         null.
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
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.NECK,
					CarryLocation.TORSO);
		case CHEST_POUCH:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH);
		case CHEST_POUCH_CONSUMABLE:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH,
					CarryLocation.CONSUMABLE);
		case CHEST_POUCH_NECK:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH,
					CarryLocation.NECK);
		case CHEST_POUCH_QUIVER1:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH,
					CarryLocation.QUIVER1);
		case CHEST_POUCH_QUIVER2:
			return EnumSet.of(CarryLocation.CHEST, CarryLocation.POUCH,
					CarryLocation.QUIVER2);
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