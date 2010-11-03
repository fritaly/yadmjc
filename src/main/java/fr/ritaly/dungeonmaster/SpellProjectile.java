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
package fr.ritaly.dungeonmaster;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;

/**
 * Un projectile créé à l'aide d'un {@link Spell}.
 *
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class SpellProjectile implements Projectile {

	private static enum State {
		FLYING,
		EXPLODING,
		EXPLODED;

		private boolean isTransitionAllowed(State state) {
			switch (this) {
			case FLYING:
				return EXPLODING.equals(state);
			case EXPLODING:
				return EXPLODED.equals(state);
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	private final Log log = LogFactory.getLog(SpellProjectile.class);

	private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

	private final int id = SEQUENCE.incrementAndGet();

	private final Temporizer temporizer = new Temporizer("Projectile", 3);

	private Direction direction = Direction.NORTH;

	private final Spell spell;

	private Position position;

	/**
	 * {@link SubCell} où est situé le projectile dans la position actuelle.
	 */
	private SubCell subCell;

	private State state = State.FLYING;

	/**
	 * La distance restante à parcourir par le projectile avant de disparaître
	 */
	private int range;

	private final Dungeon dungeon;

	public SpellProjectile(Spell spell, Champion champion) {
		Validate.notNull(spell, "The given spell is null");
		Validate.isTrue(spell.isValid(), "The given spell <" + spell.getName()
				+ "> isn't valid");
		Validate.isTrue(spell.getType().isProjectile(), "The given spell <"
				+ spell.getName() + "> isn't a projectile spell");
		Validate.notNull(champion, "The given champion is null");
		Validate.notNull(champion.getParty(),
				"The given champion didn't join a party");

		this.spell = spell;
		this.dungeon = champion.getParty().getDungeon();

		// Mémoriser la position de départ du projectile
		this.position = champion.getParty().getPosition();

		// ... sa direction
		this.direction = champion.getParty().getDirection();

		// ... et son emplacement
		this.subCell = champion.getLocation().toSubCell(direction);

		// On mémorise la distance restante à parcourir par le projectile avant
		// de disparaître TODO Distance à calculer
		this.range = spell.getDuration();

		// Installer le projectile dans le donjon
		champion.getParty().getDungeon().getElement(position)
				.projectileArrived(this, subCell);

		// Enregistrer le projectile
		Clock.getInstance().register(this);

		if (log.isDebugEnabled()) {
			log.debug(getId() + " created at " + position);
		}
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public void setDirection(Direction direction) {
		Validate.notNull(direction, "The given diretcion is null");

		this.direction = direction;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	public int getRange() {
		return this.range;
	}

	private void setState(State state) {
		Validate.notNull(state, "The given state is null");

		if (this.state != state) {
			// On vérifie que la transition est autorisée
			if (!this.state.isTransitionAllowed(state)) {
				throw new IllegalArgumentException("Transition from "
						+ this.state + " to " + state + " is forbidden");
			}

			if (log.isDebugEnabled()) {
				log.debug(getId() + ".State: " + this.state + " -> " + state);
			}

			// FIXME Tester projectile dans un téléporteur
			// FIXME Tester projectile dans un mur
			// FIXME Tester projectile qui "meurt"
			this.state = state;
		}
	}

	public String getId() {
		return getClass().getSimpleName() + "[" + id + "]";
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (log.isDebugEnabled()) {
				log.debug("Moving " + getId() + " ...");
			}

			switch (state) {
			case FLYING: {
				// Le projectile doit-il exploser car il est situé sur une porte
				// fermée ?
				final Element currentElement = dungeon.getElement(position);

				if (currentElement.getType().equals(Element.Type.DOOR)
						&& !currentElement.isTraversableByProjectile()) {

					// C'est une porte non traversable par le projectile,
					// celui-ci explose
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(getId() + " is about to explode in "
								+ currentElement.getId());
					}

					return true;
				}

				// Le projectile "avance". Change-t-il de position ?
				final boolean changesPosition = subCell
						.changesPosition(direction);

				// Futur emplacement ?
				final Position targetPosition;

				if (changesPosition) {
					targetPosition = position.towards(direction);
				} else {
					targetPosition = position;
				}

				// Subcell cible ?
				final SubCell targetSubCell = subCell.towards(direction);

				final Element targetElement = dungeon
						.getElement(targetPosition);

				if (targetElement == null) {
					// Ne doit pas arriver
					throw new IllegalStateException(
							"Unable to determine element with position "
									+ targetPosition);
				}

				// Emplacement traversable par un projectile ?
				if (!targetElement.isTraversableByProjectile()
						&& !targetElement.getType().equals(Element.Type.DOOR)) {

					// S'il la cible est une porte, le projectile explose dans
					// celle-ci pas à côté !

					// Le projectile explose à sa position actuelle
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(getId()
								+ " is about to explode because of "
								+ targetElement.getId());
					}

					return true;
				}

				// --- Déplacer le projectile --- //

				// Le projectile quitte sa position actuelle
				dungeon.getElement(position).projectileLeft(this, subCell);

				// Le projectile avance, la distance restante diminue
				this.position = targetPosition;
				this.subCell = targetSubCell;
				this.range--;

				// Il arrive sur la nouvelle position
				targetElement.projectileArrived(this, targetSubCell);

				// L'emplacement est-il occupé par une créature ?
				if (targetElement.getCreature(targetSubCell) != null) {
					// Oui, le projectile explose
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(getId()
								+ " is about to explode because of "
								+ targetElement.getCreature(targetSubCell)
										.getId());
					}

					return true;
				}

				if (this.range == 0) {
					// Le projectile est arrivé en bout de course, il explose
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(getId()
								+ " is about to explode because it wore off");
					}

					return true;
				}

				// Le projectile continue de voler
				return true;
			}
			case EXPLODING: {
				if (log.isDebugEnabled()) {
					log.debug(getId() + " is exploding ...");
				}

				// Jouer le son TODO Le son varie selon le type de projectile 
				SoundSystem.getInstance().play(position, AudioClip.FIRE_BALL);
				
				if (Spell.Type.OPEN_DOOR.equals(spell.getType())) {
					final Element currentElement = dungeon.getElement(position);

					if (currentElement.getType().equals(Element.Type.DOOR)) {
						// Ouvrir ou fermer la porte
						final Door door = (Door) currentElement;

						if (Door.Motion.IDLE.equals(door.getMotion())) {
							if (Door.State.OPEN.equals(door.getState())) {
								// Fermer la porte
								door.close();
							} else if (Door.State.CLOSED
									.equals(door.getState())) {
								// Ouvrir la porte
								door.open();
							} else {
								// Pas géré
								throw new IllegalStateException(
										"Unexpected door state: "
												+ door.getState());
							}
						} else if (Door.Motion.CLOSING.equals(door.getMotion())) {
							// Ouvrir la porte
							door.open();
						} else if (Door.Motion.OPENING.equals(door.getMotion())) {
							// Fermer la porte
							door.close();
						} else {
							// Pas géré
							throw new IllegalStateException(
									"Unexpected door motion: "
											+ door.getMotion());
						}
					}
				}

				// FIXME Appliquer les dégâts aux créatures / champions / porte
				// ou créer nuage de poison

				setState(State.EXPLODED);

				return true;
			}
			case EXPLODED: {
				// Le projectile disparaît
				if (log.isDebugEnabled()) {
					log.debug(getId() + " vanishes into thin air");
				}

				// Le projectile disparaît du donjon
				dungeon.getElement(position).projectileLeft(this, subCell);

				return false;
			}

			default:
				throw new UnsupportedOperationException();
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id=" + id + ", position="
				+ position + ", subCell=" + subCell + ", direction="
				+ direction + ", spell=" + spell.getName() + "]";
	}
}