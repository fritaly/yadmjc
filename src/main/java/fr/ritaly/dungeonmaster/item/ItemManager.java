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
public class ItemManager implements ItemEventSource, HasItems<Sector> {

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

	private void fireItemRemovedEvent(Item item, Sector sector) {
		eventSupport.fireItemRemovedEvent(new ItemEvent(this, item, sector));
	}

	private void fireItemAddedEvent(Item item, Sector sector) {
		eventSupport.fireItemAddedEvent(new ItemEvent(this, item, sector));
	}

	@Override
	public synchronized void addItem(Item item, Sector sector) {
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

		fireItemAddedEvent(item, sector);
	}

	@Override
	public synchronized Sector addItem(Item item) {
		Validate.notNull(item, "The given item is null");

		final Sector sector = Sector.random();
		addItem(item, sector);

		return sector;
	}

	@Override
	public synchronized Item removeItem(Sector sector) {
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

				fireItemRemovedEvent(item, sector);

				return item;
			}
		}

		return null;
	}

	@Override
	public synchronized Item removeItem() {
		if (items == null) {
			return null;
		}

		final List<Sector> sectors = new ArrayList<Sector>(items.keySet());

		if (sectors.isEmpty()) {
			// Not supposed to happen
			return null;
		}

		Collections.shuffle(sectors);

		return removeItem(sectors.iterator().next());
	}

	@Override
	public final Sector getPlace(Item item) {
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

	@Override
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

	@Override
	public final int getItemCount() {
		int count = 0;

		if (items != null) {
			for (Stack<Item> stack : items.values()) {
				count += stack.size();
			}
		}

		return count;
	}

	@Override
	public final int getItemCount(Sector sector) {
		Validate.notNull(sector, "The given sector is null");

		if (items != null) {
			final Stack<Item> stack = items.get(sector);

			return (stack != null) ? stack.size() : 0;
		}

		// No item on the floor
		return 0;
	}

	@Override
	public List<Item> getItems(Sector sector) {
		Validate.isTrue(sector != null, "The given sector is null");

		if (items != null) {
			final List<Item> list = items.get(sector);

			if (list != null) {
				// Defensive recopy
				return new ArrayList<Item>(list);
			}
		}

		return Collections.emptyList();
	}

	@Override
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

	@Override
	public boolean removeItem(Item item) {
		return removeItem(getPlace(item)) == item;
	}

	@Override
	public Sector getRandomPlace() {
		if (items == null) {
			return null;
		}

		final List<Sector> sectors = new ArrayList<Sector>(items.keySet());

		if (sectors.isEmpty()) {
			// Not supposed to happen
			return null;
		}

		Collections.shuffle(sectors);

		return sectors.iterator().next();
	}
}
