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
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.ai.Creature;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class CreatureManager {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Les créatures qui occupent l'élément. Une même {@link Creature} selon sa
	 * taille peut occuper plusieurs {@link SubCell}s de sorte que le nombre
	 * d'entrées de la {@link Map} ne correspond pas forcément au nombre de
	 * {@link Creature}s !
	 */
	private Map<SubCell, Creature> creatures;

	private final Element element;

	public CreatureManager(Element element) {
		Validate.notNull(element, "The given element is null");

		this.element = element;
	}

	/**
	 * Retourne les créatures occupant cet élément sous forme de {@link List}.
	 * 
	 * @return une Set&lt;Creature&gt. Cette méthode ne retourne jamais null.
	 */
	public final Set<Creature> getCreatures() {
		// Retourner un Set permet de supprimer les doublons du résultat
		if (creatures == null) {
			return Collections.emptySet();
		}

		// Recopie défensive
		return new HashSet<Creature>(creatures.values());
	}

	/**
	 * Retourne la créature occupant l'emplacement donné s'il y a lieu.
	 * 
	 * @param subCell
	 *            l'emplacement sur lequel rechercher la créature.
	 * @return une instance de {@link Creature} ou null s'il n'y en a aucune à
	 *         cet emplacement.
	 */
	public final Creature getCreature(SubCell subCell) {
		Validate.isTrue(subCell != null, "The given sub-cell is null");

		if (creatures != null) {
			return creatures.get(subCell);
		}

		return null;
	}

	public final int getCreatureCount() {
		if (creatures != null) {
			// creatures != null -> Il y a forcément au moins une créature
			return new HashSet<Creature>(creatures.values()).size();
		}

		return 0;
	}

	/**
	 * Retourne les créatures occupant cet élément sous forme de Map.
	 * 
	 * @return une Map&lt;SubCell, Creature&gt. Cette méthode ne retourne jamais
	 *         null.
	 */
	public final Map<SubCell, Creature> getCreatureMap() {
		if (creatures == null) {
			return Collections.emptyMap();
		}

		// Recopie défensive
		return Collections.unmodifiableMap(creatures);
	}

	/**
	 * Calcule et retourne la place libre restante pour accueillir de nouvelles
	 * {@link Creature}s sous forme d'un entier (représentant un nombre de
	 * {@link SubCell}s).
	 * 
	 * @return un entier dans l'intervalle [0-4] représentant le nombre de
	 *         {@link SubCell}s libres.
	 */
	public int getFreeRoom() {
		int room = 4;

		if (creatures != null) {
			for (Creature creature : new HashSet<Creature>(creatures.values())) {
				room -= creature.getSize().value();
			}

			if (room < 0) {
				// Pas censé arriver
				throw new IllegalStateException(
						"Stumbled upon a negative room value <" + room
								+ ">. Creatures are: "
								+ new HashSet<Creature>(creatures.values()));
			}
		}

		return room;
	}

	/**
	 * Retourne les {@link SubCell}s libres de cet {@link Element}.
	 * 
	 * @return un EnumSet&lt;SubCell&gt;. Ne retourne jamais null.
	 */
	public EnumSet<SubCell> getFreeSubCells() {
		if (creatures != null) {
			return EnumSet.complementOf(getOccupiedSubCells());
		}

		return EnumSet.allOf(SubCell.class);
	}

	public EnumSet<Direction> getFreeDirections() {
		if (creatures != null) {
			final boolean ne = !creatures.containsKey(SubCell.NORTH_EAST);
			final boolean nw = !creatures.containsKey(SubCell.NORTH_WEST);
			final boolean se = !creatures.containsKey(SubCell.SOUTH_EAST);
			final boolean sw = !creatures.containsKey(SubCell.SOUTH_WEST);

			int count = 0;

			if (ne) {
				count++;
			}
			if (nw) {
				count++;
			}
			if (se) {
				count++;
			}
			if (sw) {
				count++;
			}

			switch (count) {
			case 0:
			case 1:
				// Aucun emplacement libre
				return EnumSet.noneOf(Direction.class);
			case 2:
				// Un seul emplacement libre au plus
				if (ne && nw) {
					return EnumSet.of(Direction.NORTH);
				} else if (se && sw) {
					return EnumSet.of(Direction.SOUTH);
				} else if (nw && sw) {
					return EnumSet.of(Direction.WEST);
				} else if (ne && se) {
					return EnumSet.of(Direction.EAST);
				} else {
					// Aucun emplacement libre (Cells en diagonale)
					return EnumSet.noneOf(Direction.class);
				}
			case 3:
				// Un seul emplacement libre (2 possibilités au plus)
				final List<Direction> directions = new ArrayList<Direction>(2);
				
				if (ne && nw) {
					directions.add(Direction.NORTH);
				}
				if (se && sw) {
					directions.add(Direction.SOUTH);
				}
				if (nw && sw) {
					directions.add(Direction.WEST);
				}
				if (ne && se) {
					directions.add(Direction.EAST);
				}
				
				if (directions.size() == 1) {
					return EnumSet.of(directions.iterator().next());
				} else {
					Collections.shuffle(directions);
					
					return EnumSet.of(directions.iterator().next());
				}
			case 4:
				// 2 emplacements libres

				// On tire au hasard la paire de directions retournées
				if (RandomUtils.nextBoolean()) {
					return EnumSet.of(Direction.EAST, Direction.WEST);
				} else {
					return EnumSet.of(Direction.NORTH, Direction.SOUTH);
				}
			default:
				throw new RuntimeException("Unexpected count <" + count + ">");
			}
		}

		// On tire au hasard la paire de directions retournées
		if (RandomUtils.nextBoolean()) {
			return EnumSet.of(Direction.EAST, Direction.WEST);
		} else {
			return EnumSet.of(Direction.NORTH, Direction.SOUTH);
		}
	}

	/**
	 * Retourne les {@link SubCell}s occupées par les {@link Creature}s
	 * présentes sur cet {@link Element}.
	 * 
	 * @return un EnumSet&lt;SubCell&gt;. Ne retourne jamais null.
	 */
	public EnumSet<SubCell> getOccupiedSubCells() {
		if (creatures != null) {
			return EnumSet.copyOf(creatures.keySet());
		}

		return EnumSet.noneOf(SubCell.class);
	}

	public final SubCell getSubCell(Creature creature) {
		Validate.notNull(creature, "The given creature is null");
		if (!Creature.Size.ONE.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.ONE
					+ ")");
		}

		if (creatures == null) {
			return null;
		}

		for (SubCell subCell : creatures.keySet()) {
			if (creatures.get(subCell) == creature) {
				return subCell;
			}
		}

		// Creature introuvable
		return null;
	}

	public final Direction getDirection(Creature creature) {
		Validate.notNull(creature, "The given creature is null");
		if (!Creature.Size.TWO.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.TWO
					+ ")");
		}

		if (creatures == null) {
			return null;
		}

		final boolean ne = (creatures.get(SubCell.NORTH_EAST) == creature);
		final boolean nw = (creatures.get(SubCell.NORTH_WEST) == creature);
		final boolean se = (creatures.get(SubCell.SOUTH_EAST) == creature);
		final boolean sw = (creatures.get(SubCell.SOUTH_WEST) == creature);

		if (ne && nw) {
			return Direction.NORTH;
		} else if (se && sw) {
			return Direction.SOUTH;
		} else if (ne && se) {
			return Direction.EAST;
		} else if (nw && sw) {
			return Direction.WEST;
		} else {
			throw new IllegalStateException(
					"Unable to determine direction for creature <" + creature
							+ "> ne ? " + ne + ", nw ? " + nw + ", se ? " + se
							+ ", sw ? " + sw + ")");
		}
	}

	/**
	 * Indique si l'élément est occupé par au moins une créature.
	 * 
	 * @return si l'élément est occupé par au moins une créature.
	 */
	public boolean hasCreatures() {
		return (creatures != null) && !creatures.isEmpty();
	}

	// Méthode pour une créature de taille Size.ONE
	public final void creatureSteppedOn(Creature creature, SubCell subCell) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (subCell == null) {
			throw new IllegalArgumentException("The given sub-cell is null");
		}
		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature
					+ " can't step on " + element.getId());
		}
		if (!Creature.Size.ONE.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.ONE
					+ ")");
		}

		// L'emplacement doit initialement être vide
		if ((creatures != null) && (creatures.get(subCell) != null)) {
			throw new IllegalArgumentException("The cell " + subCell
					+ " of element " + element.getId()
					+ " is already occupied by a creature ("
					+ creatures.get(subCell) + ")");
		}

		// Il doit y avoir la place d'accueillir la créature
		if (!element.canHost(creature)) {
			throw new IllegalArgumentException("Unable to install creature "
					+ creature + " on cell " + subCell + " of element "
					+ element.getId() + " because the remaining room is "
					+ getFreeRoom());
		}

		if (creatures == null) {
			// Créer la Map à la volée (après les contrôles)
			creatures = new EnumMap<SubCell, Creature>(SubCell.class);
		}

		// Mémoriser la créature
		creatures.put(subCell, creature);
		
		// Positionner l'élémenet sur la créature
		creature.setElement(element);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " ("
					+ subCell + ")");
		}

		element.afterCreatureSteppedOn(creature);
	}

	// Méthode pour une créature de taille Size.ONE
	public final void creatureSteppedOff(Creature creature, SubCell subCell) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (subCell == null) {
			throw new IllegalArgumentException("The given sub-cell is null");
		}
		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature
					+ " can't step off " + element.getId());
		}
		if (!Creature.Size.ONE.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.ONE
					+ ")");
		}
		if (creatures == null) {
			throw new IllegalStateException(
					"There is currently no creature on element " + element);
		}

		final Creature removed = creatures.remove(subCell);

		if (removed != creature) {
			throw new IllegalArgumentException("Removed: " + removed
					+ " / Creature: " + creature + " / SubCell: " + subCell);
		}

		if (creatures.isEmpty()) {
			// Purger la Map à la volée
			creatures = null;
		}
		
		// Positionner l'élémenet sur la créature
		creature.setElement(element);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId() + " ("
					+ subCell + ")");
		}

		element.afterCreatureSteppedOff(creature);
	}

	// Méthode pour une créature de taille Size.FOUR
	public final void creatureSteppedOn(Creature creature) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature
					+ " can't step on " + element.getId());
		}
		if (!Creature.Size.FOUR.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.FOUR
					+ ")");
		}

		// L'emplacement doit être vide
		if (hasCreatures()) {
			throw new IllegalArgumentException("The element " + element.getId()
					+ " is already occupied by at least one creature ("
					+ getCreatures() + ")");
		}

		if (creatures == null) {
			// Créer la Map à la volée (après les contrôles)
			creatures = new EnumMap<SubCell, Creature>(SubCell.class);
		}

		// Mémoriser la créature qui occupe les 4 SubCells
		creatures.put(SubCell.NORTH_EAST, creature);
		creatures.put(SubCell.NORTH_WEST, creature);
		creatures.put(SubCell.SOUTH_EAST, creature);
		creatures.put(SubCell.SOUTH_WEST, creature);
		
		// Positionner l'élémenet sur la créature
		creature.setElement(element);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId()
					+ " (4 cells)");
		}

		element.afterCreatureSteppedOn(creature);
	}

	public boolean hasCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		return (creatures != null) && creatures.containsValue(creature);
	}

	// Méthode pour une créature de taille Size.FOUR
	public final void creatureSteppedOff(Creature creature) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature
					+ " can't step off " + element.getId());
		}
		if (!Creature.Size.FOUR.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.FOUR
					+ ")");
		}
		if (!hasCreature(creature)) {
			throw new IllegalArgumentException("The given creature " + creature
					+ " isn't currently on element " + this);
		}
		if (creatures == null) {
			throw new IllegalStateException(
					"There is currently no creature on element " + element);
		}

		final boolean ne = (creatures.remove(SubCell.NORTH_EAST) == creature);
		final boolean nw = (creatures.remove(SubCell.NORTH_WEST) == creature);
		final boolean se = (creatures.remove(SubCell.SOUTH_EAST) == creature);
		final boolean sw = (creatures.remove(SubCell.SOUTH_WEST) == creature);

		if (!ne || !nw || !se || !sw) {
			throw new IllegalStateException("Unable to remove creature "
					+ creature + " from all SubCells (ne ? " + ne + ", nw ? "
					+ nw + ", se ? " + se + ", sw ? " + sw + ")");
		}

		if (!creatures.isEmpty()) {
			// Pas censé arriver
			throw new IllegalStateException("The creature map isn't empty: "
					+ creatures);
		}

		this.creatures = null;
		
		// Réinitialiser l'élément sur la créature
		creature.setElement(null);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId()
					+ " (4 cells)");
		}

		element.afterCreatureSteppedOff(creature);
	}

	// Méthode pour une créature de taille Size.TWO
	public final void creatureSteppedOn(Creature creature, Direction direction) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}
		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature
					+ " can't step on " + element.getId());
		}
		if (!Creature.Size.TWO.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.TWO
					+ ")");
		}

		// L'emplacement doit initialement être vide
		final List<SubCell> subCells = SubCell.getVisibleSubCells(direction);

		for (SubCell subCell : subCells) {
			if ((creatures != null) && (creatures.get(subCell) != null)) {
				throw new IllegalArgumentException("The cell " + subCell
						+ " of element " + element.getId()
						+ " is already occupied by a creature ("
						+ creatures.get(subCell) + ")");
			}
		}

		// Il doit y avoir la place d'accueillir la créature
		if (!element.canHost(creature)) {
			throw new IllegalArgumentException("Unable to install creature "
					+ creature + " on cells " + subCells + " of element "
					+ element.getId() + " because the remaining room is "
					+ getFreeRoom());
		}

		if (creatures == null) {
			// Créer la Map à la volée (après les contrôles)
			creatures = new EnumMap<SubCell, Creature>(SubCell.class);
		}

		// Mémoriser la créature
		for (SubCell subCell : subCells) {
			creatures.put(subCell, creature);
		}
		
		// Réinitialiser l'élément sur la créature
		creature.setElement(null);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " ("
					+ direction + ")");
		}

		element.afterCreatureSteppedOn(creature);
	}

	// Méthode pour une créature de taille Size.TWO
	public final void creatureSteppedOff(Creature creature, Direction direction) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}
		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature
					+ " can't step off " + element.getId());
		}
		if (!Creature.Size.TWO.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <"
					+ creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.TWO
					+ ")");
		}
		if (creatures == null) {
			throw new IllegalStateException(
					"There is currently no creature on element " + element);
		}

		final List<SubCell> subCells = SubCell.getVisibleSubCells(direction);

		// La créature doit être sur ces emplacements
		for (SubCell subCell : subCells) {
			if (creatures.get(subCell) != creature) {
				throw new IllegalArgumentException("The cell " + subCell
						+ " of element " + element.getId()
						+ " isn't occupied by creature " + creature
						+ " but by " + creatures.get(subCell));
			}
		}

		final boolean c1 = (creatures.remove(subCells.get(0)) == creature);
		final boolean c2 = (creatures.remove(subCells.get(1)) == creature);

		if (!c1 || !c2) {
			throw new IllegalStateException("Unable to remove creature "
					+ creature + " from sub-cells (" + subCells + ")");
		}

		if (creatures.isEmpty()) {
			// Purger la Map à la volée
			creatures = null;
		}
		
		// Réinitialiser l'élément sur la créature
		creature.setElement(null);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId() + " ("
					+ direction + ")");
		}

		element.afterCreatureSteppedOff(creature);
	}

	public Creature.Height getTallestCreatureHeight() {
		if (creatures != null) {
			if (creatures.isEmpty()) {
				// On ne peut retourner de résultat
				return null;
			}

			Creature.Height height = null;

			for (Creature creature : creatures.values()) {
				if (height == null) {
					height = creature.getHeight();
				} else if (creature.getHeight().compareTo(height) > 0) {
					height = creature.getHeight();
				}
			}

			return height;
		}

		return null;
	}

	public Object removeCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		switch (creature.getSize()) {
		case ONE:
			final SubCell subCell = getSubCell(creature);

			if (subCell == null) {
				throw new IllegalArgumentException("The given creature "
						+ creature + " isn't on element " + element);
			}

			creatureSteppedOff(creature, subCell);

			return subCell;
		case TWO:
			final Direction direction = getDirection(creature);

			if (direction == null) {
				throw new IllegalArgumentException("The given creature "
						+ creature + " isn't on element " + element);
			}

			creatureSteppedOff(creature, direction);

			return direction;
		case FOUR:
			creatureSteppedOff(creature);

			return null;
		default:
			throw new UnsupportedOperationException(
					"Unsupported creature size <" + creature.getSize() + ">");
		}
	}

	public void addCreature(Creature creature, Object location) {
		// Location peut être null !
		Validate.notNull(creature, "The given creature is null");

		if (location == null) {
			// Cas à tester en premier
			creatureSteppedOn(creature);
		} else if (location instanceof SubCell) {
			final SubCell subCell = (SubCell) location;

			creatureSteppedOn(creature, subCell);
		} else if (location instanceof Direction) {
			final Direction direction = (Direction) location;

			creatureSteppedOn(creature, direction);
		} else {
			throw new IllegalArgumentException("Unexpected location <"
					+ location + ">");
		}
	}

	public Object addCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		switch (creature.getSize()) {
		case ONE:
			final List<SubCell> subCells = new ArrayList<SubCell>(
					getFreeSubCells());

			if (subCells.size() > 1) {
				Collections.shuffle(subCells);
			}

			final SubCell subCell = subCells.iterator().next();

			addCreature(creature, subCell);

			return subCell;
		case TWO:
			final List<Direction> directions = new ArrayList<Direction>(
					getFreeDirections());

			if (directions.size() > 1) {
				Collections.shuffle(directions);
			}

			final Direction direction = directions.iterator().next();

			addCreature(creature, direction);

			return direction;
		case FOUR:
			addCreature(creature, null);
			
			return null;
		default:
			throw new UnsupportedOperationException(
					"Unsupported creature size <" + creature.getSize() + ">");
		}
	}
}