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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Un {@link Temporizer} permet de retarder le déclenchement d'un évènement d'un
 * nombre entier de cycles de {@link Clock}.
 *
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Temporizer {

	private final Log log = LogFactory.getLog(Temporizer.class);

	private int max;

	private int current;

	private final String label;

	public Temporizer(String label, int max) {
		Validate.isTrue(!StringUtils.isEmpty(label), "The given label <"
				+ label + "> is blank");
		Validate.isTrue(max > 0, "The given max value <" + max
				+ "> must be positive");

		this.label = label;
		this.max = max;
		this.current = max;
	}

	public final boolean trigger() {
		final int oldCount = current;

		current--;

		if (log.isDebugEnabled()) {
			log.debug("Temporizer[" + label + "].Count: " + oldCount + " -> "
					+ current);
		}

		if (current == 0) {
			if (log.isDebugEnabled()) {
				log.debug("Temporizer timed-out");
			}

			// Réinitialiser le compte à rebours
			current = max;

			// Déclenchement
			return true;
		}

		// Pas encore de déclenchement
		return false;
	}
}
