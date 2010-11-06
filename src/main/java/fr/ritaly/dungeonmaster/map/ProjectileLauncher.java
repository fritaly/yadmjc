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

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Projectile;
import fr.ritaly.dungeonmaster.ProjectileFactory;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.actuator.Triggered;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class ProjectileLauncher extends DirectedElement implements Triggered {

	private final Log log = LogFactory.getLog(ProjectileLauncher.class);

	private final ProjectileFactory factory;

	public ProjectileLauncher(final Direction direction,
			final ProjectileFactory factory) {
		
		super(Element.Type.PROJECTILE_LAUNCHER, direction);

		Validate.notNull(factory, "The given projectile factory is null");

		this.factory = factory;
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		return (creature != null) && creature.isImmaterial();
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public String getCaption() {
		return "PL";
	}

	@Override
	public void validate() throws ValidationException {
	}

	public void trigger() {
		// On tire en fait deux projectiles en raison de la largeur du lanceur
		// et du fait qu'on ne peut tirer un projectile "au centre" !

		// Les projectile apparaîssent sur l'élément d'à côté (fonction de la
		// direction du lanceur) !
		final Position startPosition = getPosition().towards(getDirection());

		// Déterminer les deux SubCells sur lesquelles apparaissent les
		// projectiles !
		final List<SubCell> subCells = SubCell
				.getVisibleSubCells(getDirection().getOpposite());

		final Projectile projectile1 = factory.createProjectile(getLevel()
				.getDungeon(), startPosition, getDirection(), subCells.get(0));

		final Projectile projectile2 = factory.createProjectile(getLevel()
				.getDungeon(), startPosition, getDirection(), subCells.get(1));
	}

	@Override
	public final void trigger(TriggerAction action) {
		Validate.notNull(action);

		if (log.isDebugEnabled()) {
			log.debug(this + " is being triggered [action=" + action + "]");
		}

		switch (action) {
		case ENABLE:
			trigger();
			break;
		case DISABLE:
			trigger();
			break;
		case TOGGLE:
			trigger();
			break;

		default:
			throw new UnsupportedOperationException();
		}
	}
}