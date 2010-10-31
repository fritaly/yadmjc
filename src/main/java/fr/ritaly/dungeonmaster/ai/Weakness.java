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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.Weapon;
import fr.ritaly.dungeonmaster.magic.Spell;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum Weakness {
	// Armes
	WEAPONS, VORPAL_BLADE, YEW_STAFF, STAFF_OF_MANAR, THE_FIRESTAFF,

	// Sorts
	POISON_BOLT, FIREBALL, LIGHTNING_BOLT, WEAKEN_NON_MATERIAL_BEINGS_SPELL,

	// Divers
	POISON_CLOUD;

	public boolean acceptsWeapon(Weapon weapon) {
		Validate.notNull(weapon, "The given weapon is null");

		switch (this) {
		case WEAPONS:
			// Toutes les armes fonctionnent
			return true;
		case VORPAL_BLADE:
			// Seule l'arme du type donné fonctionne
			return Item.Type.VORPAL_BLADE.equals(weapon.getType());
		case YEW_STAFF:
			// Seule l'arme du type donné fonctionne
			return Item.Type.YEW_STAFF.equals(weapon.getType());
		case STAFF_OF_MANAR:
			// Seule l'arme du type donné fonctionne
			return Item.Type.STAFF_OF_MANAR.equals(weapon.getType());
		case THE_FIRESTAFF:
			// Seule l'arme du type donné fonctionne
			return Item.Type.THE_FIRESTAFF_COMPLETE.equals(weapon.getType());
		default:
			return false;
		}
	}

	public boolean acceptsSpell(Spell.Type spellType) {
		Validate.notNull(spellType, "The given spell type is null");

		switch (this) {
		case FIREBALL:
			return Spell.Type.FIREBALL.equals(spellType);
		case LIGHTNING_BOLT:
			return Spell.Type.LIGHTNING_BOLT.equals(spellType);
		case POISON_BOLT:
			return Spell.Type.POISON_BOLT.equals(spellType);
		case WEAKEN_NON_MATERIAL_BEINGS_SPELL:
			return Spell.Type.WEAKEN_IMMATERIAL.equals(spellType);
		default:
			return false;
		}
	}

	public boolean acceptsPoisonCloud() {
		return equals(POISON_CLOUD);
	}
}