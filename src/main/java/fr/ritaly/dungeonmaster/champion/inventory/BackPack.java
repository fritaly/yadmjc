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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * The backpack is an item container with a capacity of 16. Any item can fit
 * into a back pack.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class BackPack extends AbstractItemContainer {

	public BackPack(Champion champion) {
		super(champion, 16);
	}

	@Override
	protected String getName() {
		return "BackPack";
	}

	@Override
	protected boolean accepts(int index, Item item) {
		checkIndex(index);
		Validate.notNull(item, "The given item is null");

		// The back pack can store any item at any index
		return true;
	}
}
