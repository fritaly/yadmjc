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
package fr.ritaly.dungeonmaster.champion.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A container of items with a fixed capacity. Each item inside the container
 * can be accessed with a 0-based index.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public abstract class AbstractItemContainer implements ChangeEventSource,
		ItemContainer {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The container's total capacity.
	 */
	private final int capacity;

	/**
	 * The array storing the items.
	 */
	private final Item[] items;

	/**
	 * Support class for firing change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The champion this container belongs to.
	 */
	private final Champion champion;

	protected AbstractItemContainer(Champion champion, int capacity) {
		Validate.notNull(champion, "The given champion is null");
		Validate.isTrue(capacity > 0, String.format("The given capacity %d must be positive", capacity));

		this.capacity = capacity;
		this.items = new Item[capacity];
		this.champion = champion;
	}

	protected AbstractItemContainer(int capacity) {
		Validate.isTrue(capacity > 0, String.format("The given capacity %d must be positive", capacity));

		this.capacity = capacity;
		this.items = new Item[capacity];
		this.champion = null;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	@Override
	public int getItemCount() {
		int count = 0;

		for (int i = 0; i < capacity; i++) {
			if (items[i] != null) {
				count++;
			}
		}

		return count;
	}

	@Override
	public List<Item> getItems() {
		final List<Item> list = new ArrayList<Item>(capacity);

		for (Item item : items) {
			if (item != null) {
				// Ignore the null references
				list.add(item);
			}
		}

		return list;
	}

	@Override
	public boolean isFull() {
		return getItemCount() == getCapacity();
	}

	@Override
	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	protected void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	@Override
	public Item getRandom() {
		final List<Item> list = getItems();

		if (list.isEmpty()) {
			return null;
		}

		return list.get(new Random().nextInt(list.size()));
	}

	@Override
	public Item removeRandom() {
		final Item item = getRandom();

		if ((item != null) && !remove(item)) {
			throw new IllegalStateException(String.format("Unable to remove random item %s from container", item));
		}

		itemRemoved(item);

		return item;
	}

	private String getFullName() {
		return (champion != null) ? champion.getName() + "." + getName() : getName();
	}

	/**
	 * Tries adding the given item to this container and returns the index where
	 * the item was added.
	 *
	 * @param item
	 *            the item to add. Can't be null.
	 * @return an integer representing the index where the item was added inside
	 *         the container or -1 if the item couldn't be added.
	 */
	@Override
	public int add(Item item) {
		Validate.notNull(item, "The given item is null");

		for (int i = 0; i < capacity; i++) {
			if (items[i] == null) {
				// Is the item compatible with this container at this index ?
				if (!accepts(i, item)) {
					// Keep on iterating as the item can be rejected for an
					// index but accepted for another
					continue;
				}

				items[i] = item;

				if (log.isDebugEnabled()) {
					log.debug(getFullName() + ": [+] " + item.getType().name());
				}

				itemAdded(item);

				fireChangeEvent();

				return i;
			}
		}

		return -1;
	}

	private void itemAdded(Item item) {
		if ((item != null) && Item.Type.RABBIT_FOOT.equals(item.getType())) {
			if (champion != null) {
				champion.getStats().getLuck().inc(10);
			}
		}
	}

	private void itemRemoved(Item item) {
		if ((item != null) && Item.Type.RABBIT_FOOT.equals(item.getType())) {
			if (champion != null) {
				champion.getStats().getLuck().dec(10);
			}
		}
	}

	@Override
	public List<Item> removeAll() {
		final List<Item> result = new ArrayList<Item>(capacity);

		for (int i = 0; i < capacity; i++) {
			if (items[i] != null) {
				result.add(items[i]);

				if (log.isDebugEnabled()) {
					log.debug(getFullName() + ": [-] " + items[i].getType().name());
				}

				itemRemoved(items[i]);

				items[i] = null;
			}
		}

		if (!result.isEmpty()) {
			fireChangeEvent();
		}

		return result;
	}

	@Override
	public Item remove(int index) {
		checkIndex(index);

		final Item removed = items[index];

		items[index] = null;

		if (removed != null) {
			if (log.isDebugEnabled()) {
				log.debug(getFullName() + ": [-] " + removed.getType().name());
			}

			itemRemoved(removed);

			fireChangeEvent();
		}

		return removed;
	}

	@Override
	public boolean remove(Item item) {
		Validate.notNull(item);

		for (int i = 0; i < items.length; i++) {
			if (items[i] == item) {
				items[i] = null;

				if (log.isDebugEnabled()) {
					log.debug(getFullName() + ": [-] " + item.getType().name());
				}

				itemRemoved(item);

				fireChangeEvent();

				return true;
			}
		}

		return false;
	}

	@Override
	public Item set(int index, Item item) {
		checkIndex(index);

		if (item == null) {
			throw new IllegalArgumentException("The given item is null");
		}

		// Is the item compatible with the container at this index ?
		if (!accepts(index, item)) {
			return item;
		}

		final Item removed = items[index];

		items[index] = item;

		if (log.isDebugEnabled()) {
			log.debug(getFullName() + ": [+] " + item.getType().name());
		}

		if (item != removed) {
			itemRemoved(removed);
			itemAdded(item);
			fireChangeEvent();
		}

		return removed;
	}

	protected final void checkIndex(int index) {
		if ((index < 0) || (index >= capacity)) {
			throw new IllegalArgumentException("The given index <" + index + "> must be in range [0-" + (capacity - 1) + "]");
		}
	}

	protected abstract String getName();

	protected abstract boolean accepts(int index, Item item);

	@Override
	public float getTotalWeight() {
		float weight = 0.0f;

		for (int i = 0; i < capacity; i++) {
			if (items[i] != null) {
				weight += items[i].getWeight();
			}
		}

		return weight;
	}
}