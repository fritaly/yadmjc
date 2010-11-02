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

import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.actuator.Triggered;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.ai.Creature.Height;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Party;

// FIXME Une porte peut se déclencher elle-même: implémenter HasActuator
/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Door extends OrientedElement implements ClockListener,
		Triggered {

	private final Log log = LogFactory.getLog(Door.class);

	private final Temporizer temporizer = new Temporizer("Temporizer.Door", 4);

	/**
	 * Enumération des différents degrés de résistance d'une porte.
	 */
	public static enum Resistance {
		LOW(0x2A),
		MEDIUM(0x6E),
		HIGH(0xE6),
		UNBREAKABLE(0xFF);

		private Resistance(int value) {
			this.value = value;
		}

		private final int value;

		public int getValue() {
			return value;
		}

		public boolean isBreakable() {
			return !equals(UNBREAKABLE);
		}
	}

	/**
	 * Enumération des différents styles de porte existantes.
	 */
	public static enum Style {
		/**
		 * Porte style grille.
		 */
		GRATE(Resistance.MEDIUM),

		/**
		 * Porte en bois.
		 */
		WOODEN(Resistance.LOW),

		/**
		 * Porte en fer.
		 */
		IRON(Resistance.HIGH),

		/**
		 * Porte spéciale.
		 */
		RA(Resistance.UNBREAKABLE);

		private final Resistance resistance;

		private Style(Resistance resistance) {
			if (resistance == null) {
				throw new IllegalArgumentException(
						"The given resistance is null");
			}

			this.resistance = resistance;
		}

		/**
		 * Indique si l'image affichée de la porte est inversée périodiquement.
		 * 
		 * @return si l'image affichée de la porte est inversée périodiquement.
		 */
		public boolean isAnimated() {
			return equals(RA);
		}

		/**
		 * Indique si les objets lancés peuvent passer à travers la porte.
		 * 
		 * @return si les objets lancés peuvent passer à travers la porte.
		 */
		public boolean itemsCanPassThrough() {
			return equals(GRATE);
		}

		/**
		 * Indique si les créatures peuvent voir à travers la porte.
		 * 
		 * @return si les créatures peuvent voir à travers la porte.
		 */
		public boolean creaturesCanSeeThrough() {
			return equals(GRATE) || equals(RA);
		}

		/**
		 * Retourne la résistance aux dégâts de la porte.
		 * 
		 * @return une instance de {@link Resistance}.
		 */
		public Resistance getResistance() {
			return resistance;
		}
	}

	public static enum Motion {
		IDLE,
		OPENING,
		CLOSING,
		OPENING_AFTER_REBOUND;
	}

	public static enum State {
		OPEN,
		ONE_FOURTH_OPEN,
		HALF_OPEN,
		THREE_FOURTH_OPEN,
		CLOSED,
		BROKEN;

		public boolean isTraversable(Party party) {
			Validate.isTrue(party != null, "The given party is null");

			return equals(OPEN) || equals(THREE_FOURTH_OPEN) || equals(BROKEN);
		}

		public boolean isTraversableByProjectile() {
			return equals(OPEN) || equals(THREE_FOURTH_OPEN) || equals(BROKEN);
		}

		private State nextOpeningState() {
			switch (this) {
			case OPEN:
				return this;
			case BROKEN:
				throw new UnsupportedOperationException();
			case CLOSED:
				return ONE_FOURTH_OPEN;
			case HALF_OPEN:
				return THREE_FOURTH_OPEN;
			case ONE_FOURTH_OPEN:
				return HALF_OPEN;
			case THREE_FOURTH_OPEN:
				return OPEN;

			default:
				throw new UnsupportedOperationException();
			}
		}

		private State nextClosingState() {
			switch (this) {
			case OPEN:
				return THREE_FOURTH_OPEN;
			case BROKEN:
				throw new UnsupportedOperationException();
			case CLOSED:
				return this;
			case HALF_OPEN:
				return ONE_FOURTH_OPEN;
			case ONE_FOURTH_OPEN:
				return CLOSED;
			case THREE_FOURTH_OPEN:
				return HALF_OPEN;

			default:
				throw new UnsupportedOperationException();
			}
		}

		private State next(Motion motion) {
			if (motion == null) {
				throw new IllegalArgumentException("The given motion is null");
			}

			switch (motion) {
			case IDLE:
				return this;
			case CLOSING:
				return nextClosingState();
			case OPENING:
				return nextOpeningState();
			case OPENING_AFTER_REBOUND:
				return nextOpeningState();
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	private State state;

	private Motion motion = Motion.IDLE;

	private final Style style;

	public Door(Style style, Orientation orientation) {
		this(style, orientation, State.CLOSED);
	}
	
	public Door(Style style, Orientation orientation, State state) {
		super(Element.Type.DOOR, orientation);

		Validate.notNull(style, "The given door style is null");
		Validate.notNull(state, "The given door state is null");

		this.style = style;
		this.state = state;
	}

	public boolean destroy() {
		if (isBroken()) {
			throw new IllegalStateException("The door is already broken");
		}

		if (log.isDebugEnabled()) {
			log.debug("Trying to break door ...");
		}

		// Ne peut fonctionner que si porte fermée
		if (State.CLOSED.equals(state) && Motion.IDLE.equals(motion)) {
			if (isBreakable()) {
				// TODO Force de frappe ?

				SoundSystem.getInstance().play(getPosition(),
						AudioClip.DOOR_BROKEN);

				state = State.BROKEN;

				if (log.isDebugEnabled()) {
					log.debug("Door successfully broken");
				}

				fireChangeEvent();

				return true;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Door can't be broken");
				}

				SoundSystem.getInstance().play(getPosition(), AudioClip.CLONK);

				return false;
			}
		} else {
			// Coup dans le vide
			SoundSystem.getInstance().play(getPosition(), AudioClip.SWING);

			if (log.isDebugEnabled()) {
				log.debug("Door can't be broken in its current state: " + state
						+ " and " + motion);
			}

			return false;
		}
	}

	public void open() {
		if (State.BROKEN.equals(state)) {
			// Ne rien faire
			return;
		}

		if (Motion.CLOSING.equals(motion) || Motion.IDLE.equals(motion)
				|| Motion.OPENING_AFTER_REBOUND.equals(motion)) {

			if (log.isDebugEnabled()) {
				log.debug("Opening door ...");
			}

			motion = Motion.OPENING;

			fireChangeEvent();

			// Animer la porte
			Clock.getInstance().register(this);
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (State.BROKEN.equals(state)) {
				return false;
			}
			if (Motion.IDLE.equals(motion)) {
				return false;
			}
			if (Motion.OPENING.equals(motion)) {
				// Transition d'état
				final State oldState = state;

				state = state.next(motion);

				if (log.isDebugEnabled()) {
					log.debug("Door.State: " + oldState + " -> " + state);
				}

				final boolean open = State.OPEN.equals(state);

				if (open) {
					if (log.isDebugEnabled()) {
						log.debug("Door is open");
					}

					motion = Motion.IDLE;
				}

				return !open;
			}
			if (Motion.CLOSING.equals(motion)) {
				// Transition d'état
				final State oldState = state;

				state = state.next(motion);

				if (log.isDebugEnabled()) {
					log.debug("Door.State: " + oldState + " -> " + state);
				}

				if (hasParty()) {
					if (!State.THREE_FOURTH_OPEN.equals(state)) {
						// On vérifie que l'état de la porte est cohérent
						throw new IllegalStateException("Unexpected state <"
								+ state + ">. Should be "
								+ State.THREE_FOURTH_OPEN);
					}

					// TODO Cogner la tête des héros si présents dessous (+
					// dégâts visibles) + si casque dégâts réduits

					SoundSystem.getInstance().play(AudioClip.BONG);

					// La porte rebondit
					motion = Motion.OPENING_AFTER_REBOUND;
				} else if (hasCreatures()) {
					// Frapper les monstres. On détermine la hauteur de la plus
					// grande des créatures
					final Creature.Height height = getCreatureManager()
							.getTallestCreatureHeight();

					if ((height != null)
							&& !Creature.Height.UNDEFINED.equals(height)) {

						// Hauteur définie, la porte rebondit si la hauteur
						// "matche" avec celle de la créature la plus grande
						if (State.THREE_FOURTH_OPEN.equals(state)
								&& Creature.Height.GIANT.equals(height)) {

							// FIXME Blesser les monstres

							// Jouer le son
							SoundSystem.getInstance().play(getPartyPosition(),
									AudioClip.BONG);

							// La porte rebondit
							motion = Motion.OPENING_AFTER_REBOUND;

						} else if (State.HALF_OPEN.equals(state)
								&& Creature.Height.MEDIUM.equals(height)) {

							// FIXME Blesser les monstres

							// Jouer le son
							SoundSystem.getInstance().play(getPartyPosition(),
									AudioClip.BONG);

							// La porte rebondit
							motion = Motion.OPENING_AFTER_REBOUND;

						} else if (State.ONE_FOURTH_OPEN.equals(state)
								&& Creature.Height.SMALL.equals(height)) {

							// FIXME Blesser les monstres

							// Jouer le son
							SoundSystem.getInstance().play(getPartyPosition(),
									AudioClip.BONG);

							// La porte rebondit
							motion = Motion.OPENING_AFTER_REBOUND;
						} else {
							// La porte se ferme simplement et rebondira au tour
							// d'après
						}
					} else {
						// Pas de hauteur déterminée. Survient pour les
						// créatures immatérielles
					}
				}

				final boolean closed = State.CLOSED.equals(state);

				if (closed) {
					if (log.isDebugEnabled()) {
						log.debug("Door is closed");
					}

					motion = Motion.IDLE;
				}

				return !closed;
			}
			if (Motion.OPENING_AFTER_REBOUND.equals(motion)) {
				// Transition d'état
				final State oldState = state;

				// La porte rebondit
				state = state.next(motion);

				if (log.isDebugEnabled()) {
					log.debug("Door.State: " + oldState + " -> " + state);
				}

				// La porte retombe au tour d'après
				motion = Motion.CLOSING;

				return true;
			}
		}

		// Animer tant que la porte n'est pas au repos
		return !Motion.IDLE.equals(motion);
	}

	public void close() {
		if (State.BROKEN.equals(state)) {
			// Ne rien faire
			return;
		}

		if (!Motion.CLOSING.equals(motion)) {
			motion = Motion.CLOSING;

			fireChangeEvent();

			// Animer la porte
			Clock.getInstance().register(this);
		}
	}

	public void toggle() {
		if (State.BROKEN.equals(state)) {
			// Ne rien faire
			return;
		}

		if (Motion.CLOSING.equals(motion)) {
			// Inverser le mouvement de la porte
			open();
		} else if (Motion.OPENING.equals(motion)) {
			// Inverser le mouvement de la porte
			close();
		} else if (Motion.IDLE.equals(motion)) {
			// Ne rien faire
		} else if (Motion.OPENING_AFTER_REBOUND.equals(motion)) {
			// Inverser le mouvement de la porte (en train de se fermer)
			open();
		}
	}

	@Override
	public boolean isTraversable(Party party) {
		// Le résultat dépend de l'état de la porte
		return state.isTraversable(party);
	}

	@Override
	public boolean isTraversableByProjectile() {
		// Le résultat dépend de l'état de la porte
		return state.isTraversableByProjectile();
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.isTrue(creature != null, "The given creature is null");

		final State state = getState();

		if (state.equals(State.OPEN)) {
			return true;
		}
		if (state.equals(State.BROKEN)) {
			return true;
		}

		final boolean medium = Height.MEDIUM.equals(creature.getHeight());
		final boolean small = Height.SMALL.equals(creature.getHeight());

		if (state.equals(State.THREE_FOURTH_OPEN) && (medium || small)) {
			return true;
		}
		if (state.equals(State.HALF_OPEN) && small) {
			return true;
		}

		if (creature.isImmaterial() && !Style.RA.equals(getStyle())) {
			// Les créature immatérielles ne peuvent passer les portes RA !!!
			return true;
		}

		return false;
	}

	public boolean isBreakable() {
		return style.getResistance().isBreakable();
	}

	public boolean isBroken() {
		return State.BROKEN.equals(state);
	}

	public State getState() {
		return state;
	}

	public Motion getMotion() {
		return motion;
	}

	/**
	 * Indique si l'image affichée de la porte est inversée périodiquement.
	 * 
	 * @return si l'image affichée de la porte est inversée périodiquement.
	 */
	public boolean isAnimated() {
		return style.isAnimated();
	}

	// FIXME Implémenter un objet passant à travers une grille

	/**
	 * Indique si les créatures peuvent voir à travers la porte.
	 * 
	 * @return si les créatures peuvent voir à travers la porte.
	 */
	public boolean creaturesCanSeeThrough() {
		return style.creaturesCanSeeThrough();
	}

	public Style getStyle() {
		return style;
	}

	@Override
	public String getCaption() {
		return "DR";
	}

	@Override
	public void validate() throws ValidationException {
		// Une porte doit être entourée de deux murs
		final List<Element> surroundingElements = getSurroundingElements();

		final Element element1 = surroundingElements.get(0);
		final Element element2 = surroundingElements.get(1);

		if (!element1.isConcrete()) {
			throw new ValidationException("The element at "
					+ element1.getPosition() + " must be concrete [actual="
					+ element1.getType().name() + "]");
		}
		if (!element2.isConcrete()) {
			throw new ValidationException("The element at "
					+ element2.getPosition() + " must be concrete [actual="
					+ element2.getType().name() + "]");
		}
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
			if (Motion.OPENING.equals(motion)) {
				close();
			} else if (Motion.CLOSING.equals(motion)) {
				open();
			} else if (Motion.IDLE.equals(motion)) {
				if (State.OPEN.equals(state)) {
					close();
				} else if (State.CLOSED.equals(state)) {
					open();
				} else {
					throw new UnsupportedOperationException();
				}
			} else if (Motion.OPENING_AFTER_REBOUND.equals(motion)) {
				// Inverser le mouvement de la porte (en train de se fermer)
				open();
			} else {
				throw new UnsupportedOperationException();
			}
			break;

		default:
			throw new UnsupportedOperationException();
		}
	}
}