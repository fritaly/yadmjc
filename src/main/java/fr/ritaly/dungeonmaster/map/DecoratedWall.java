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

import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class DecoratedWall extends DirectedElement {

	public static enum Style {
		RING,
		HOOK,
		SLIME,
		GRATE,
		DRAIN,
		CRACK,
		SCRATCH,
		CHAOS,
		CHAOS_2,
		DEMON;
	}
	
	private final Style style;

	public DecoratedWall(Direction direction, Style style) {
		super(Element.Type.DECORATED_WALL, direction);
		
		Validate.notNull(style, "The given style is null");
		
		this.style = style;
	}

	@Override
	public String getCaption() {
		return "DW";
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.isTrue(creature != null, "The given creature is null");

		return (creature != null) && creature.isImmaterial();
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public synchronized Item pickItem(SubCell corner) {
		// Méthode non supportée
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void itemDroppedDown(Item item, SubCell corner) {
		// Méthode non supportée
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> getItems(SubCell subCell) {
		// Méthode non supportée
		throw new UnsupportedOperationException();
	}

	@Override
	public void validate() throws ValidationException {
		if (hasParty()) {
			throw new ValidationException("A fountain can't have champions");
		}
	}

	public Style getStyle() {
		return style;
	}
}