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
 * A temporizer is responsible for delaying the triggering of an event for a
 * specified number of clock "cycles". This class acts as a count down and is
 * reusable: once a temporizer triggered, it can be reused endlessly.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Temporizer {

	private final Log log = LogFactory.getLog(Temporizer.class);

	/**
	 * Stores the number of clock cycles to wait before triggering.
	 */
	private final int max;

	/**
	 * The remaining number of clock cycles to wait before triggering.
	 */
	private int current;

	/**
	 * A label used mainly for debugging purposes.
	 */
	private final String label;

	public Temporizer(String label, int max) {
		Validate.isTrue(!StringUtils.isEmpty(label), String.format("The given label '%s' is blank", label));
		Validate.isTrue(max > 0, String.format("The given max value %d must be positive", max));

		this.label = label;
		this.max = max;
		this.current = max;
	}

	/**
	 * Notifies the temporizer that a clock cycle elapsed and returns if the
	 * temporiser triggered.
	 *
	 * @return whether the temporiser triggered.
	 */
	public final boolean trigger() {
		final int oldCount = current;

		current--;

		if (log.isDebugEnabled()) {
			log.debug(String.format("Temporizer[%s].Count: %d -> %d", label, oldCount, current));
		}

		if (current == 0) {
			if (log.isDebugEnabled()) {
				log.debug("Temporizer timed-out");
			}

			// Reset the count down
			current = max;

			return true;
		}

		return false;
	}
}
