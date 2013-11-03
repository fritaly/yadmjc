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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * The inventory of a champion consists in:
 * <ul>
 * <li>a backpack.</li>
 * <li>a pouch.</li>
 * <li>a quiver (aka sheath).</li>
 * </ul>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Inventory implements ChangeEventSource, ChangeListener {

	/**
	 * The champion whose inventory this is.
	 */
	private final Champion champion;

	/**
	 * This inventory's back pack.
	 */
	private final BackPack backPack;

	/**
	 * This inventory's pouch.
	 */
	private final Pouch pouch;

	/**
	 * This inventory's quiver.
	 */
	private final Quiver quiver;

	/**
	 * Support class for firing change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The inventory's full capacity.
	 */
	private final int capacity;

	public Inventory(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		this.champion = champion;

		this.pouch = new Pouch(champion);
		this.pouch.addChangeListener(this);

		this.quiver = new Quiver(champion);
		this.quiver.addChangeListener(this);

		this.backPack = new BackPack(champion);
		this.backPack.addChangeListener(this);

		this.capacity = backPack.getCapacity() + quiver.getCapacity() + pouch.getCapacity();
	}

	/**
	 * Returns the champion whose inventory this is.
	 *
	 * @return a champion. Never returns null.
	 */
	public Champion getChampion() {
		return champion;
	}

	/**
	 * Returns all the items in the champion's inventory as a list.
	 *
	 * @return a list of items. Never returns null.
	 */
	public List<Item> getItems() {
		final List<Item> items = new ArrayList<Item>(capacity);
		items.addAll(backPack.getItems());
		items.addAll(pouch.getItems());
		items.addAll(quiver.getItems());

		return items;
	}

	/**
	 * Tells whether the inventory is empty.
	 *
	 * @return whether the inventory is empty.
	 */
	public boolean isEmpty() {
		return backPack.isEmpty() && pouch.isEmpty() && quiver.isEmpty();
	}

	/**
	 * Tells whether the inventory is full.
	 *
	 * @return whether the inventory is full.
	 */
	public boolean isFull() {
		return backPack.isFull() && pouch.isFull() && quiver.isFull();
	}

	/**
	 * Returns this inventory's back pack.
	 *
	 * @return a back pack. Never returns null.
	 */
	public BackPack getBackPack() {
		return backPack;
	}

	/**
	 * Returns this inventory's pouch.
	 *
	 * @return a pouch. Never returns null.
	 */
	public Pouch getPouch() {
		return pouch;
	}

	/**
	 * Returns this inventory's quiver.
	 *
	 * @return a quiver. Never returns null.
	 */
	public Quiver getQuiver() {
		return quiver;
	}

	/**
	 * Returns the weight of all the items in this inventory as a float.
	 *
	 * @return a float representing a weight in Kg.
	 */
	public float getTotalWeight() {
		return backPack.getTotalWeight() + pouch.getTotalWeight() + quiver.getTotalWeight();
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	protected void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	@Override
	public void onChangeEvent(ChangeEvent event) {
		final Object source = event.getSource();

		if ((source == backPack) || (source == quiver) || (source == pouch)) {
			// Propagate the event to our listeners
			fireChangeEvent();
		}
	}

	/**
	 * Remove all the items in this inventory and returns them as a list.
	 *
	 * @return a list of items. Never returns null.
	 */
	public List<Item> empty() {
		final List<Item> items = new ArrayList<Item>(capacity);
		items.addAll(backPack.removeAll());
		items.addAll(quiver.removeAll());
		items.addAll(pouch.removeAll());

		return items;
	}
}