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

import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.actuator.Triggered;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class RetractableWall extends Element implements Triggered {
	
	private final Log log = LogFactory.getLog(RetractableWall.class);

	private boolean open;

	// FIXME Lorsque le mur se rétracte, il tue les monstres ? quid des objets ?

	public RetractableWall() {
		super(Element.Type.RETRACTABLE_WALL);
	}

	public boolean isOpen() {
		return open;
	}
	
	public boolean isClosed() {
		return !open;
	}
	
	public boolean open() {
		if (isClosed()) {
			if (log.isDebugEnabled()) {
				log.debug("Opening " + this + " ...");
			}
			
			this.open = true;
			
			if (log.isDebugEnabled()) {
				log.debug(this + " open");
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean close() {
		if (isOpen()) {
			if (log.isDebugEnabled()) {
				log.debug("Closing " + this + " ...");
			}
			
			this.open = false;
			
			if (log.isDebugEnabled()) {
				log.debug(this + " closed");
			}
			
			return true;
		}
		
		return false;
	}
	
	public void toggle() {
		if (isOpen()) {
			close();
		} else {
			open();
		}
	}
	
	@Override
	public void trigger(TriggerAction action) {
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
			toggle();
			break;

		default:
			throw new UnsupportedOperationException();
		}		
	}

	@Override
	public boolean isTraversable(Party party) {
		Validate.notNull(party, "The given party is null");

		return isOpen();
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		return isOpen() || Materiality.IMMATERIAL.equals(creature.getMateriality());
	}

	@Override
	public boolean isTraversableByProjectile() {
		return isOpen();
	}

	@Override
	public String getCaption() {
		return "RW";
	}

	@Override
	public void validate() throws ValidationException {
	}
}