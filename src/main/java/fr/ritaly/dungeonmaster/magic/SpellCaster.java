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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class SpellCaster implements ChangeEventSource {

	private final Log log = LogFactory.getLog(SpellCaster.class);

	private final Champion champion;

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	public SpellCaster(Champion champion) {
		if (champion == null) {
			throw new IllegalArgumentException("The given champion is null");
		}

		this.champion = champion;
	}

	private final Stack<Rune> runes = new Stack<Rune>();

	public Champion getChampion() {
		return champion;
	}

	public int getRuneCount() {
		return runes.size();
	}

	public void cast(Rune rune) {
		if (rune == null) {
			throw new IllegalArgumentException("The given rune is null");
		}

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + " is invoking " + rune.getType()
					+ " rune " + rune + " ...");
		}

		switch (getRuneCount()) {
		case 0:
			if (!(rune instanceof PowerRune)) {
				throw new IllegalArgumentException("The given rune <" + rune
						+ "> isn't a power rune");
			}
			break;
		case 1:
			if (!(rune instanceof ElementRune)) {
				throw new IllegalArgumentException("The given rune <" + rune
						+ "> isn't an element rune");
			}
			break;
		case 2:
			if (!(rune instanceof FormRune)) {
				throw new IllegalArgumentException("The given rune <" + rune
						+ "> isn't a form rune");
			}
			break;
		case 3:
			if (!(rune instanceof AlignmentRune)) {
				throw new IllegalArgumentException("The given rune <" + rune
						+ "> isn't an alignment rune");
			}
			break;
		default:
			throw new IllegalStateException(
					"A spell can only have from 2 to 4 runes");
		}

		runes.add(rune);

		fireChangeEvent();
	}

	public Rune cancel() {
		if (runes.isEmpty()) {
			throw new IllegalStateException("There is no rune to cancel");
		}

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + " is cancelling last rune ...");
		}

		final Rune cancelled = runes.pop();

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + " cancelled rune " + cancelled);
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
			throw new IllegalStateException(
					"Can't cast a spell with only a power rune");
		}

		if (log.isDebugEnabled() && !preview) {
			log.debug(champion.getName() + " is casting spell " + runes
					+ " ...");
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

		throw new IllegalStateException("Unexpected rune count <"
				+ runes.size() + ">");
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
	 * Retourne le rune de puissance utilisé pour invoquer le sort courant.
	 * 
	 * @return un {@link PowerRune} ou null si le rune de puissance n'a pas
	 *         encore été invoqué.
	 */
	public PowerRune getPowerRune() {
		if (runes.isEmpty()) {
			return null;
		}

		return (PowerRune) runes.get(0);
	}
}
