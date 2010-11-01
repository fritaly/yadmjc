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
package fr.ritaly.dungeonmaster.champion;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.stat.IntStat;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class PartySpells implements ClockListener {
	
	private final Log log = LogFactory.getLog(this.getClass());

	private final Party party;

	private final IntStat invisibility;
	
	private final IntStat antiMagic;
	
	private final IntStat shield;
	
	private final IntStat dispellIllusion;
	
	private final IntStat seeThroughWalls;

	private final Temporizer temporizer = new Temporizer("Party.Spells", 4);

	public PartySpells(Party party) {
		Validate.isTrue(party != null, "The given party is null");

		this.party = party;
		this.invisibility = new IntStat("Party.Spells", "Invisibility");
		this.antiMagic = new IntStat("Party.Spells", "AntiMagic");
		this.shield = new IntStat("Party.Spells", "Shield");
		this.dispellIllusion = new IntStat("Party.Spells", "DispellIllusion");
		this.seeThroughWalls = new IntStat("Party.Spells", "SeeThroughWalls");
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (invisibility.actualValue() > 0) {
				if (invisibility.dec() == 0) {
					// TODO Lever un évènement
					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.Invisibility is now inactive");
					}
				}
			}
			if (antiMagic.actualValue() > 0) {
				if (antiMagic.dec() == 0) {
					// TODO Lever un évènement
					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.AntiMagic is now inactive");
					}
				}
			}
			if (shield.actualValue() > 0) {
				if (shield.dec() == 0) {
					// TODO Lever un évènement
					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.Shield is now inactive");
					}
				}
			}
			if (dispellIllusion.actualValue() > 0) {
				if (dispellIllusion.dec() == 0) {
					// TODO Lever un évènement
					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.DispellIllusion is now inactive");
					}
				}
			}
			if (seeThroughWalls.actualValue() > 0) {
				if (seeThroughWalls.dec() == 0) {
					// TODO Lever un évènement
					if (log.isDebugEnabled()) {
						log.debug("Party.Spells.SeeThroughWalls is now inactive");
					}
				}
			}
		}

		return true;
	}

	public boolean isInvisibilityActive() {
		return (invisibility.actualValue() > 0);
	}
	
	public boolean isDispellIllusionActive() {
		return (dispellIllusion.actualValue() > 0);
	}
	
	public boolean isSeeThroughWallsActive() {
		return (seeThroughWalls.actualValue() > 0);
	}
	
	public Party getParty() {
		return party;
	}
	
	public IntStat getAntiMagic() {
		return antiMagic;
	}
	
	public IntStat getShield() {
		return shield;
	}
	
	public IntStat getInvisibility() {
		return invisibility;
	}

	public IntStat getDispellIllusion() {
		return dispellIllusion;
	}

	public IntStat getSeeThroughWalls() {
		return seeThroughWalls;
	}
}