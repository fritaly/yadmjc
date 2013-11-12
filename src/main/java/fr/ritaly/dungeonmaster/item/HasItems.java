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

import java.util.List;

import fr.ritaly.dungeonmaster.Place;

public interface HasItems<P extends Place> {

	/**
	 * Adds the given into onto the given place.
	 *
	 * @param item
	 *            the item to add. Can't be null.
	 * @param place
	 *            the place where to add the item. Can't be null.
	 */
	public void addItem(Item item, P place);

	/**
	 * Returns the number of items (whatever the place).
	 *
	 * @return the number of items found.
	 */
	public int getItemCount();

	/**
	 * Returns the number of items for the given place.
	 *
	 * @param place
	 *            the place where to count the items. Can't be null.
	 * @return the number of items found for this place.
	 */
	public int getItemCount(P place);

	/**
	 * Returns all the items (whatever the place).
	 *
	 * @return a list of items. Never returns null.
	 */
	public List<Item> getItems();

	/**
	 * Returns the items for the given place.
	 *
	 * @param place
	 *            the place where the items are. Can't be null.
	 * @return a list of items representing the items found for this place.
	 *         Never returns null.
	 */
	public List<Item> getItems(P place);

	/**
	 * Returns the place where the given item is stored (if present).
	 *
	 * @param item
	 *            the item whose associated place is requested. Can't be null.
	 * @return a place where the item was found or null if missing.
	 */
	public P getPlace(Item item);

	/**
	 * Returns a random place (if any) where an item can be removed.
	 *
	 * @return a place where an item can be removed.
	 */
	public P getRandomPlace();

	/**
	 * Tells whether this manager has some items.
	 *
	 * @return whether this manager has some items.
	 */
	public boolean hasItems();

	/**
	 * Removes the first item (if any) at the given place.
	 *
	 * @param place
	 *            the place where the item to remove is. Can't be null.
	 * @return the removed item or null if there was no item at the given
	 *         place.
	 */
	public Item removeItem(P place);

	public boolean removeItem(Item item);

	/**
	 * Adds the given item onto a random place and returns it.
	 *
	 * @param item
	 *            the item to drop. Can't be null.
	 * @return the place where the item was added.
	 */
	public P addItem(Item item);

	/**
	 * Picks a random item and returns it.
	 *
	 * @return the randomly picked item or null if no item was found.
	 */
	public Item removeItem();
}