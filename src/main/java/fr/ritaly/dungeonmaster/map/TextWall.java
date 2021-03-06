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
import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class TextWall extends DirectedElement {

	private final List<String> lines;

	public TextWall(Direction direction, List<String> lines) {
		super(Element.Type.TEXT_WALL, direction);

		Validate.notNull(lines, "The given list of lines is null");

		this.lines = new ArrayList<String>(lines);
	}

	@Override
	public String getSymbol() {
		return "X";
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		return (creature != null) && creature.isImmaterial();
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public synchronized Item removeItem(Sector corner) {
		// M�thode non support�e
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void addItem(Item item, Sector corner) {
		// M�thode non support�e
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> getItems(Sector sector) {
		// M�thode non support�e
		throw new UnsupportedOperationException();
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException("A text wall can't have champions");
		}
	}

	public List<String> getLines() {
		return new ArrayList<String>(lines);
	}

	@Override
	public boolean isFluxCageAllowed() {
		return false;
	}
}