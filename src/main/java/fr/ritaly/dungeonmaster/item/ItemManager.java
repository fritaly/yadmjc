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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.event.ItemEvent;
import fr.ritaly.dungeonmaster.event.ItemEventSource;
import fr.ritaly.dungeonmaster.event.ItemEventSupport;
import fr.ritaly.dungeonmaster.event.ItemListener;

/**
 * An object responsible for managing items picked / dropped.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class ItemManager implements ItemEventSource {

	protected final Log log = LogFactory.getLog(this.getClass());

	private final ItemEventSupport eventSupport = new ItemEventSupport();

	/**
	 * Stores the items for this element. Populated when an item is dropped.
	 * Reset to null when the last item is picked up. The items are handled as a
	 * stack (LIFO data structure). A regular floor tile has 4 sectors where
	 * items can be stacked hence the Map<Sector, Stack<Item>>.
	 */
	private Map<Sector, Stack<Item>> items;

	public ItemManager() {
	}

	@Override
	public void addItemListener(ItemListener listener) {
		eventSupport.addItemListener(listener);
	}

	@Override
	public void removeItemListener(ItemListener listener) {
		eventSupport.removeItemListener(listener);
	}

	private void fireItemPickedEvent(Item item, Sector sector) {
		eventSupport.fireItemPickedEvent(new ItemEvent(this, item, sector));
	}

	private void fireItemDroppedEvent(Item item, Sector sector) {
		eventSupport.fireItemDroppedEvent(new ItemEvent(this, item, sector));
	}

	/**
	 * Drops the given item onto the given sector.
	 *
	 * @param item
	 *            the item to drop. Can't be null.
	 * @param sector
	 *            the sector where to drop the item. Can't be null.
	 */
	public synchronized void dropItem(Item item, Sector sector) {
		Validate.notNull(item, "The given item is null");
		Validate.notNull(sector, "The given sector is null");

		if (items == null) {
			items = new EnumMap<Sector, Stack<Item>>(Sector.class);
		}

		Stack<Item> stack = items.get(sector);

		if (stack == null) {
			items.put(sector, stack = new Stack<Item>());
		}

		stack.push(item);

		fireItemDroppedEvent(item, sector);
	}

	/**
	 * Drops the given item onto a random sector.
	 *
	 * @param item
	 *            the item to drop. Can't be null.
	 */
	public synchronized void dropItem(Item item) {
		Validate.notNull(item, "The given item is null");

		dropItem(item, Sector.random());
	}

	/**
	 * Picks the first item (if any) at the given sector.
	 *
	 * @param sector
	 *            the sector where the item to pick is. Can't be null.
	 * @return the picked item or null if there was no item at the given
	 *         sector.
	 */
	public synchronized Item pickItem(Sector sector) {
		Validate.notNull(sector, "The given sector is null");

		if (items != null) {
			final Stack<Item> stack = items.get(sector);

			if (stack != null) {
				// Remove the top item from the stack
				final Item item = stack.pop();

				if (stack.isEmpty()) {
					items.remove(sector);

					if (items.isEmpty()) {
						items = null;
					}
				}

				fireItemPickedEvent(item, sector);

				return item;
			}
		}

		return null;
	}

	/**
	 * Picks a random item and returns it.
	 *
	 * @return the randomly picked item or null if no item was found.
	 */
	public synchronized Item pickItem() {
		if (items == null) {
			return null;
		}

		final List<Sector> sectors = new ArrayList<Sector>(items.keySet());

		if (sectors.isEmpty()) {
			// Not supposed to happen
			return null;
		}

		Collections.shuffle(sectors);

		return pickItem(sectors.iterator().next());
	}

	/**
	 * Returns the sector where the given item is stored (if present).
	 *
	 * @param item
	 *            the item whose associated sector is requested. Can't be null.
	 * @return a sector where the item was found or null if missing.
	 */
	public final Sector getSector(Item item) {
		Validate.notNull(item, "The given item is null");

		if (items != null) {
			for (Sector sector : items.keySet()) {
				final Stack<Item> stack = items.get(sector);

				if ((stack != null) && stack.contains(item)) {
					return sector;
				}
			}
		}

		// Can't find item
		return null;
	}

	/**
	 * Returns all the items (whatever the sector).
	 *
	 * @return a list of items. Never returns null.
	 */
	public final List<Item> getItems() {
		if (items != null) {
			final List<Item> list = new ArrayList<Item>();

			for (Stack<Item> stack : items.values()) {
				list.addAll(stack);
			}

			return list;
		}

		// No item on the floor
		return Collections.emptyList();
	}

	/**
	 * Returns the number of items (whatever the sector).
	 *
	 * @return the number of items found.
	 */
	public final int getItemCount() {
		int count = 0;

		if (items != null) {
			for (Stack<Item> stack : items.values()) {
				count += stack.size();
			}
		}

		return count;
	}

	/**
	 * Returns the number of items for the given sector.
	 *
	 * @param sector
	 *            the sector where to count the items. Can't be null.
	 * @return the number of items found for this sector.
	 */
	public final int getItemCount(Sector sector) {
		Validate.notNull(sector, "The given sector is null");

		if (items != null) {
			final Stack<Item> stack = items.get(sector);

			return (stack != null) ? stack.size() : 0;
		}

		// No item on the floor
		return 0;
	}

	/**
	 * Returns the items for the given sector.
	 *
	 * @param sector
	 *            the sector where the items are. Can't be null.
	 * @return a list of items representing the items found for this sector.
	 *         Never returns null.
	 */
	public List<Item> getItems(Sector sector) {
		Validate.isTrue(sector != null, "The given sectore is null");

		if (items != null) {
			final List<Item> list = items.get(sector);

			if (list != null) {
				// Defensive recopy
				return new ArrayList<Item>(list);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Tells whether this manager has some items.
	 *
	 * @return whether this manager has some items.
	 */
	public boolean hasItems() {
		if (items != null) {
			for (Stack<Item> stack : items.values()) {
				if (!stack.isEmpty()) {
					return true;
				}
			}
		}

		return false;
	}
}
