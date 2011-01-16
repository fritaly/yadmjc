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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.actuator.Actuator;
import fr.ritaly.dungeonmaster.actuator.Actuators;
import fr.ritaly.dungeonmaster.actuator.HasActuator;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Torch;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class TorchWall extends DirectedElement implements HasActuator {
	
	private final Log log = LogFactory.getLog(TorchWall.class);

	private Torch torch;
	
	private Actuator actuator;

	public TorchWall(Direction direction) {
		this(direction, true);
	}
	
	public TorchWall(Direction direction, boolean hasTorch) {
		super(Element.Type.ALCOVE, direction);
		
		if (hasTorch) {
			this.torch = new Torch();
		}
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
	public String getCaption() {
		return "T";
	}

	@Override
	public void validate() throws ValidationException {
	}

	public boolean hasTorch() {
		return (torch != null);
	}

	public Torch takeTorch() {
		if (hasTorch()) {
			final Torch item = this.torch;

			this.torch = null;

			if (log.isDebugEnabled()) {
				log.debug("Removed torch " + item + " from " + this);
			}
			
			if (actuator != null) {
				// Déclencher l'actuator
				Clock.getInstance().register(actuator);
			}

			return item;
		}

		return null;
	}

	public Torch putTorch(Torch torch) {
		Validate.notNull(torch, "The given torch is null");

		final Torch previous = takeTorch();

		this.torch = torch;

		if (log.isDebugEnabled()) {
			log.debug("Put torch " + torch + " on " + this);
		}
		
		if (actuator != null) {
			// Déclencher l'actuator
			Clock.getInstance().register(actuator);
		}

		// Retourner la torche qui était sur le mur
		return previous;
	}
	
	public Actuator getActuator() {
		return actuator;
	}
	
	public void addActuator(Actuator actuator) {
		Validate.notNull(actuator, "The given actuator is null");

		this.actuator = Actuators.combine(this.actuator, actuator);
	}

	public void setActuator(Actuator actuator) {
		this.actuator = actuator;
	}
	
	@Override
	public void clearActuator() {
		this.actuator = null;
	}
}