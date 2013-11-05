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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.champion.inventory.AbstractItemContainer;
import fr.ritaly.dungeonmaster.champion.inventory.ItemContainer;

/**
 * A chest is a special item because it's also an item container.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Chest extends Item implements ItemContainer {

	/**
	 * The underlying item container managing the chest's content.
	 */
	private final ItemContainer container = new AbstractItemContainer(8) {
		@Override
		protected String getName() {
			return "Chest";
		}

		@Override
		protected boolean accepts(int index, Item item) {
			checkIndex(index);
			Validate.notNull(item, "The given item is null");

			// The chest can store any item compatible with the chest
			return item.getCarryLocations().contains(CarryLocation.CHEST);
		}
	};

	/**
	 * Creates a new empty chest.
	 */
	public Chest() {
		super(Item.Type.CHEST);
	}

	@Override
	public int getCapacity() {
		return container.getCapacity();
	}

	@Override
	public int getItemCount() {
		return container.getItemCount();
	}

	@Override
	public List<Item> getItems() {
		return container.getItems();
	}

	@Override
	public boolean isFull() {
		return container.isFull();
	}

	@Override
	public boolean isEmpty() {
		return container.isEmpty();
	}

	@Override
	public Item getRandom() {
		return container.getRandom();
	}

	@Override
	public Item removeRandom() {
		return container.removeRandom();
	}

	@Override
	public int add(Item item) {
		return container.add(item);
	}

	@Override
	public List<Item> removeAll() {
		return container.removeAll();
	}

	@Override
	public Item remove(int index) {
		return container.remove(index);
	}

	@Override
	public boolean remove(Item item) {
		return container.remove(item);
	}

	@Override
	public Item set(int index, Item item) {
		return container.set(index, item);
	}

	@Override
	public float getTotalWeight() {
		// Take into account the chest's weight (5.0 Kg)
		return 5.0f + container.getTotalWeight();
	}

	@Override
	protected BodyPart.Type getActivationBodyPart() {
		return null;
	}

	@Override
	public int getAntiMagic() {
		return 0;
	}

	@Override
	public int getShield() {
		return 0;
	}
}