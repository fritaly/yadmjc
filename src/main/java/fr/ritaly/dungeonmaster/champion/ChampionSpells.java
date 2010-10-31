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

import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.stat.IntStat;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class ChampionSpells implements ClockListener {

	private final Champion champion;

	private final Temporizer temporizer;

	private final IntStat light;
	
	private final IntStat shield;

	public ChampionSpells(Champion champion) {
		Validate.isTrue(champion != null, "The given champion is null");

		this.champion = champion;
		this.light = new IntStat(champion.getName(), "Light");
		this.shield = new IntStat(champion.getName(), "Shield");
		this.temporizer = new Temporizer(champion.getName() + ".Spells", 4);
	}

	public int getLightValue() {
		return light.actualValue();
	}
	
	public IntStat getLight() {
		return light;
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (light.actualValue() > 0) {
				if (light.dec() == 0) {
					// TODO Lever un évènement
				}
			}
			if (shield.actualValue() > 0) {
				if (shield.dec() == 0) {
					// TODO Lever un évènement
				}
			}
		}

		return true;
	}
	
	public IntStat getShield() {
		return shield;
	}
}