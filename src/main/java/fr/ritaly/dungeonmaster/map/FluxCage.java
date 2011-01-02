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
package fr.ritaly.dungeonmaster.map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;

public class FluxCage implements ClockListener, ChangeEventSource {

	private final Log log = LogFactory.getLog(FluxCage.class);

	/**
	 * The {@link Element} where the flux cage is located.
	 */
	private final Element element;

	private final Temporizer temporizer;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	// 10 secondes
	private int lifeTime = 10;

	public FluxCage(Element element) {
		Validate.notNull(element, "The given element is null");

		this.element = element;
		this.temporizer = new Temporizer("FluxCage" + element.getPosition(),
				Clock.ONE_SECOND);
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	private void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			// La durée de vie du nuage diminue
			final int backup = lifeTime;

			final boolean again = --lifeTime > 0;

			if (log.isDebugEnabled()) {
				log.debug(this + ".LifeTime: " + backup + " -> " + lifeTime
						+ " [-1]");
			}

			if (!again) {
				// Notifie la fin de vie du nuage
				fireChangeEvent();
			}
		}

		return (lifeTime > 0);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + element.getPosition();
	}
}