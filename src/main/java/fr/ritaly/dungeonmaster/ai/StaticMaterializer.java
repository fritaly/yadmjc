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

import fr.ritaly.dungeonmaster.Materiality;

/**
 * Simple implementation of {@link Materializer} used for a creature with a
 * static (that is, fixed) materiality that doesn't change over time.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class StaticMaterializer implements Materializer {

	/**
	 * The creature's static materiality.
	 */
	private final Materiality materiality;

	public StaticMaterializer(Materiality materiality) {
		Validate.notNull(materiality, "The given materiality is null");

		this.materiality = materiality;
	}

	@Override
	public boolean clockTicked() {
		// This implementation doesn't depend on clock ticks, no need to listen
		// for clock ticks
		return false;
	}

	@Override
	public Materiality getMateriality() {
		return materiality;
	}
}