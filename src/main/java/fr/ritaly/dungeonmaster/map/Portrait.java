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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Portrait extends DirectedElement {

	private final Log log = LogFactory.getLog(Portrait.class);

	private Champion champion;

	public Portrait(Direction direction, Champion champion) {
		super(Element.Type.PORTRAIT, direction);

		Validate.notNull(champion, "The given champion is null");

		this.champion = champion;
	}

	public Champion getChampion() {
		return champion;
	}

	public boolean hasChampion() {
		return (champion != null);
	}

	public Champion reincarnate() {
		if (hasChampion()) {
			// FIXME Réincarner le champion
			final Champion backup = this.champion;

			this.champion = null;

			if (log.isInfoEnabled()) {
				log.info("Champion " + backup.getName() + " reincarnated");
			}

			return backup;
		}

		return null;
	}

	@Override
	public String getCaption() {
		return "PO";
	}

	@Override
	public boolean isTraversable(Party party) {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.isTrue(creature != null, "The given creature is null");

		return (creature != null) && Materiality.IMMATERIAL.equals(creature.getMateriality());
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
}