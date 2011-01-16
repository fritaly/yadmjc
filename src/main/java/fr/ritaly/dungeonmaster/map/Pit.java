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
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.actuator.Triggered;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A pit. A pit can be real or virtual (i.e. just an illusion). It can also be
 * open or closed.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Pit extends Element implements Triggered {

	/**
	 * Tells whether the pit is open.
	 */
	private boolean open = true;

	/**
	 * Tells whether the pit is fake (an illusion).
	 */
	private boolean illusion;

	public Pit() {
		// Vraie oubliette ouverte
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
	protected void afterItemDropped(Item item, SubCell subCell) {
		if (!isIllusion() && isOpen()) {
			// Les objets tombent dans l'oubliette
			dropItems();
		}
	}

	@Override
	protected void afterCreatureSteppedOn(Creature creature) {
		if (!isIllusion() && isOpen()) {
			// Les créatures tombent dans l'oubliette
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
			// C'est une vraie oubliette
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
			// Le groupe descend-t-il en corde ou tombe-t-il ?
			final boolean climbingDown = Party.State.CLIMBING_DOWN
					.equals(getParty().getState());
			
			if (climbingDown) {
				// Le groupe descend en corde
				if (log.isDebugEnabled()) {
					log.debug("Party is climbing down ...");
				}

				// Conserver une référence vers le groupe car getParty() 
				// retourne null après la chute !
				final Party party = getParty();

				// Le groupe descend (silencieusement)
				getParty().getDungeon().moveParty(Move.DOWN, true);

				if (log.isDebugEnabled()) {
					log.debug("Party climbed down of one floor");
				}
			} else {
				// Le groupe tombe dans l'oubliette
				
				// Faire tomber le groupe
				if (log.isDebugEnabled()) {
					log.debug("Party stepped on an open pit. Party is falling ...");
				}

				// Conserver une référence vers le groupe car getParty() 
				// retourne null après la chute !
				final Party party = getParty();

				// Le groupe tombe
				getParty().getDungeon().moveParty(Move.DOWN, true,
						AudioClip.SHOUT);

				// FIXME Gérer le cas de chute dans plusieurs oubliettes d'un 
				// coup!
				
				// Blesser les champions à cause de la chute !
				for (Champion champion : party.getChampions(false)) {
					final Item item = champion.getBody().getFeet().getItem();

					final int damage;

					if (item != null) {
						// Les bottes protègent un peu le héros de la chute
						damage = Utils.random(7, 21);
					} else {
						// Blessure maximale
						damage = Utils.random(10, 30);
					}

					champion.hit(damage);

					if (champion.isAlive()) {
						// Le champion est-il blessé aux pieds ?
						if (item != null) {
							// Champion un peu mieux protégé (25%)
							if (Utils.random(1, 4) > 3) {
								champion.getBody().getFeet().wound();
							}
						} else {
							// Champion un peu moins protégé (50%)
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
		// Le résultat de cette méthode ne dépend pas du caractère fake de
		// l'oubliette
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
		// Le résultat de cette méthode ne dépend pas du caractère fake de
		// l'oubliette
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
	public String getCaption() {
		return "O";
	}

	private void dropCreatures() {
		if (isReal()) {
			// Faire tomber les créatures qui ne volent pas !

			// Position cible ?
			final Position targetPosition = getPosition().towards(
					Direction.DOWN);

			// Element cible ?
			final Element targetElement = getLevel().getDungeon().getElement(
					targetPosition);

			for (Creature creature : getCreatures()) {
				if (creature.getType().levitates()) {
					// La créature ne peut tomber dans l'oubliette car elle vole
					continue;
				}

				if (log.isDebugEnabled()) {
					log.debug(creature + " is falling through " + this);
				}

				// La créature quitte la position
				final Object location = removeCreature(creature);

				// La créature tombe au niveau inférieur
				targetElement.addCreature(creature, location);
			}
		}
	}

	private void dropItems() {
		if (isReal()) {
			// Faire tomber les objets au niveau inférieur
			for (Item item : getItems()) {
				if (log.isDebugEnabled()) {
					log.debug(item + " is falling through " + this);
				}

				// Emplacement de l'objet ?
				final SubCell subCell = getSubCell(item);

				if (subCell == null) {
					throw new IllegalStateException("Unable to find place of "
							+ item);
				}

				// L'objet quitte la position
				itemPickedUp(item);

				// Position cible ?
				final Position targetPosition = getPosition().towards(
						Direction.DOWN);

				// Element cible ?
				final Element targetElement = getLevel().getDungeon()
						.getElement(targetPosition);

				// L'objet tombe au niveau inférieur
				targetElement.itemDroppedDown(item, subCell);
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