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
package fr.ritaly.dungeonmaster.projectile;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;

/**
 * A projectile created by casting a spell.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class SpellProjectile extends AbstractProjectile {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The spell which created this projectile.
	 */
	private final Spell spell;

	public SpellProjectile(Spell spell, Champion champion) {
		// TODO Compute the projectile range
		super(champion.getParty().getDungeon(), champion.getParty()
				.getPosition(), champion.getParty().getDirection(), champion
				.getSector(), spell.getDuration());

		Validate.isTrue(spell.isValid(), "The given spell <" + spell.getName()
				+ "> isn't valid");
		Validate.isTrue(spell.getType().isProjectile(), "The given spell <"
				+ spell.getName() + "> isn't a projectile spell");

		this.spell = spell;
	}

	public SpellProjectile(final Spell spell, final Dungeon dungeon,
			final Position position, final Direction direction,
			final Sector sector) {

		// TODO Compute the projectile range
		super(dungeon, position, direction, sector, spell.getDuration());

		Validate.notNull(spell, "The given spell is null");
		Validate.isTrue(spell.isValid(), "The given spell <" + spell.getName()
				+ "> isn't valid");
		Validate.isTrue(spell.getType().isProjectile(), "The given spell <"
				+ spell.getName() + "> isn't a projectile spell");

		this.spell = spell;
	}

	@Override
	protected void projectileDied() {
		// Play the projectile final sound
		// TODO This sound depends on the projectile type. Create a property 'sound' on the projectile type
		SoundSystem.getInstance().play(getPosition(), AudioClip.FIRE_BALL);

		if (Spell.Type.OPEN_DOOR.equals(spell.getType())) {
			openDoor();
		} else if (Spell.Type.FIREBALL.equals(spell.getType())) {
			fireballExplodes();
		} else if (Spell.Type.POISON_CLOUD.equals(spell.getType())) {
			poisonCloudExplodes();
		} else {
			// TODO Implement the other types of SpellProjectile
		}

		// FIXME Hit the creatures / champions
	}

	private void poisonCloudExplodes() {
		// Create a poison could where the projectile exploded
		dungeon.getElement(getPosition()).createPoisonCloud();
	}

	private void fireballExplodes() {
		// TODO Are there other spells able to destroy a door (lightning for instance) ?
		final Element currentElement = dungeon.getElement(getPosition());

		if (currentElement.getType().equals(Element.Type.DOOR)) {
			// Retrieve the door
			final Door door = (Door) currentElement;

			// Is the door already broken ?
			if (!door.isBroken()) {
				// Try destroying the door
				if (door.destroy()) {
					// The door has been destroyed
				}
			}
		} else {
			// TODO Hit the champions (if any)
		}
	}

	private void openDoor() {
		final Element currentElement = dungeon.getElement(getPosition());

		if (currentElement.getType().equals(Element.Type.DOOR)) {
			// Open or close the door
			final Door door = (Door) currentElement;

			if (Door.Motion.IDLE.equals(door.getMotion())) {
				if (Door.State.OPEN.equals(door.getState())) {
					// Close the door
					door.close();
				} else if (Door.State.CLOSED.equals(door.getState())) {
					// Open the door
					door.open();
				} else {
					// Not supposed to happen
					throw new IllegalStateException("Unexpected door state: " + door.getState());
				}
			} else if (Door.Motion.CLOSING.equals(door.getMotion())) {
				// Open the door
				door.open();
			} else if (Door.Motion.OPENING.equals(door.getMotion())) {
				// Close the door
				door.close();
			} else {
				// Not supposed to happen
				throw new IllegalStateException("Unexpected door motion: " + door.getMotion());
			}
		}
	}

//	@Override
//	public String toString() {
//		return getClass().getSimpleName() + "[id=" + getId() + ", position="
//				+ getPosition() + ", sector=" + getSector() + ", direction="
//				+ getDirection() + ", spell=" + spell.getName() + "]";
//	}
}