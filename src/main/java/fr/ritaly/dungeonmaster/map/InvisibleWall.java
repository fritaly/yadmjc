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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class InvisibleWall extends Element {

	public InvisibleWall() {
		super(Type.INVISIBLE_WALL);
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.isTrue(creature != null, "The given creature is null");

		return creature.isImmaterial();
	}
	
	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public String getCaption() {
		return "IW";
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException(
					"An invisible wall can't have champions");
		}
	}
}
