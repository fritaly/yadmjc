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
package fr.ritaly.dungeonmaster.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class ChangeEventSupport implements ChangeEventSource {

	// Instancié à la demande uniquement si nécessaire
	private List<ChangeListener> listeners;

	@Override
	public void addChangeListener(ChangeListener listener) {
		if (listener != null) {
			if (listeners == null) {
				listeners = new ArrayList<ChangeListener>();
			}

			listeners.add(listener);
		}
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		if (listener != null) {
			listeners.remove(listener);

			if (listeners.isEmpty()) {
				listeners = null;
			}
		}
	}

	public void fireChangeEvent(ChangeEvent event) {
		if (event == null) {
			throw new IllegalArgumentException("The given event is null");
		}

		if (listeners != null) {
			// Recopie de la liste avant itération
			for (ChangeListener listener : new ArrayList<ChangeListener>(
					listeners)) {

				listener.onChangeEvent(event);
			}
		}
	}
}
