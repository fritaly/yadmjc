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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;

/**
 * Abstraction of {@link Projectile}.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
abstract class AbstractProjectile implements Projectile {

	/**
	 * Enumerates the possible states of a projectile. The state transitions
	 * allowed are {@link #FLYING} -> {@link #EXPLODING} -> {@link #EXPLODED}.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	private static enum State {
		/**
		 * State when the projectile is moving.
		 */
		FLYING,

		/**
		 * When the projectile hits an obstacle, a creature, a champion or
		 * disappears, the state changes from {@link #FLYING} to
		 * {@link #EXPLODING}. The end animation starts.
		 */
		EXPLODING,

		/**
		 * The state of a projectile after the end animation. That's the
		 * terminal state.
		 */
		EXPLODED;

		/**
		 * Tells whether the transition from this initial state to the given
		 * target state is allowed.
		 *
		 * @param state
		 *            the target state of the transition to validate. Can't be
		 *            null.
		 * @return whether the transition from this initial state to the given
		 *         target state is allowed.
		 */
		private boolean isTransitionAllowed(State state) {
			Validate.notNull(state, "The given state is null");

			switch (this) {
			case FLYING:
				return EXPLODING.equals(state);
			case EXPLODING:
				return EXPLODED.equals(state);
			default:
				throw new UnsupportedOperationException(String.format("The transition from %s to %s is forbidden", this, state));
			}
		}
	}

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Sequence used for generating unique identifiers for projectiles.
	 */
	private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

	/**
	 * The projectile's unique id.
	 */
	private final int id = SEQUENCE.incrementAndGet();

	/**
	 * The temporizer that manages the move of projectiles.
	 */
	private final Temporizer temporizer = new Temporizer("Projectile", 3);

	/**
	 * The direction this projectile is currently flying towards.
	 */
	private Direction direction = Direction.NORTH;

	/**
	 * The projectile's current position.
	 */
	private Position position;

	/**
	 * The projectile's current sector.
	 */
	private Sector sector;

	/**
	 * The projectile's current state. Never null.
	 */
	private State state = State.FLYING;

	/**
	 * The remaining fly distance before the projectile explodes.
	 */
	private int range;

	/**
	 * The dungeon where the projectile is.
	 */
	protected final Dungeon dungeon;

	public AbstractProjectile(final Dungeon dungeon, final Position position, final Direction direction, final Sector sector,
			int range) {

		Validate.notNull(dungeon, "The given dungeon is null");
		Validate.notNull(position, "The given position is null");
		Validate.notNull(direction, "The given direction is null");
		Validate.notNull(sector, "The given sector is null");
		Validate.isTrue(range > 0, "The given range " + range + " must be positive");

		this.dungeon = dungeon;

		// Store the start position, direction and sector
		this.position = position;
		this.direction = direction;
		this.sector = sector;

		// Store the remaining distance to travel
		this.range = range;

		// Install the projectile in the dungeon
		this.dungeon.getElement(position).addProjectile(this, sector);

		// Listen to clock ticks (to animate the projectile)
		Clock.getInstance().register(this);

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s created at %s", getId(), position));
		}
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public void setDirection(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		if (!this.direction.equals(direction)) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.Direction: %s -> %s", getId(), this.direction, direction));
			}

			this.direction = direction;
		}
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public int getRange() {
		return this.range;
	}

	private void setState(State state) {
		Validate.notNull(state, "The given state is null");

		if (this.state != state) {
			// Ensure the transition is allowed
			if (!this.state.isTransitionAllowed(state)) {
				throw new IllegalArgumentException("Transition from " + this.state + " to " + state + " is forbidden");
			}

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s.State: %s -> %s", getId(), this.state, state));
			}

			// FIXME Test a projectile entering a teleport
			// FIXME Test a projectile hitting a wall
			// FIXME Test a projectile that dies
			this.state = state;
		}
	}

	@Override
	public String getId() {
		return String.format("%s[%s]", getClass().getSimpleName(), id);
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Moving %s ...", getId()));
			}

			// The door is a special element because contrary to other elements
			// when hit by a projectile, the projectile explodes on the door and
			// possibly explodes it. For others elements, the projectile
			// explodes next to the hit element

			switch (state) {
			case FLYING: {
				// Should the projectile explode because it's hitting a closed
				// door ?
				final Element currentElement = dungeon.getElement(position);

				if (currentElement.getType().equals(Element.Type.DOOR) && !currentElement.isTraversableByProjectile()) {
					// It's a non-traversable door, the projectile explodes
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(String.format("%s is about to explode in %s", getId(), currentElement.getId()));
					}

					return true;
				}

				// The projectile moves, does the position change ?
				final boolean changesPosition = sector.changesPosition(direction);

				// What's the next position ?
				final Position targetPosition;

				if (changesPosition) {
					targetPosition = position.towards(direction);
				} else {
					targetPosition = position;
				}

				// What's the next sector ?
				final Sector targetSector = sector.towards(direction);

				final Element targetElement = dungeon.getElement(targetPosition);

				if (targetElement == null) {
					// Shouldn't happen
					throw new IllegalStateException("Unable to determine element with position " + targetPosition);
				}

				// TODO Can the poison cloud spell traverse a grate ?
				// Is this element traversable by the projectile ?
				if (!targetElement.isTraversableByProjectile() && !targetElement.getType().equals(Element.Type.DOOR)) {
					// If the target is a door, the projectile explodes on it

					// The projectile explodes on its current position
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(String.format("%s is about to explode because of facing %s", getId(), targetElement.getId()));
					}

					return true;
				}

				// --- Move the projectile --- //

				// The projectile leaves its current position
				dungeon.getElement(position).removeProjectile(this, sector);

				// The projectile moves, the remaining distance decreases
				this.position = targetPosition;
				this.sector = targetSector;

				final int backup = range;

				this.range--;

				if (log.isDebugEnabled()) {
					log.debug(String.format("%s.Range: %s -> %s [-1]", getId(), backup, + range));
				}

				// The projectile enters the new position
				targetElement.addProjectile(this, targetSector);

				// Is the target element occupied by a creature ?
				if (targetElement.getCreature(targetSector) != null) {
					// Yes, the projectile explodes
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(String.format("%s is about to explode because of facing %s", getId(), targetElement
								.getCreature(targetSector).getId()));
					}

					return true;
				}

				if (this.range == 0) {
					// The projectile can move any further, it explodes
					setState(State.EXPLODING);

					if (log.isDebugEnabled()) {
						log.debug(String.format("%s is about to explode because it wore off", getId()));
					}

					return true;
				}

				// The projectile keeps on moving
				return true;
			}
			case EXPLODING: {
				if (log.isDebugEnabled()) {
					log.debug(String.format("%s is exploding ...", getId()));
				}

				// Let the projectile operate
				projectileDied();

				setState(State.EXPLODED);

				return true;
			}
			case EXPLODED: {
				// The projectile disappears
				if (log.isDebugEnabled()) {
					log.debug(String.format("%s vanishes into thin air", getId()));
				}

				// Remove the projectile
				dungeon.getElement(position).removeProjectile(this, sector);

				return false;
			}

			default:
				throw new UnsupportedOperationException("Unsupported state " + state);
			}
		}

		return true;
	}

	/**
	 * Callback method used to notify the projectile that it exploded. This
	 * allows the projectile to "operate" on the final position.
	 */
	protected abstract void projectileDied();

	protected Sector getSector() {
		return sector;
	}
}