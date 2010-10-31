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

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Projectile;
import fr.ritaly.dungeonmaster.DirectionTransform;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.actuator.Triggered;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Teleporter extends Element implements ClockListener,
		Triggered {

	public static enum State {
		ENABLED,
		DISABLED;
	}

	private final Position destination;

	private final DirectionTransform directionTransform;

	/**
	 * Indique si l'image du téléporteur doit être inversée (permet d'animer le
	 * téléporteur).
	 */
	private boolean mirrored;

	// Animation toutes les secondes
	private final Temporizer temporizer = new Temporizer("Teleporter.Animator",
			Clock.ONE_SECOND);

	/**
	 * Indique si le téléporteur est silencieux. Permet de téléporter un groupe
	 * de champions sans qu'il s'en aperçoive.
	 */
	private final boolean silent;

	private State state = State.ENABLED;

	public Teleporter(Position destination,
			DirectionTransform directionTransform, boolean silent) {

		super(Type.TELEPORTER);

		Validate.isTrue(destination != null, "The given destination is null");
		Validate.isTrue(directionTransform != null,
				"The given direction transform is null");

		this.destination = destination;
		this.directionTransform = directionTransform;
		this.silent = silent;
	}

	public Teleporter(DirectionTransform directionTransform, boolean silent) {
		super(Type.TELEPORTER);

		Validate.isTrue(directionTransform != null,
				"The given direction transform is null");

		// La destination est nulle car le téléporteur ne téléporte pas
		this.destination = null;
		this.directionTransform = directionTransform;
		this.silent = silent;
	}

	@Override
	public boolean isTraversable(Party party) {
		return true;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		// TODO Conditionner par le fait que la créature va mourir en prennant
		// le téléporteur
		return true;
	}

	@Override
	public boolean isTraversableByProjectile() {
		return true;
	}

	public boolean isEnabled() {
		return State.ENABLED.equals(state);
	}

	public boolean enable() {
		if (!isEnabled()) {
			if (log.isDebugEnabled()) {
				log.debug("Enabling " + this + " ...");
			}

			this.state = State.ENABLED;

			if (log.isDebugEnabled()) {
				log.debug(this + " is enabled");
			}

			if (hasParty()) {
				teleportParty();
			}
			if (hasProjectiles()) {
				for (Projectile projectile : getProjectiles().values()) {
					teleportProjectile(projectile);
				}
			}
			if (hasCreatures()) {
				for (Creature creature : getCreatures()) {
					teleportCreature(creature);
				}
			}

			fireChangeEvent();

			return true;
		}

		return false;
	}

	public boolean disable() {
		if (isEnabled()) {
			if (log.isDebugEnabled()) {
				log.debug("Disabling " + this + " ...");
			}

			this.state = State.DISABLED;

			if (log.isDebugEnabled()) {
				log.debug(this + " is disabled");
			}

			fireChangeEvent();

			return true;
		}

		return false;
	}

	@Override
	protected void afterPartySteppedOn() {
		super.afterPartySteppedOn();

		if (isEnabled()) {
			// Téléporter le groupe sur l'endroit cible
			if (log.isDebugEnabled()) {
				log.debug("Party stepped on an active teleporter");
			}

			teleportParty();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Party stepped on an inactive teleporter");
			}
		}
	}

	@Override
	protected void afterProjectileArrived(Projectile projectile) {
		super.afterProjectileArrived(projectile);

		if (isEnabled()) {
			// Téléporter le projectile sur l'endroit cible
			if (log.isDebugEnabled()) {
				log.debug("Projectile " + projectile.getId()
						+ " arrived on an active teleporter");
			}

			teleportProjectile(projectile);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Projectile " + projectile.getId()
						+ " arrived on an inactive teleporter");
			}
		}
	}

	private void teleportProjectile(Projectile projectile) {
		// TODO Implémenter teleportProjectile(Projectile)
	}

	@Override
	protected void afterCreatureSteppedOn(Creature creature) {
		super.afterCreatureSteppedOn(creature);

		if (isEnabled()) {
			// Téléporter la créature sur l'endroit cible
			if (log.isDebugEnabled()) {
				log.debug("Creature " + creature.getId()
						+ " arrived on an active teleporter");
			}

			teleportCreature(creature);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Creature " + creature.getId()
						+ " arrived on an inactive teleporter");
			}
		}
	}

	private void teleportCreature(Creature creature) {
		// TODO Implémenter teleportCreature(Creature)
	}

	private void teleportParty() {
		final Dungeon dungeon = getParty().getDungeon();

		if (destination != null) {
			dungeon.teleportParty(destination,
					directionTransform.transform(getParty().getLookDirection()),
					silent);
		} else {
			// Le groupe ne fait que changer de direction
			getParty().setLookDirection(
					directionTransform.transform(getParty().getLookDirection()));
		}
	}

	public Position getDestination() {
		return destination;
	}

	public boolean isSilent() {
		return silent;
	}

	@Override
	public boolean clockTicked() {
		if (isSilent()) {
			// Téléporteur invisible, pas besoin de l'animer
			return false;
		}

		if (temporizer.trigger()) {
			// On inverse l'image du téléporteur
			mirrored = !mirrored;
		}

		// Un téléporteur visible a tout le temps besoin d'être animé
		return true;
	}

	public DirectionTransform getDirectionTransform() {
		return directionTransform;
	}

	public boolean isMirrored() {
		return mirrored;
	}

	@Override
	public String getCaption() {
		return "TP";
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
			enable();
			break;
		case DISABLE:
			disable();
			break;
		case TOGGLE:
			if (isEnabled()) {
				disable();
			} else {
				enable();
			}
			break;

		default:
			throw new UnsupportedOperationException();
		}
	}
}