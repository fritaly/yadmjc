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
 * Une oubliette.
 *
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Pit extends Element implements Triggered {

	/**
	 * Indique si l'oubliette est ouverte.
	 */
	private boolean open = true;

	/**
	 * Indique si l'oubliette est une fausse (illusion).
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
			// Faire tomber le groupe
			if (log.isDebugEnabled()) {
				log.debug("Party stepped on an open pit. Party is falling ...");
			}
			
			// Conserver une référence vers le groupe car getParty() retourne
			// null après la chute !
			final Party party = getParty();

			// Le groupe tombe
			getParty().getDungeon().moveParty(Move.DOWN, true, AudioClip.SHOUT);
			
			// FIXME Gérer le cas de chute dans plusieurs oubliettes d'un coup!
			// Blesser les champions à cause de la chute !
			for (Champion champion : party.getChampions(false)) {
				final Item item = champion.getBody().getFeet().getItem();
				
				final int injury;
				
				if (item != null) {
					// Les bottes protègent un peu le héros de la chute
					injury = Utils.random(7, 21);
				} else {
					// Blessure maximale
					injury = Utils.random(10, 30);
				}
				
				champion.hit(injury);
				
				if (champion.isAlive()) {
					// Le champion est-il blessé aux pieds ?
					if (item != null) {
						// Champion un peu mieux protégé (25%)
						if (Utils.random(1, 4) > 3 ) {
							champion.getBody().getFeet().wound();
						}						
					} else {
						// Champion un peu moins protégé (50%)
						if (Utils.random(1, 2) > 1 ) {
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

	/**
	 * Indique si l'oubliette est ouverte.
	 * 
	 * @return si l'oubliette est ouverte.
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Indique si l'oubliette est fermée.
	 * 
	 * @return si l'oubliette est fermée.
	 */
	public final boolean isClosed() {
		return !isOpen();
	}

	/**
	 * Ouvre l'oubliette et retourne si l'opération a réussi.
	 * 
	 * @return si l'oubliette a été ouverte.
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
			if (isReal() && hasItem()) {
				dropItems();
			}

			fireChangeEvent();

			return true;
		}

		return false;
	}

	/**
	 * Ferme l'oubliette et retourne si l'opération a réussi.
	 * 
	 * @return si l'oubliette a été fermée.
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
	 * Indique si l'oubliette est une illusion (ou réelle).
	 * 
	 * @return si l'oubliette est une illusion.
	 */
	public boolean isIllusion() {
		return illusion;
	}

	/**
	 * Indique si l'oubliette est réelle (ou une illusion).
	 * 
	 * @return si l'oubliette est réelle.
	 */
	public final boolean isReal() {
		return !isIllusion();
	}

	@Override
	public String getCaption() {
		return "PT";
	}

	private void dropCreatures() {
		if (isReal()) {
			// Faire tomber les créatures qui ne volent pas !
			
			// Position cible ?
			final Position targetPosition = getPosition().towards(
					Direction.DOWN);

			// Element cible ?
			final Element targetElement = getLevel().getDungeon()
					.getElement(targetPosition);
			
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