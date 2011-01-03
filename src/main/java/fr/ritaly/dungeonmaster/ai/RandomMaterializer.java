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

import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.Utils;

public class RandomMaterializer implements Materializer {

	private final Log log = LogFactory.getLog(this.getClass());

	private Temporizer temporizer;

	private boolean material;

	private final Creature creature;

	public RandomMaterializer(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		final int count = Utils.random(6, 6 * 3);

		this.creature = creature;
		this.temporizer = new Temporizer(creature.getId() + ".", count);

		if (log.isDebugEnabled()) {
			log.debug(creature + " is now material for " + count + " ticks");
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			this.material = !this.material;

			// Recycler le temporizer
			final int count = Utils.random(6, 6 * 3);

			this.temporizer = new Temporizer(creature.getId() + ".", count);

			if (log.isDebugEnabled()) {
				log.debug(creature + " is now "
						+ (material ? "material" : "immaterial") + " for "
						+ count + " ticks");
			}
		}

		return true;
	}
	
	@Override
	public Materiality getMateriality() {
		return material ? Materiality.MATERIAL : Materiality.IMMATERIAL;
	}
}