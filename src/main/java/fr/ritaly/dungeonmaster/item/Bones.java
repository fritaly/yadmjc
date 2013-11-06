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

import fr.ritaly.dungeonmaster.champion.Champion;

/**
 * Some bones. Those can be plain bones or the bones of a dead champion. The
 * bones are used for resurrecting a dead champion in an altar.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Bones extends MiscItem {

	/**
	 * The champion those bones belong to.
	 */
	private final Champion champion;

	/**
	 * Creates new bones associated to the given champion.
	 *
	 * @param champion
	 *            a champion whose bones those are. Can't be null.
	 */
	public Bones(Champion champion) {
		super(Type.BONES);

		Validate.notNull(champion, "The given champion is null");

		this.champion = champion;
	}

	/**
	 * Creates new bones with no associated champion.
	 */
	public Bones() {
		super(Type.BONES);

		this.champion = null;
	}

	/**
	 * Returns the champion associated to those bones.
	 *
	 * @return a champion or null if none is associated to those bones.
	 */
	public Champion getChampion() {
		return champion;
	}

	/**
	 * Tells whether those bones are associated to a dead champion.
	 *
	 * @return whether those bones are associated to a dead champion.
	 */
	public boolean hasChampion() {
		return (champion != null);
	}

	@Override
	public String toString() {
		return (champion != null) ? champion.getName() + "'s " + Type.BONES.name() : Type.BONES.name();
	}
}