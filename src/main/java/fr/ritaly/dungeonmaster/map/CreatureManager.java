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
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.ai.Creature;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class CreatureManager {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Les cr�atures qui occupent l'�l�ment. Une m�me {@link Creature} selon sa
	 * taille peut occuper plusieurs {@link Sector}s de sorte que le nombre
	 * d'entr�es de la {@link Map} ne correspond pas forc�ment au nombre de
	 * {@link Creature}s !
	 */
	private Map<Sector, Creature> creatures;

	private final Element element;

	public CreatureManager(Element element) {
		Validate.notNull(element, "The given element is null");

		this.element = element;
	}

	/**
	 * Retourne les cr�atures occupant cet �l�ment sous forme de {@link List}.
	 *
	 * @return une Set&lt;Creature&gt. Cette m�thode ne retourne jamais null.
	 */
	public final Set<Creature> getCreatures() {
		// Retourner un Set permet de supprimer les doublons du r�sultat
		if (creatures == null) {
			return Collections.emptySet();
		}

		// Recopie d�fensive
		return new HashSet<Creature>(creatures.values());
	}

	/**
	 * Retourne la cr�ature occupant l'emplacement donn� s'il y a lieu.
	 *
	 * @param sector
	 *            l'emplacement sur lequel rechercher la cr�ature.
	 * @return une instance de {@link Creature} ou null s'il n'y en a aucune �
	 *         cet emplacement.
	 */
	public final Creature getCreature(Sector sector) {
		Validate.isTrue(sector != null, "The given sector is null");

		if (creatures != null) {
			return creatures.get(sector);
		}

		return null;
	}

	public final int getCreatureCount() {
		if (creatures != null) {
			// creatures != null -> Il y a forc�ment au moins une cr�ature
			return new HashSet<Creature>(creatures.values()).size();
		}

		return 0;
	}

	/**
	 * Retourne les cr�atures occupant cet �l�ment sous forme de Map.
	 *
	 * @return une Map&lt;Sector, Creature&gt. Cette m�thode ne retourne jamais
	 *         null.
	 */
	public final Map<Sector, Creature> getCreatureMap() {
		if (creatures == null) {
			return Collections.emptyMap();
		}

		// Recopie d�fensive
		return Collections.unmodifiableMap(creatures);
	}

	/**
	 * Calcule et retourne la place libre restante pour accueillir de nouvelles
	 * {@link Creature}s sous forme d'un entier (repr�sentant un nombre de
	 * {@link Sector}s).
	 *
	 * @return un entier dans l'intervalle [0-4] repr�sentant le nombre de
	 *         {@link Sector}s libres.
	 */
	public int getFreeRoom() {
		int room = 4;

		if (creatures != null) {
			for (Creature creature : new HashSet<Creature>(creatures.values())) {
				room -= creature.getSize().value();
			}

			if (room < 0) {
				// Pas cens� arriver
				throw new IllegalStateException(
						"Stumbled upon a negative room value <" + room
								+ ">. Creatures are: "
								+ new HashSet<Creature>(creatures.values()));
			}
		}

		return room;
	}

	/**
	 * Retourne les {@link Sector}s libres de cet {@link Element}.
	 *
	 * @return un EnumSet&lt;Sector&gt;. Ne retourne jamais null.
	 */
	public EnumSet<Sector> getFreeSectors() {
		if (creatures != null) {
			return EnumSet.complementOf(getOccupiedSectors());
		}

		return EnumSet.allOf(Sector.class);
	}

	public EnumSet<Direction> getFreeDirections() {
		if (creatures != null) {
			final boolean ne = !creatures.containsKey(Sector.NORTH_EAST);
			final boolean nw = !creatures.containsKey(Sector.NORTH_WEST);
			final boolean se = !creatures.containsKey(Sector.SOUTH_EAST);
			final boolean sw = !creatures.containsKey(Sector.SOUTH_WEST);

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
				// Un seul emplacement libre (2 possibilit�s au plus)
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

				// On tire au hasard la paire de directions retourn�es
				if (RandomUtils.nextBoolean()) {
					return EnumSet.of(Direction.EAST, Direction.WEST);
				} else {
					return EnumSet.of(Direction.NORTH, Direction.SOUTH);
				}
			default:
				throw new RuntimeException("Unexpected count <" + count + ">");
			}
		}

		// On tire au hasard la paire de directions retourn�es
		if (RandomUtils.nextBoolean()) {
			return EnumSet.of(Direction.EAST, Direction.WEST);
		} else {
			return EnumSet.of(Direction.NORTH, Direction.SOUTH);
		}
	}

	/**
	 * Retourne les {@link Sector}s occup�es par les {@link Creature}s
	 * pr�sentes sur cet {@link Element}.
	 *
	 * @return un EnumSet&lt;Sector&gt;. Ne retourne jamais null.
	 */
	public EnumSet<Sector> getOccupiedSectors() {
		if (creatures != null) {
			return EnumSet.copyOf(creatures.keySet());
		}

		return EnumSet.noneOf(Sector.class);
	}

	public final Sector getSector(Creature creature) {
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

		for (Sector sector : creatures.keySet()) {
			if (creatures.get(sector) == creature) {
				return sector;
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

		final boolean ne = (creatures.get(Sector.NORTH_EAST) == creature);
		final boolean nw = (creatures.get(Sector.NORTH_WEST) == creature);
		final boolean se = (creatures.get(Sector.SOUTH_EAST) == creature);
		final boolean sw = (creatures.get(Sector.SOUTH_WEST) == creature);

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
	 * Indique si l'�l�ment est occup� par au moins une cr�ature.
	 *
	 * @return si l'�l�ment est occup� par au moins une cr�ature.
	 */
	public boolean hasCreatures() {
		return (creatures != null) && !creatures.isEmpty();
	}

	// M�thode pour une cr�ature de taille Size.ONE
	public final void creatureSteppedOn(Creature creature, Sector sector) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (sector == null) {
			throw new IllegalArgumentException("The given sector is null");
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

		// L'emplacement doit initialement �tre vide
		if ((creatures != null) && (creatures.get(sector) != null)) {
			throw new IllegalArgumentException("The cell " + sector
					+ " of element " + element.getId()
					+ " is already occupied by a creature ("
					+ creatures.get(sector) + ")");
		}

		// Il doit y avoir la place d'accueillir la cr�ature
		if (!element.canHost(creature)) {
			throw new IllegalArgumentException("Unable to install creature "
					+ creature + " on cell " + sector + " of element "
					+ element.getId() + " because the remaining room is "
					+ getFreeRoom());
		}

		if (creatures == null) {
			// Cr�er la Map � la vol�e (apr�s les contr�les)
			creatures = new EnumMap<Sector, Creature>(Sector.class);
		}

		// M�moriser la cr�ature
		creatures.put(sector, creature);

		// Positionner l'�l�menet sur la cr�ature
		creature.setElement(element);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " ("
					+ sector + ")");
		}

		element.afterCreatureSteppedOn(creature);
	}

	// M�thode pour une cr�ature de taille Size.ONE
	public final void creatureSteppedOff(Creature creature, Sector sector) {
		if (creature == null) {
			throw new IllegalArgumentException("The given creature is null");
		}
		if (sector == null) {
			throw new IllegalArgumentException("The given sector is null");
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

		final Creature removed = creatures.remove(sector);

		if (removed != creature) {
			throw new IllegalArgumentException("Removed: " + removed
					+ " / Creature: " + creature + " / Sector: " + sector);
		}

		if (creatures.isEmpty()) {
			// Purger la Map � la vol�e
			creatures = null;
		}

		// Positionner l'�l�menet sur la cr�ature
		creature.setElement(element);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId() + " ("
					+ sector + ")");
		}

		element.afterCreatureSteppedOff(creature);
	}

	// M�thode pour une cr�ature de taille Size.FOUR
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

		// L'emplacement doit �tre vide
		if (hasCreatures()) {
			throw new IllegalArgumentException("The element " + element.getId()
					+ " is already occupied by at least one creature ("
					+ getCreatures() + ")");
		}

		if (creatures == null) {
			// Cr�er la Map � la vol�e (apr�s les contr�les)
			creatures = new EnumMap<Sector, Creature>(Sector.class);
		}

		// M�moriser la cr�ature qui occupe les 4 sectors
		creatures.put(Sector.NORTH_EAST, creature);
		creatures.put(Sector.NORTH_WEST, creature);
		creatures.put(Sector.SOUTH_EAST, creature);
		creatures.put(Sector.SOUTH_WEST, creature);

		// Positionner l'�l�menet sur la cr�ature
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

	// M�thode pour une cr�ature de taille Size.FOUR
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

		final boolean ne = (creatures.remove(Sector.NORTH_EAST) == creature);
		final boolean nw = (creatures.remove(Sector.NORTH_WEST) == creature);
		final boolean se = (creatures.remove(Sector.SOUTH_EAST) == creature);
		final boolean sw = (creatures.remove(Sector.SOUTH_WEST) == creature);

		if (!ne || !nw || !se || !sw) {
			throw new IllegalStateException("Unable to remove creature "
					+ creature + " from all sectors (ne ? " + ne + ", nw ? "
					+ nw + ", se ? " + se + ", sw ? " + sw + ")");
		}

		if (!creatures.isEmpty()) {
			// Pas cens� arriver
			throw new IllegalStateException("The creature map isn't empty: "
					+ creatures);
		}

		this.creatures = null;

		// R�initialiser l'�l�ment sur la cr�ature
		creature.setElement(null);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId()
					+ " (4 cells)");
		}

		element.afterCreatureSteppedOff(creature);
	}

	// M�thode pour une cr�ature de taille Size.TWO
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

		// L'emplacement doit initialement �tre vide
		final List<Sector> sectors = Sector.getVisibleSectors(direction);

		for (Sector sector : sectors) {
			if ((creatures != null) && (creatures.get(sector) != null)) {
				throw new IllegalArgumentException("The cell " + sector
						+ " of element " + element.getId()
						+ " is already occupied by a creature ("
						+ creatures.get(sector) + ")");
			}
		}

		// Il doit y avoir la place d'accueillir la cr�ature
		if (!element.canHost(creature)) {
			throw new IllegalArgumentException("Unable to install creature "
					+ creature + " on cells " + sectors + " of element "
					+ element.getId() + " because the remaining room is "
					+ getFreeRoom());
		}

		if (creatures == null) {
			// Cr�er la Map � la vol�e (apr�s les contr�les)
			creatures = new EnumMap<Sector, Creature>(Sector.class);
		}

		// M�moriser la cr�ature
		for (Sector sector : sectors) {
			creatures.put(sector, creature);
		}

		// R�initialiser l'�l�ment sur la cr�ature
		creature.setElement(null);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " ("
					+ direction + ")");
		}

		element.afterCreatureSteppedOn(creature);
	}

	// M�thode pour une cr�ature de taille Size.TWO
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

		final List<Sector> sectors = Sector.getVisibleSectors(direction);

		// La cr�ature doit �tre sur ces emplacements
		for (Sector sector : sectors) {
			if (creatures.get(sector) != creature) {
				throw new IllegalArgumentException("The cell " + sector
						+ " of element " + element.getId()
						+ " isn't occupied by creature " + creature
						+ " but by " + creatures.get(sector));
			}
		}

		final boolean c1 = (creatures.remove(sectors.get(0)) == creature);
		final boolean c2 = (creatures.remove(sectors.get(1)) == creature);

		if (!c1 || !c2) {
			throw new IllegalStateException("Unable to remove creature "
					+ creature + " from sectors (" + sectors + ")");
		}

		if (creatures.isEmpty()) {
			// Purger la Map � la vol�e
			creatures = null;
		}

		// R�initialiser l'�l�ment sur la cr�ature
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
				// On ne peut retourner de r�sultat
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
			final Sector sector = getSector(creature);

			if (sector == null) {
				throw new IllegalArgumentException("The given creature "
						+ creature + " isn't on element " + element);
			}

			creatureSteppedOff(creature, sector);

			return sector;
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
		// Location peut �tre null !
		Validate.notNull(creature, "The given creature is null");

		if (location == null) {
			// Cas � tester en premier
			creatureSteppedOn(creature);
		} else if (location instanceof Sector) {
			final Sector sector = (Sector) location;

			creatureSteppedOn(creature, sector);
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
			final List<Sector> sectors = new ArrayList<Sector>(
					getFreeSectors());

			if (sectors.size() > 1) {
				Collections.shuffle(sectors);
			}

			final Sector sector = sectors.iterator().next();

			addCreature(creature, sector);

			return sector;
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