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
package fr.ritaly.dungeonmaster.map;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Place;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.actuator.Triggerable;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A pit. A pit can be real or virtual (i.e. just an illusion). It can also be
 * open or closed.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Pit extends FloorTile implements Triggerable {

	/**
	 * Tells whether the pit is open.
	 */
	private boolean open = true;

	/**
	 * Tells whether the pit is fake (an illusion).
	 */
	private boolean illusion;

	public Pit() {
		// Create a real pit open
		this(false, true);
	}

	public Pit(boolean fake) {
		this(fake, true);
	}

	public Pit(boolean fake, boolean open) {
		super(Type.PIT);

		this.illusion = fake;
		this.open = open;
	}

	@Override
	public boolean isTraversable(Party party) {
		return true;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		return true;
	}

	@Override
	public boolean isTraversableByProjectile() {
		return true;
	}

	@Override
	protected void afterItemAdded(Item item, Sector sector) {
		if (!isIllusion() && isOpen()) {
			// The items fall into the pit
			dropItems();
		}
	}

	@Override
	protected void afterCreatureSteppedOn(Creature creature) {
		if (!isIllusion() && isOpen()) {
			// The creatures fall into the pit
			dropCreatures();
		}
	}

	@Override
	protected void afterPartySteppedOn() {
		if (isIllusion()) {
			if (log.isDebugEnabled()) {
				log.debug("Party stepped on a fake pit");
			}
		} else {
			// It's a real pit
			if (isOpen()) {
				dropParty();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Party stepped on a closed pit");
				}
			}
		}
	}

	private void dropParty() {
		if (isReal()) {
			// Is the party falling or climbing down ?
			final boolean climbingDown = Party.State.CLIMBING_DOWN.equals(getParty().getState());

			if (climbingDown) {
				// The party's climbing down
				if (log.isDebugEnabled()) {
					log.debug("Party is climbing down ...");
				}

				// The party silently goes to the lower level
				getParty().getDungeon().moveParty(Move.DOWN, true);

				if (log.isDebugEnabled()) {
					log.debug("Party climbed down of one floor");
				}
			} else {
				// The party's falling into the pit
				if (log.isDebugEnabled()) {
					log.debug("Party stepped on an open pit. Party is falling ...");
				}

				// Keep the reference to the party (will be reset during)
				final Party party = getParty();

				// The party's falling
				getParty().getDungeon().moveParty(Move.DOWN, true, AudioClip.SHOUT);

				// FIXME Handle the fall through several stacked pits

				// Hurt the champions because of the fall
				for (Champion champion : party.getChampions(false)) {
					final Item boots = champion.getBody().getFeet().getItem();

					final int damage;

					if (boots != null) {
						// The boots protect the champion during the fall
						damage = Utils.random(7, 21);
					} else {
						// No boots, the damage is worst
						damage = Utils.random(10, 30);
					}

					champion.hit(damage);

					if (champion.isAlive()) {
						// Is the champion injured to the feet ?
						if (boots != null) {
							// Odds of being wounded: 25%
							if (Utils.random(1, 4) > 3) {
								champion.getBody().getFeet().wound();
							}
						} else {
							// Odds of being wounded: 50%
							if (Utils.random(1, 2) > 1) {
								champion.getBody().getFeet().wound();
							}
						}
					}
				}

				if (log.isDebugEnabled()) {
					log.debug("Party fell in pit");
				}
			}
		}
	}

	/**
	 * Tells whether the pit is open.
	 *
	 * @return whether the pit is open.
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Tells whether the pit is closed.
	 *
	 * @return whether the pit is closed.
	 */
	public final boolean isClosed() {
		return !isOpen();
	}

	/**
	 * Tries to open the pit and returns whether the operation succeeded.
	 *
	 * @return whether the pit was successfully opened.
	 */
	public boolean open() {
		// The result of this method doesn't depend on whether the pit is a fake
		// one
		if (!open) {
			if (log.isDebugEnabled()) {
				log.debug("Opening " + this + " ...");
			}

			this.open = true;

			if (log.isDebugEnabled()) {
				log.debug(this + " is open");
			}

			if (isReal() && hasParty()) {
				dropParty();
			}
			if (isReal() && hasCreatures()) {
				dropCreatures();
			}
			if (isReal() && hasItems()) {
				dropItems();
			}

			fireChangeEvent();

			return true;
		}

		return false;
	}

	/**
	 * Tries to close the pit and returns whether the operation succeeded.
	 *
	 * @return whether the pit was successfully closed.
	 */
	public boolean close() {
		// The result of this method doesn't depend on whether the pit is a fake
		// one
		if (open) {
			if (log.isDebugEnabled()) {
				log.debug("Closing " + this + " ...");
			}

			this.open = false;

			if (log.isDebugEnabled()) {
				log.debug(this + " is closed");
			}

			fireChangeEvent();

			return true;
		}

		return false;
	}

	/**
	 * Tells whether the pit is fake (an illusion) or real.
	 *
	 * @return whether the pit is fake (an illusion) or real.
	 */
	public boolean isIllusion() {
		return illusion;
	}

	/**
	 * Tells whether the pit is real (or an illusion).
	 *
	 * @return whether the pit is real (or an illusion).
	 */
	public final boolean isReal() {
		return !isIllusion();
	}

	@Override
	public String getSymbol() {
		return "O";
	}

	private void dropCreatures() {
		if (isReal()) {
			// The non-levitating creatures fall

			// Position on the lower level ?
			final Position targetPosition = getPosition().towards(Direction.DOWN);

			// Corresponding element ?
			final Element targetElement = getLevel().getDungeon().getElement(targetPosition);

			for (Creature creature : getCreatures()) {
				if (creature.getType().levitates()) {
					// The creature levitates and therefore can't fall
					continue;
				}

				if (log.isDebugEnabled()) {
					log.debug(creature + " is falling through " + this);
				}

				// The creature leaves the start position
				final Place place = removeCreature(creature);

				// The creature arrives on the end position
				targetElement.addCreature(creature, place);
			}
		}
	}

	private void dropItems() {
		if (isReal()) {
			// Let the items fall onto the lower level
			for (Item item : getItems()) {
				if (log.isDebugEnabled()) {
					log.debug(item + " is falling through " + this);
				}

				// Sector of this item ?
				final Sector sector = getPlace(item);

				if (sector == null) {
					throw new IllegalStateException("Unable to find place of " + item);
				}

				// The item leaves the current position
				removeItem(item);

				// What's the target position ?
				final Position targetPosition = getPosition().towards(Direction.DOWN);

				// Corresponding element ?
				final Element targetElement = getLevel().getDungeon().getElement(targetPosition);

				// The item arrives onto the lower level
				targetElement.addItem(item, sector);
			}
		}
	}

	@Override
	public void validate() throws ValidationException {
	}

	@Override
	public final void trigger(TriggerAction action) {
		Validate.notNull(action);

		if (log.isDebugEnabled()) {
			log.debug(this + " is being triggered [action=" + action + "]");
		}

		switch (action) {
		case ENABLE:
			open();
			break;
		case DISABLE:
			close();
			break;
		case TOGGLE:
			if (isOpen()) {
				close();
			} else {
				open();
			}
			break;

		default:
			throw new UnsupportedOperationException();
		}
	}
}