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
package fr.ritaly.dungeonmaster.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Place;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.map.Element;

/**
 * An object responsible for managing the positioning of creatures on a given
 * position. Creatures can be different "sizes" that determine the number of
 * sectors occupied.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class CreatureManager {

	private final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The managed creatures stored as a map. The map content depends on the
	 * size of the creatures. If a dragon (whose size is 4) is present, the 4
	 * sectors will be occupied by the dragon. For a worm (size of 2), only 2
	 * sectors will be occupied per creature.
	 */
	private Map<Sector, Creature> creatures;

	/**
	 * The element managed by this class.
	 */
	private final Element element;

	public CreatureManager(Element element) {
		Validate.notNull(element, "The given element is null");

		this.element = element;
	}

	/**
	 * Returns the creatures occupying this position as a set.
	 *
	 * @return a set of creatures. Never returns null.
	 */
	public final Set<Creature> getCreatures() {
		if (creatures == null) {
			return Collections.emptySet();
		}

		// Using a set will remove the double entries (1 creature can occupy
		// several sectors).
		return new HashSet<Creature>(creatures.values());
	}

	/**
	 * Returns the creature on the given sector (if any).
	 *
	 * @param sector
	 *            the sector to test. Can't be null.
	 * @return the creature occupying the sector or null if there's none.
	 */
	public final Creature getCreature(Sector sector) {
		Validate.notNull(sector, "The given sector is null");

		return (creatures != null) ? creatures.get(sector) : null;
	}

	/**
	 * Returns the number of creatures present.
	 *
	 * @return the number of creatures.
	 */
	public final int getCreatureCount() {
		return getCreatures().size();
	}

	/**
	 * Returns the creatures occupying this position as a map.
	 *
	 * @return a map of creatures per sector. Never returns null.
	 */
	public final Map<Sector, Creature> getCreatureMap() {
		if (creatures == null) {
			return Collections.emptyMap();
		}

		// Defensive recopy
		return Collections.unmodifiableMap(creatures);
	}

	/**
	 * Return the unoccupied sectors.
	 *
	 * @return the set of unoccupied sectors. Never returns null.
	 */
	public EnumSet<Sector> getFreeSectors() {
		return (creatures != null) ? EnumSet.complementOf(getOccupiedSectors()) : EnumSet.allOf(Sector.class);
	}

	private EnumSet<Direction> getFreeDirections() {
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
				// No free direction
				return EnumSet.noneOf(Direction.class);
			case 2:
				// At most one direction free
				if (ne && nw) {
					return EnumSet.of(Direction.NORTH);
				} else if (se && sw) {
					return EnumSet.of(Direction.SOUTH);
				} else if (nw && sw) {
					return EnumSet.of(Direction.WEST);
				} else if (ne && se) {
					return EnumSet.of(Direction.EAST);
				} else {
					// No direction free (the sectors are staggered)
					return EnumSet.noneOf(Direction.class);
				}
			case 3:
				// One for sure, maybe 2
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
				// 2 are free
				// Choose a random pair of directions
				if (RandomUtils.nextBoolean()) {
					return EnumSet.of(Direction.EAST, Direction.WEST);
				} else {
					return EnumSet.of(Direction.NORTH, Direction.SOUTH);
				}
			default:
				throw new RuntimeException("Unexpected count <" + count + ">");
			}
		}

		// Choose a random pair of directions
		if (RandomUtils.nextBoolean()) {
			return EnumSet.of(Direction.EAST, Direction.WEST);
		} else {
			return EnumSet.of(Direction.NORTH, Direction.SOUTH);
		}
	}

	/**
	 * Returns the sectors occupied by creatures.
	 *
	 * @return a set of sectors. Never returns null.
	 */
	public EnumSet<Sector> getOccupiedSectors() {
		return (creatures != null) ? EnumSet.copyOf(creatures.keySet()) : EnumSet.noneOf(Sector.class);
	}

	public final Sector getSector(Creature creature) {
		Validate.notNull(creature, "The given creature is null");
		if (!Creature.Size.ONE.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.ONE + ")");
		}

		if (creatures != null) {
			for (Sector sector : creatures.keySet()) {
				if (creatures.get(sector) == creature) {
					return sector;
				}
			}
		}

		// Creature not found
		return null;
	}

	public final Direction getDirection(Creature creature) {
		Validate.notNull(creature, "The given creature is null");
		if (!Creature.Size.TWO.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.TWO + ")");
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
		}

		throw new IllegalStateException("Unable to determine direction for creature <" + creature + "> ne ? " + ne + ", nw ? "
				+ nw + ", se ? " + se + ", sw ? " + sw + ")");
	}

	/**
	 * Indique si l'�l�ment est occup� par au moins une cr�ature.
	 *
	 * @return si l'�l�ment est occup� par au moins une cr�ature.
	 */
	public boolean hasCreatures() {
		return (creatures != null) && !creatures.isEmpty();
	}

	private final void _addCreature(Creature creature, Sector sector) {
		Validate.notNull(creature, "The given creature is null");
		Validate.notNull(sector, "The given sector is null");

		if (!Creature.Size.ONE.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.ONE + ")");
		}

		// L'emplacement doit initialement �tre vide
		if ((creatures != null) && (creatures.get(sector) != null)) {
			throw new IllegalArgumentException("The cell " + sector + " of element " + element.getId()
					+ " is already occupied by a creature (" + creatures.get(sector) + ")");
		}

		// Il doit y avoir la place d'accueillir la cr�ature
		if (!canHost(creature)) {
			throw new IllegalArgumentException("Unable to install creature " + creature + " on cell " + sector + " of element "
					+ element.getId() + " because the remaining room is " + getFreeSectors().size());
		}

		if (creatures == null) {
			creatures = new EnumMap<Sector, Creature>(Sector.class);
		}

		creatures.put(sector, creature);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " (" + sector + ")");
		}
	}

	private final void _removeCreature(Creature creature, Sector sector) {
		Validate.notNull(creature, "The given creature is null");
		Validate.notNull(sector, "The given sector is null");

		if (!Creature.Size.ONE.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.ONE + ")");
		}
		if (creatures == null) {
			throw new IllegalStateException("There is currently no creature on element " + element);
		}

		final Creature removed = creatures.remove(sector);

		if (removed != creature) {
			throw new IllegalArgumentException("Removed: " + removed + " / Creature: " + creature + " / Sector: " + sector);
		}

		if (creatures.isEmpty()) {
			creatures = null;
		}

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId() + " (" + sector + ")");
		}
	}

	private final void _addCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		if (!Creature.Size.FOUR.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.FOUR + ")");
		}

		if (hasCreatures()) {
			throw new IllegalArgumentException("The element " + element.getId()
					+ " is already occupied by at least one creature (" + getCreatures() + ")");
		}

		if (creatures == null) {
			creatures = new EnumMap<Sector, Creature>(Sector.class);
		}

		creatures.put(Sector.NORTH_EAST, creature);
		creatures.put(Sector.NORTH_WEST, creature);
		creatures.put(Sector.SOUTH_EAST, creature);
		creatures.put(Sector.SOUTH_WEST, creature);

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " (4 sectors)");
		}
	}

	public boolean hasCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		return (creatures != null) && creatures.containsValue(creature);
	}

	private final void _removeCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		if (!Creature.Size.FOUR.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.FOUR + ")");
		}
		if (!hasCreature(creature)) {
			throw new IllegalArgumentException("The given creature " + creature + " isn't currently on element " + this);
		}
		if (creatures == null) {
			throw new IllegalStateException("There is currently no creature on element " + element);
		}

		final boolean ne = (creatures.remove(Sector.NORTH_EAST) == creature);
		final boolean nw = (creatures.remove(Sector.NORTH_WEST) == creature);
		final boolean se = (creatures.remove(Sector.SOUTH_EAST) == creature);
		final boolean sw = (creatures.remove(Sector.SOUTH_WEST) == creature);

		if (!ne || !nw || !se || !sw) {
			throw new IllegalStateException("Unable to remove creature " + creature + " from all sectors (ne ? " + ne + ", nw ? "
					+ nw + ", se ? " + se + ", sw ? " + sw + ")");
		}

		if (!creatures.isEmpty()) {
			throw new IllegalStateException("The creature map isn't empty: " + creatures);
		}

		this.creatures = null;

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId() + " (4 sectors)");
		}
	}

	private final void _addCreature(Creature creature, Direction direction) {
		Validate.notNull(creature, "The given creature is null");
		Validate.notNull(direction, "The given direction is null");

		if (!Creature.Size.TWO.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.TWO + ")");
		}

		// L'emplacement doit initialement �tre vide
		final List<Sector> sectors = Sector.getVisibleSectors(direction);

		for (Sector sector : sectors) {
			if ((creatures != null) && (creatures.get(sector) != null)) {
				throw new IllegalArgumentException("The cell " + sector + " of element " + element.getId()
						+ " is already occupied by a creature (" + creatures.get(sector) + ")");
			}
		}

		if (!canHost(creature)) {
			throw new IllegalArgumentException("Unable to install creature " + creature + " on cells " + sectors + " of element "
					+ element.getId() + " because the remaining room is " + getFreeSectors().size());
		}

		if (creatures == null) {
			creatures = new EnumMap<Sector, Creature>(Sector.class);
		}

		for (Sector sector : sectors) {
			creatures.put(sector, creature);
		}

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped on " + element.getId() + " (" + direction + ")");
		}
	}

	private final void _removeCreature(Creature creature, Direction direction) {
		Validate.notNull(creature, "The given creature is null");
		Validate.notNull(direction, "The given direction is null");

		if (!Creature.Size.TWO.equals(creature.getSize())) {
			throw new IllegalArgumentException("The given creature <" + creature + "> has an invalid size (actual: "
					+ creature.getSize() + ", expected: " + Creature.Size.TWO + ")");
		}
		if (creatures == null) {
			throw new IllegalStateException("There is currently no creature on element " + element);
		}

		final List<Sector> sectors = Sector.getVisibleSectors(direction);

		for (Sector sector : sectors) {
			if (creatures.get(sector) != creature) {
				throw new IllegalArgumentException("The cell " + sector + " of element " + element.getId()
						+ " isn't occupied by creature " + creature + " but by " + creatures.get(sector));
			}
		}

		final boolean c1 = (creatures.remove(sectors.get(0)) == creature);
		final boolean c2 = (creatures.remove(sectors.get(1)) == creature);

		if (!c1 || !c2) {
			throw new IllegalStateException("Unable to remove creature " + creature + " from sectors (" + sectors + ")");
		}

		if (creatures.isEmpty()) {
			creatures = null;
		}

		if (log.isDebugEnabled()) {
			log.debug(creature + " stepped off " + element.getId() + " (" + direction + ")");
		}
	}

	public Creature.Height getTallestCreatureHeight() {
		if (creatures != null) {
			if (creatures.isEmpty()) {
				// Height is undefined
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

	public void removeCreature(Creature creature, Place place) {
		// The place can be null
		Validate.notNull(creature, "The given creature is null");

		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature + " can't step off " + element.getId());
		}

		if (place == null) {
			_removeCreature(creature);
		} else if (place instanceof Sector) {
			_removeCreature(creature, (Sector) place);
		} else if (place instanceof Direction) {
			_removeCreature(creature, (Direction) place);
		} else {
			throw new IllegalArgumentException("Unexpected place <" + place + ">");
		}
	}

	public Place removeCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature + " can't step off " + element.getId());
		}

		switch (creature.getSize()) {
		case ONE:
			final Sector sector = getSector(creature);

			if (sector == null) {
				throw new IllegalArgumentException("The given creature " + creature + " isn't on element " + element);
			}

			_removeCreature(creature, sector);

			return sector;
		case TWO:
			final Direction direction = getDirection(creature);

			if (direction == null) {
				throw new IllegalArgumentException("The given creature " + creature + " isn't on element " + element);
			}

			_removeCreature(creature, direction);

			return direction;
		case FOUR:
			_removeCreature(creature);

			return null;
		default:
			throw new UnsupportedOperationException("Unsupported creature size <" + creature.getSize() + ">");
		}
	}

	public void addCreature(Creature creature, Place place) {
		// The place can be null
		Validate.notNull(creature, "The given creature is null");

		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature + " can't step on " + element.getId());
		}

		if (place == null) {
			_addCreature(creature);
		} else if (place instanceof Sector) {
			_addCreature(creature, (Sector) place);
		} else if (place instanceof Direction) {
			_addCreature(creature, (Direction) place);
		} else {
			throw new IllegalArgumentException("Unexpected place <" + place + ">");
		}
	}

	public Place addCreature(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		if (!element.isTraversable(creature)) {
			throw new UnsupportedOperationException("The creature " + creature + " can't step on " + element.getId());
		}

		switch (creature.getSize()) {
		case ONE:
			final List<Sector> sectors = new ArrayList<Sector>(getFreeSectors());

			if (sectors.size() > 1) {
				Collections.shuffle(sectors);
			}

			final Sector sector = sectors.iterator().next();

			addCreature(creature, sector);

			return sector;
		case TWO:
			final List<Direction> directions = new ArrayList<Direction>(getFreeDirections());

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
			throw new UnsupportedOperationException("Unsupported creature size <" + creature.getSize() + ">");
		}
	}

	/**
	 * Indique si cet {@link Element} peut accueillir la {@link Creature} donn�e
	 * compte tenu de sa taille et de la place restante.
	 *
	 * @param creature
	 *            une {@link Creature}.
	 * @return si cet {@link Element} peut accueillir la {@link Creature} donn�e
	 *         compte tenu de sa taille et de la place restante.
	 */
	public boolean canHost(Creature creature) {
		Validate.notNull(creature);

		final int room = getFreeSectors().size();
		final int creatureSize = creature.getSize().value();

		if (creatureSize > room) {
			// Plus assez de place pour accueillir la cr�ature
			return false;
		}

		// Dans le cas o� la place restante est de 2 et la taille de la cr�ature
		// �galement de 2, il faut s'assurer qu'il s'agit de Sector voisines
		// qui permettent r�ellement d'accueillir la cr�ature !
		if ((room == 2) && (creatureSize == 2)) {
			final Iterator<Sector> iterator = getFreeSectors().iterator();

			final Sector cell1 = iterator.next();
			final Sector cell2 = iterator.next();

			if (!cell1.isNeighbourOf(cell2)) {
				return false;
			}
		}

		return true;
	}
}