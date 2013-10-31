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
package fr.ritaly.dungeonmaster;

/**
 * Impl�mentation de {@link ClockListener} permettant d'ex�cuter une action
 * lorsque le temps d'attente a expir�.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public abstract class DeferredCommand implements ClockListener {

	private final Temporizer temporizer;

	public DeferredCommand(String label, int max) {
		// Contr�les r�alis�s par la classe Temporizer
		this.temporizer = new Temporizer(label, max);
	}

	/**
	 * M�thode de callback appel�e lorsque le temps d'attente a expir�.
	 */
	protected abstract void run();

	@Override
	public final boolean clockTicked() {
		if (temporizer.trigger()) {

			// D�clenchement
			run();

			return false;
		}

		// Pas encore de d�clenchement
		return true;
	}
}