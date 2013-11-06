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
import fr.ritaly.dungeonmaster.item.Item;

public class ItemManager {

	protected final Log log = LogFactory.getLog(this.getClass());

	private final Element element;

	/**
	 * Stores the items for this element. Populated when an item is dropped.
	 * Reset to null when the last item is picked up. The items are handled as a
	 * stack (LIFO data structure). A regular floor tile has 4 sectors where
	 * items can be stacked hence the Map<Sector, Stack<Item>>.
	 */
	private Map<Sector, Stack<Item>> items;

	public ItemManager(Element element) {
		Validate.notNull(element, "The given element is null");

		this.element = element;
	}

	/**
	 * Drops the given into onto the given sector.
	 *
	 * @param item
	 *            the item to drop. Can't be null.
	 * @param sector
	 *            the sector where to drop the item. Can't be null.
	 */
	public synchronized void itemDropped(Item item, Sector sector) {
		// This method isn't final on purpose to allow sub-classes to override
		// this implementation if the element can't accept items
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

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s dropped on %s at %s", item, element.getId(), sector));
		}

		element.afterItemDropped(item, sector);

		element.fireChangeEvent();
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

				if (log.isDebugEnabled()) {
					log.debug(String.format("%s picked from %s at %s", item, element.getId(), sector));
				}

				element.afterItemPicked(item, sector);

				element.fireChangeEvent();

				return item;
			}
		}

		return null;
	}

	public final Sector getSector(Item item) {
		Validate.notNull(item, "The given item is null");

		if (items == null) {
			return null;
		}

		for (Sector sector : items.keySet()) {
			final Stack<Item> stack = items.get(sector);

			if ((stack != null) && stack.contains(item)) {
				return sector;
			}
		}

		// Creature introuvable
		return null;
	}

	/**
	 * Retourne tous les objets au sol sur cet �l�ment.
	 *
	 * @return une List&lt;Item&gt;. Cette m�thode ne retourne jamais null.
	 */
	public final List<Item> getItems() {
		if (items != null) {
			// items != null -> Il y a forc�ment au moins un objet au sol
			final List<Item> list = new ArrayList<Item>();

			for (Stack<Item> stack : items.values()) {
				list.addAll(stack);
			}

			return list;
		}

		// Aucun objet au sol
		return Collections.emptyList();
	}

	public final int getItemCount() {
		if (items != null) {
			// items != null -> Il y a forc�ment au moins un objet au sol
			int count = 0;

			for (Stack<Item> stack : items.values()) {
				count += stack.size();
			}

			return count;
		}

		// Aucun objet au sol
		return 0;
	}

	public final int getItemCount(Sector sector) {
		Validate.notNull(sector, "The given sector is null");

		if (items != null) {
			final Stack<Item> stack = items.get(sector);

			return (stack != null) ? stack.size() : 0;
		}

		// Aucun objet au sol
		return 0;
	}

	/**
	 * Retourne les objets situ�s � l'emplacement donn� s'il y a lieu.
	 *
	 * @param sector
	 *            l'emplacement o� sont situ�s les objets recherch�s.
	 * @return une List&lt;Item&gt; contenant les objets trouv�s. Cette m�thode
	 *         ne retourne jamais null.
	 */
	public List<Item> getItems(Sector sector) {
		Validate.isTrue(sector != null, "The given sectore is null");

		if (items != null) {
			final List<Item> list = items.get(sector);

			if (list != null) {
				// Recopie d�fensive
				return new ArrayList<Item>(list);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Indique si l'�l�ment comporte des objets.
	 *
	 * @return si l'�l�ment comporte des objets.
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
