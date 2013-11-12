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
package fr.ritaly.dungeonmaster.ai;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.Utils;

/**
 * Custom implementation of {@link Materializer} that changes the materiality
 * over time with random durations between changes.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class RandomMaterializer implements Materializer {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The temporizer responsible for counting down before changing the
	 * materiality.
	 */
	private Temporizer temporizer;

	/**
	 * Whether the creature is currently material.
	 */
	private boolean material;

	/**
	 * The creature whose materiality is managed.
	 */
	private final Creature creature;

	private static int randomDuration() {
		return Utils.random(Clock.ONE_SECOND, 3 * Clock.ONE_SECOND);
	}

	public RandomMaterializer(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		final int count = randomDuration();

		this.creature = creature;
		this.temporizer = new Temporizer(creature.getId() + ".", count);

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s is now material for %d ticks", creature, count));
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			// Change the materiality
			this.material = !this.material;

			// Create a new temporizer
			final int count = randomDuration();

			this.temporizer = new Temporizer(creature.getId() + ".", count);

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s is now %s for %d ticks", creature, (material ? "material" : "immaterial"), count));
			}
		}

		return true;
	}

	private Materiality getMateriality() {
		return material ? Materiality.MATERIAL : Materiality.IMMATERIAL;
	}

	@Override
	public boolean isImmaterial() {
		return (getMateriality() == Materiality.IMMATERIAL);
	}

	@Override
	public boolean isMaterial() {
		return !isImmaterial();
	}
}