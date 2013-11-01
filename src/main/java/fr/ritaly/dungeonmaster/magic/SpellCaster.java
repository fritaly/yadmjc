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
package fr.ritaly.dungeonmaster.magic;

import java.util.Stack;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * This object is responsible for storing the runes when casting a spell.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class SpellCaster implements ChangeEventSource {

	private final Log log = LogFactory.getLog(SpellCaster.class);

	/**
	 * The champion casting a spell.
	 */
	private final Champion champion;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The runes invoked to cast a spell.
	 */
	private final Stack<Rune> runes = new Stack<Rune>();

	public SpellCaster(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		this.champion = champion;
	}

	public Champion getChampion() {
		return champion;
	}

	public int getRuneCount() {
		return runes.size();
	}

	public void cast(Rune rune) {
		Validate.notNull(rune, "The given rune is null");

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s is invoking %s rune %s ...", champion.getName(), rune.getType(), rune));
		}

		switch (getRuneCount()) {
		case 0:
			if (!(rune instanceof PowerRune)) {
				throw new IllegalArgumentException(String.format("The given rune %d isn't a power rune", rune));
			}
			break;
		case 1:
			if (!(rune instanceof ElementRune)) {
				throw new IllegalArgumentException(String.format("The given rune %d isn't an element rune", rune));
			}
			break;
		case 2:
			if (!(rune instanceof FormRune)) {
				throw new IllegalArgumentException(String.format("The given rune %d isn't a form rune", rune));
			}
			break;
		case 3:
			if (!(rune instanceof AlignmentRune)) {
				throw new IllegalArgumentException(String.format("The given rune %d isn't an alignment rune", rune));
			}
			break;
		default:
			throw new IllegalStateException("A spell can only have from 2 to 4 runes");
		}

		runes.add(rune);

		fireChangeEvent();
	}

	public Rune cancel() {
		if (runes.isEmpty()) {
			throw new IllegalStateException("There is no rune to cancel");
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s is cancelling last rune ...", champion.getName()));
		}

		final Rune cancelled = runes.pop();

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s cancelled rune %s", champion.getName(), cancelled));
		}

		fireChangeEvent();

		return cancelled;
	}

	public void clear() {
		if (!runes.isEmpty()) {
			runes.clear();

			fireChangeEvent();
		}
	}

	public Spell cast() {
		return cast(false);
	}

	public Spell cast(boolean preview) {
		if (runes.isEmpty()) {
			throw new IllegalStateException("There is no spell to cast");
		}
		if (runes.size() == 1) {
			throw new IllegalStateException("Can't cast a spell with only a power rune");
		}

		if (log.isDebugEnabled() && !preview) {
			log.debug(String.format("%s is casting spell %s ...", champion.getName(), runes));
		}

		final PowerRune powerRune = (PowerRune) runes.get(0);
		final ElementRune elementRune = (ElementRune) runes.get(1);

		if (runes.size() == 2) {
			if (!preview) {
				clear(); // --> event
			}

			return new Spell(powerRune, elementRune);
		}

		final FormRune formRune = (FormRune) runes.get(2);

		if (runes.size() == 3) {
			if (!preview) {
				clear(); // --> event
			}

			return new Spell(powerRune, elementRune, formRune);
		}

		final AlignmentRune alignmentRune = (AlignmentRune) runes.get(3);

		if (runes.size() == 4) {
			if (!preview) {
				clear(); // --> event
			}

			return new Spell(powerRune, elementRune, formRune, alignmentRune);
		}

		throw new IllegalStateException(String.format("Unexpected rune count (%d)", runes.size()));
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	private void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	/**
	 * Returns the power rune if available.
	 *
	 * @return a power rune or null if it hasn't yet been invoked.
	 */
	public PowerRune getPowerRune() {
		if (runes.isEmpty()) {
			return null;
		}

		return (PowerRune) runes.get(0);
	}
}
