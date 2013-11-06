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

import org.apache.commons.lang.Validate;

/**
 * Factory of items. This factory provides factory methods more convenient to
 * use than the constructors of item classes.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class ItemFactory {

	private static final ItemFactory FACTORY = new ItemFactory();

	private ItemFactory() {
	}

	public static ItemFactory getFactory() {
		return FACTORY;
	}

	/**
	 * Creates and returns a new item with the given type.
	 *
	 * @param type
	 *            the type of item to create. Can't be null.
	 * @return a new item.
	 */
	public Item newItem(final Item.Type type) {
		Validate.notNull(type, "The given item type is null");

		return type.newItem();
	}
}