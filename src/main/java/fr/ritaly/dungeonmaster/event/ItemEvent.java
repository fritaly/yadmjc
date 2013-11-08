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
package fr.ritaly.dungeonmaster.event;

import java.util.EventObject;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.item.Item;

/**
 * An item event fired to notify when an item is dropped or picked.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class ItemEvent extends EventObject {

	private static final long serialVersionUID = -1077534497337414635L;

	private final Item item;

	private final Object place;

	public ItemEvent(Object source, Item item, Object place) {
		super(source);

		Validate.notNull(item, "The given item is null");
		Validate.notNull(place, "The given place is null");

		this.item = item;
		this.place = place;
	}

	public Item getItem() {
		return item;
	}

	public Object getPlace() {
		return place;
	}
}
