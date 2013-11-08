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

import org.apache.commons.lang.Validate;

/**
 * Helper class used for simplifying the notification of item events.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class ItemEventSupport implements ItemEventSource {

	/**
	 * Lists the listeners to be notified of item events.
	 */
	private List<ItemListener> listeners;

	@Override
	public void addItemListener(ItemListener listener) {
		if (listener != null) {
			if (listeners == null) {
				// Create the list only when strictly necessary
				listeners = new ArrayList<ItemListener>();
			}

			listeners.add(listener);
		}
	}

	@Override
	public void removeItemListener(ItemListener listener) {
		if (listener != null) {
			listeners.remove(listener);

			if (listeners.isEmpty()) {
				// Get rid of the unnecessary list
				listeners = null;
			}
		}
	}

	/**
	 * Notifies the registered listeners of the given item event.
	 *
	 * @param event
	 *            an item event to be notified to registered listeners. Can't be
	 *            null.
	 */
	public void fireItemPickedEvent(final ItemEvent event) {
		Validate.notNull(event, "The given item event is null");

		if (listeners != null) {
			// Clone the list to avoid concurrent modifications
			for (ItemListener listener : new ArrayList<ItemListener>(listeners)) {
				listener.onItemPicked(event);
			}
		}
	}

	/**
	 * Notifies the registered listeners of the given item event.
	 *
	 * @param event
	 *            an item event to be notified to registered listeners. Can't be
	 *            null.
	 */
	public void fireItemDroppedEvent(final ItemEvent event) {
		Validate.notNull(event, "The given item event is null");

		if (listeners != null) {
			// Clone the list to avoid concurrent modifications
			for (ItemListener listener : new ArrayList<ItemListener>(listeners)) {
				listener.onItemDropped(event);
			}
		}
	}
}
