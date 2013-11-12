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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Constants;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.map.Element.Type;
import fr.ritaly.dungeonmaster.projectile.Projectile;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Level {

	private final Log log = LogFactory.getLog(Level.class);

	/**
	 * The level's number.
	 */
	private final int number;

	/**
	 * The level's width.
	 */
	private final int width;

	/**
	 * The level's width.
	 */
	private final int height;

	/**
	 * The elements inside this level.
	 */
	private final Element[][] elements;

	/**
	 * The dungeon this level is bound to.
	 */
	private final Dungeon dungeon;

	/**
	 * Each level defines an experience multiplier which defines how many xp
	 * points a champion earns when on this level.
	 */
	private int experienceMultiplier = 1;

	/**
	 * The level's ambient light. Usually set to 0 (pitch dark) except for the
	 * first levels in the game. Value within [0,255].
	 */
	private int ambientLight;

	public Level(Dungeon dungeon, int number, int height, int width) {
		Validate.notNull(dungeon, "The given dungeon is null");
		Validate.isTrue(number > 0, String.format("The given level number %d must be positive", number));
		Validate.isTrue(height > 0, String.format("The given level height %d must be positive", height));
		Validate.isTrue(width > 0, String.format("The given level width %d must be positive", width));

		this.dungeon = dungeon;
		this.number = number;
		this.height = height;
		this.width = width;
		this.elements = new Element[width][height];

		init();
	}

	public void init() {
		if (log.isDebugEnabled()) {
			log.debug("Initializing level " + number + " ...");
		}

		// Put walls all around the level and floor tiles in between
		surround(ElementFactory.WALL_FACTORY);
		fill(ElementFactory.FLOOR_FACTORY);

		if (log.isInfoEnabled()) {
			log.info("Initialized level " + number);
		}
	}

	/**
	 * Removes all the level elements.
	 */
	public void clear() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				final Element removed = elements[x][y];

				if (removed != null) {
					// Detach the element from its parent level
					removed.setLevel(null);
					removed.setPosition(null);

					elements[x][y] = null;
				}
			}
		}
	}

	/**
	 * Fills the level with the elements created by the given factory. This
	 * won't touch the elements on the borders.
	 *
	 * @param factory
	 *            a factory of elements used to fill the level. Can't be null.
	 */
	public void fill(ElementFactory factory) {
		Validate.notNull(factory, "The given element factory is null");

		for (int x = 0; x < width; x++) {
			final boolean borderX = (x == 0) || (x == width - 1);

			for (int y = 0; y < height; y++) {
				final boolean borderY = (y == 0) || (y == height - 1);

				if (!borderX && !borderY) {
					setElement(x, y, factory.createElement());
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Filled level " + number + " with " + factory.getType() + " elements");
		}
	}

	/**
	 * Surrounds the level with the elements created by the given factory. This
	 * will only touch the elements on the borders.
	 *
	 * @param factory
	 *            a factory of elements used to surround the level. Can't be
	 *            null.
	 */
	public void surround(ElementFactory factory) {
		Validate.notNull(factory, "The given element factory is null");

		for (int x = 0; x < width; x++) {
			final boolean borderX = (x == 0) || (x == width - 1);

			for (int y = 0; y < height; y++) {
				final boolean borderY = (y == 0) || (y == height - 1);

				if (borderX || borderY) {
					setElement(x, y, factory.createElement());
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Surrounded level " + number + " with " + factory.getType() + " elements");
		}
	}

	private void checkX(int x) {
		if ((x < 0) || (x > width - 1)) {
			throw new IllegalArgumentException("The given x must be in range [0-" + (width - 1) + "]");
		}
	}

	private void checkY(int y) {
		if ((y < 0) || (y > height - 1)) {
			throw new IllegalArgumentException("The given y must be in range [0-" + (height - 1) + "]");
		}
	}

	/**
	 * Returns the element located at coordinated (x,y).
	 *
	 * @param x
	 *            the x coordinate.
	 * @param y
	 *            the y coordinate.
	 * @return the element found or null.
	 */
	public Element getElement(int x, int y) {
		return getElement(x, y, true);
	}

	private Element getElement(int x, int y, boolean fail) {
		if (fail) {
			checkX(x);
			checkY(y);

			return elements[x][y];
		} else {
			if ((x >= 0) && (x <= width - 1) && (y >= 0) && (y <= height - 1)) {
				return elements[x][y];
			}

			return null;
		}
	}

	public int getNumber() {
		return number;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setElement(int x, int y, Element element) {
		checkX(x);
		checkY(y);
		Validate.notNull(element, "The given element is null");

		final Element removed = elements[x][y];

		if (removed != null) {
			// Detach the element from its parent level
			removed.setLevel(null);
			removed.setPosition(null);

			elements[x][y] = null;

			if (removed instanceof ClockListener) {
				Clock.getInstance().unregister((ClockListener) element);
			}
		}

		// Attach the new element to this level
		element.setLevel(this);
		element.setPosition(new Position(x, y, number));

		// Swap the elements
		elements[x][y] = element;

		if (element instanceof ClockListener) {
			Clock.getInstance().register((ClockListener) element);
		}
	}

	public Dungeon getDungeon() {
		return dungeon;
	}

	public int getAmbientLight() {
		return ambientLight;
	}

	public void setAmbientLight(int ambientLight) {
		if ((ambientLight < 0) || (ambientLight > Constants.MAX_LIGHT)) {
			throw new IllegalArgumentException("The given ambient light <" + ambientLight + "> must be in range [0-"
					+ Constants.MAX_LIGHT + "]");
		}

		this.ambientLight = ambientLight;
	}

	public int getExperienceMultiplier() {
		return experienceMultiplier;
	}

	public void setExperienceMultiplier(int experienceMultiplier) {
		Validate.isTrue(experienceMultiplier > 0, "The experience multiplier <" + experienceMultiplier + "> must be positive");

		this.experienceMultiplier = experienceMultiplier;
	}

	/**
	 * Tells whether this level contains the given position.
	 *
	 * @param position
	 *            the position to test. Can't be null.
	 * @return whether this level contains the given position.
	 */
	public boolean contains(Position position) {
		Validate.notNull(position, "The given position is null");

		if (position.z != number) {
			return false;
		}

		if ((position.x < 0) || (position.x > width - 1)) {
			return false;
		}

		if ((position.y < 0) || (position.y > height - 1)) {
			return false;
		}

		return true;
	}

	/**
	 * Validates the level.
	 *
	 * @throws ValidationException
	 *             if the level isn't valid.
	 */
	public void validate() throws ValidationException {
		for (int x = 0; x < width; x++) {
			final boolean borderX = (x == 0) || (x == width - 1);

			for (int y = 0; y < height; y++) {
				final boolean borderY = (y == 0) || (y == height - 1);

				final Element element = elements[x][y];

				if (element == null) {
					// All elements should be set
					throw new ValidationException("The element at [" + x + "," + y + "] isn't set");
				}

				if (borderX || borderY) {
					// The border elements must be concrete (walls, not floor tiles)
					if (!element.isConcrete()) {
						throw new ValidationException("The element at [" + x + "," + y + "] must be concrete");
					}
				}

				element.validate();
			}
		}
	}

	/**
	 * Renders the level as a string.
	 *
	 * @return the rendered level.
	 */
	public String draw() {
		return draw(null);
	}

	/**
	 * Renders the level as a string and highlights the path consisting of the
	 * following elements.
	 *
	 * @param path
	 *            a list of elements representing the path to render. Can be
	 *            null.
	 * @return a string representing the rendered level.
	 */
	public String draw(List<Element> path) {
		final StringBuilder builder = new StringBuilder(1024);

		final Element start;
		final Element goal;
		final boolean pathDefined = ((path != null) && (path.size() >= 2));

		if (pathDefined) {
			start = path.get(0);
			goal = path.get(path.size() - 1);
		} else {
			start = null;
			goal = null;
		}

		for (int y = 0; y < height; y++) {
			if (y == 0) {
				builder.append("+");

				for (int x = 0; x < width; x++) {
					builder.append("---+");
				}

				builder.append("\n");
			}

			builder.append("|");

			for (int x = 0; x < width; x++) {
				final Element element = getElement(x, y);

				builder.append(" ");

				final boolean drawn;

				if (pathDefined) {
					if (element.equals(start)) {
						builder.append("S");

						drawn = true;
					} else if (element.equals(goal)) {
						builder.append("G");

						drawn = true;
					} else if (path.contains(element)) {
						builder.append(".");

						drawn = true;
					} else {
						drawn = false;
					}
				} else {
					drawn = false;
				}

				if (!drawn) {
					if (element != null) {
						// The floor tiles are rendered as " "
						if (Type.FLOOR.equals(element.getType())) {
							builder.append(" ");
						} else {
							builder.append(element.getSymbol());
						}
					} else {
						builder.append("?");
					}
				}

				builder.append(" |");
			}

			builder.append("\n");
			builder.append("+");

			for (int x = 0; x < width; x++) {
				builder.append("---+");
			}

			// End of a row
			builder.append("\n");
		}

		return builder.toString();
	}

	/**
	 * Returns the projectiles located on this {@link Level}.
	 *
	 * @return a {@link List} of {@link Projectile}s. Never returns null.
	 */
	public List<Projectile> getProjectiles() {
		final List<Projectile> projectiles = new ArrayList<Projectile>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				projectiles.addAll(getElement(x, y).getProjectiles().values());
			}
		}

		return projectiles;
	}

	/**
	 * Returns the creatures located on this {@link Level}.
	 *
	 * @return a {@link List} of {@link Creatures}s. Never returns null.
	 */
	public List<Creature> getCreatures() {
		final List<Creature> creatures = new ArrayList<Creature>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				creatures.addAll(getElement(x, y).getCreatures());
			}
		}

		return creatures;
	}

	/**
	 * Returns the number of creatures located on this level.
	 *
	 * @return a number of creatures.
	 */
	public int getCreatureCount() {
		int count = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				count += getElement(x, y).getCreatureCount();
			}
		}

		return count;
	}

	// TODO Method to retrieve the projectiles on a level

	/**
	 * Returns the elements corresponding to the given positions.
	 *
	 * @param positions
	 *            a list of positions. Can't be null.
	 * @return a list of elements. Never returns null.
	 */
	public List<Element> getElements(List<Position> positions) {
		Validate.notNull(positions, "The given list of positions is null");

		final List<Element> result = new ArrayList<Element>(positions.size());

		for (Position position : positions) {
			if (position.z != this.number) {
				// Position located on another level, skip it
				continue;
			}

			final Element element = getElement(position.x, position.y, false);

			if (element != null) {
				result.add(element);
			}
		}

		return result;
	}
}