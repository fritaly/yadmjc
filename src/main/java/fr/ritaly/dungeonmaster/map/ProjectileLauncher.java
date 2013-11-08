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
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.actuator.Triggerable;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.projectile.Projectile;
import fr.ritaly.dungeonmaster.projectile.ProjectileFactory;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class ProjectileLauncher extends DirectedElement implements Triggerable {

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
		return (creature != null) && Materiality.IMMATERIAL.equals(creature.getMateriality());
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public String getSymbol() {
		return "L";
	}

	@Override
	public void validate() throws ValidationException {
	}

	public void trigger() {
		// On tire en fait deux projectiles en raison de la largeur du lanceur
		// et du fait qu'on ne peut tirer un projectile "au centre" !

		// Les projectile appara�ssent sur l'�l�ment d'� c�t� (fonction de la
		// direction du lanceur) !
		final Position startPosition = getPosition().towards(getDirection());

		// D�terminer les deux sectors sur lesquelles apparaissent les
		// projectiles !
		final List<Sector> sectors = Sector
				.getVisibleSectors(getDirection().getOpposite());

		final Projectile projectile1 = factory.createProjectile(getLevel()
				.getDungeon(), startPosition, getDirection(), sectors.get(0));

		final Projectile projectile2 = factory.createProjectile(getLevel()
				.getDungeon(), startPosition, getDirection(), sectors.get(1));
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