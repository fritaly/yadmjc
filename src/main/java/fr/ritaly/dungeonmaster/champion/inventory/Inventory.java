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

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Inventory implements ChangeEventSource, ChangeListener {

	private final Champion champion;

	private final BackPack backPack;

	private final Pouch pouch;

	private final Quiver quiver;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	private final int capacity;

	public Inventory(Champion champion) {
		if (champion == null) {
			throw new IllegalArgumentException("The given champion is null");
		}

		this.champion = champion;

		this.pouch = new Pouch(champion);
		this.pouch.addChangeListener(this);

		this.quiver = new Quiver(champion);
		this.quiver.addChangeListener(this);

		this.backPack = new BackPack(champion);
		this.backPack.addChangeListener(this);

		this.capacity = backPack.getCapacity() + quiver.getCapacity()
				+ pouch.getCapacity();
	}

	public Champion getChampion() {
		return champion;
	}

	public List<Item> getItems() {
		final List<Item> items = new ArrayList<Item>(capacity);
		items.addAll(backPack.getItems());
		items.addAll(pouch.getItems());
		items.addAll(quiver.getItems());

		return items;
	}

	public boolean isEmpty() {
		return backPack.isEmpty() && pouch.isEmpty() && quiver.isEmpty();
	}
	
	public boolean isFull() {
		return backPack.isFull() && pouch.isFull() && quiver.isFull();
	}

	public BackPack getBackPack() {
		return backPack;
	}

	public Pouch getPouch() {
		return pouch;
	}

	public Quiver getQuiver() {
		return quiver;
	}

	public float getTotalWeight() {
		return backPack.getTotalWeight() + pouch.getTotalWeight()
				+ quiver.getTotalWeight();
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

		// On teste les références dans l'ordre de probabilité !
		if ((source == backPack) || (source == quiver) || (source == pouch)) {
			// Propager l'évènement
			fireChangeEvent();
		}
	}

	/**
	 * Supprime tous les objets de l'inventaire et les retourne sous forme de
	 * List.
	 * 
	 * @return une List&lt;Item&gt;.
	 */
	public List<Item> empty() {
		final List<Item> items = new ArrayList<Item>(capacity);
		items.addAll(backPack.removeAll());
		items.addAll(quiver.removeAll());
		items.addAll(pouch.removeAll());

		return items;
	}
}