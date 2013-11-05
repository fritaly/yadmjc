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
 * A torch. The torch is useful for providing light. The torch decays over time.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Torch extends Weapon implements ClockListener {

	private final Log log = LogFactory.getLog(Torch.class);

	/**
	 * The remaining light provided by this torch. This value decreases over
	 * time. value within range [0,255].
	 */
	private int light = Constants.MAX_LIGHT;

	/**
	 * Whether the torch is currently burning (and therefore providing light).
	 */
	private boolean burning;

	/**
	 * The name of the champion currently using the torch.
	 */
	private String owner;

	/**
	 * The temporizer managing the decay of this torch.
	 */
	private final Temporizer temporizer = new Temporizer("Torch", 4);

	/**
	 * Creates a new torch.
	 */
	public Torch() {
		super(Type.TORCH);
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			if (isBurning()) {
				final int oldLight = light;

				light--;

				if (log.isDebugEnabled()) {
					log.debug(String.format("%s.%s.Light: %d -> %d", owner, this, oldLight, light));
				}

				// TODO Limit the events raised (there are only 7 states possible for a torch)
				fireChangeEvent();
			}
		}

		// Listen as long as the torch isn't depleted
		return isBurning() && (light > 0);
	}

	@Override
	protected void putOn() {
		// Store the name of the champion (for debugging logs)
		this.owner = getBodyPart().getBody().getChampion().getName();

		// Light the torch
		light();

		// Register the torch and listen to clock ticks
		Clock.getInstance().register(this);
	}

	@Override
	public void takeOff() {
		if (burning) {
			burning = false;

			fireChangeEvent();
		}

		// Reset the name of the champion
		this.owner = null;
	}

	/**
	 * Tells whether the torch is currently burning.
	 *
	 * @return whether the torch is currently burning.
	 */
	public boolean isBurning() {
		return burning;
	}

	/**
	 * Lights the torch.
	 */
	public void light() {
		if (burning) {
			throw new IllegalStateException("The torch is already burning");
		}

		burning = true;

		fireChangeEvent();
	}

	/**
	 * Returns the remaining light for this torch as an integer.
	 *
	 * @return an integer value within range [0,255] representing the remaining
	 *         light.
	 */
	public int getLight() {
		return light;
	}
}