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
package fr.ritaly.dungeonmaster.item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Constants;
import fr.ritaly.dungeonmaster.Temporizer;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Torch extends Weapon implements ClockListener {

	public static final int TICK_COUNT = 4;

	private final Log log = LogFactory.getLog(Torch.class);

	private int light = Constants.MAX_LIGHT;

	private boolean burning;

	private String owner;

	private final Temporizer temporizer = new Temporizer("Torch", TICK_COUNT);

	public Torch() {
		super(Type.TORCH);
	}

	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (isBurning()) {
				final int oldLight = light;

				light--;

				if (log.isDebugEnabled()) {
					log.debug(owner + "." + this + ".Light: " + oldLight
							+ " -> " + light);
					// log.debug(owner + "'s torch is burning [light=" + light
					// + "] ...");
				}

				// TODO Limiter la levée des events (7 états possibles)
				fireChangeEvent();
			}
		}

		// Continuer tant que la torche n'a pas complètement brûlé
		return isBurning() && (light > 0);
	};

	@Override
	protected void putOn() {
		// Mémoriser le nom du champion
		this.owner = getBodyPart().getBody().getChampion().getName();

		// Allumer la torche
		light();

		// Enregistrer la torche
		Clock.getInstance().register(this);
	}

	public void takeOff() {
		if (burning) {
			burning = false;

			fireChangeEvent();
		}

		// Réinitialiser le nom du champion
		this.owner = null;
	}

	public boolean isBurning() {
		return burning;
	}

	public void light() {
		if (burning) {
			throw new IllegalStateException("The torch is already burning");
		}

		burning = true;

		fireChangeEvent();
	}

	public int getLight() {
		return light;
	}
}