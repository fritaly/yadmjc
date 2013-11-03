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

import java.util.List;

import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A container of items with a fixed capacity. Each item inside the container
 * can be accessed with a 0-based index.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public interface ItemContainer {

	/**
	 * Returns the container's fixed capacity (that is, the number of items the
	 * container can store).
	 *
	 * @return a positive integer.
	 */
	public int getCapacity();

	public void addChangeListener(ChangeListener listener);

	public void removeChangeListener(ChangeListener listener);

	/**
	 * Returns the actual number of items inside the container.
	 *
	 * @return a positive integer within [0,capacity].
	 */
	public int getItemCount();

	/**
	 * Returns the items inside the container.
	 *
	 * @return a list of items. Never returns null.
	 */
	public List<Item> getItems();

	/**
	 * Tells whether the container is full.
	 *
	 * @return whether the container is full.
	 * @see #getCapacity()
	 * @see #getItemCount()
	 */
	public boolean isFull();

	/**
	 * Tells whether the container is empty.
	 *
	 * @return whether the container is empty.
	 * @see #getItemCount()
	 */
	public boolean isEmpty();

	/**
	 * Returns a random item among those inside the container. This method doesn't remove the item.
	 *
	 * @return an item or null if the container is empty.
	 */
	public Item getRandom();

	/**
	 * Adds the given itme to this container and returns the index where the
	 * item was added.
	 *
	 * @param item
	 *            the item to add. Can't be null.
	 * @return an integer representing the index where the item was added to the
	 *         container or -1 if the operation failed.
	 */
	public int add(Item item);

	/**
	 * Removes all the items from this container and returns them as a list.
	 *
	 * @return a list containing the removed items. Never returns null.
	 */
	public List<Item> removeAll();

	/**
	 * Removes the item located at the given index (if any) and returns it.
	 *
	 * @param index
	 *            an integer representing the index of item to remove. Must be a
	 *            positive or zero value.
	 * @return the removed item or null if there was no item at the given index.
	 */
	public Item remove(int index);

	/**
	 * Removes a random item from this container and returns it.
	 *
	 * @return the removed item or null if the container is empty.
	 */
	public Item removeRandom();

	/**
	 * Removes the given item from this container and returns whether the
	 * operation succeeded.
	 *
	 * @param item
	 *            the item to remove. Can't be null.
	 * @return whether the removal operation succeeded.
	 */
	public boolean remove(Item item);

	/**
	 * Sets the item at the given index and returns the item that was possibly
	 * removed by this operation.
	 *
	 * @param index
	 *            an integer representing the index where to set the item.
	 * @param item
	 *            the item to set. Can't be null.
	 * @return the item previously at this index or null if there was none.
	 */
	public Item set(int index, Item item);

	/**
	 * Returns the weight of all items in this container.
	 *
	 * @return a float representing the total weight (in Kg) of all the
	 *         contained items.
	 */
	public float getTotalWeight();
}