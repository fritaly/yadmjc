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
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.map.Element.Type;
import fr.ritaly.dungeonmaster.projectile.Projectile;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Level {

	private final Log log = LogFactory.getLog(Level.class);

	private final int level;

	private final int width;

	private final int height;

	private final Element[][] elements;

	private final Dungeon dungeon;

	/**
	 * Le multiplicateur par lequel l'expérience gagnée par un {@link Champion}
	 * doit être multipliée pour ce niveau-ci.
	 */
	private int experienceMultiplier = 1;

	/**
	 * La luminosité ambiante propre au niveau. Vaut en général 0 sauf pour les
	 * premiers niveaux d'un donjon.
	 */
	private int ambiantLight;

	public Level(Dungeon dungeon, int number, int height, int width) {
		if (dungeon == null) {
			throw new IllegalArgumentException("The given dungeon is null");
		}
		if (number <= 0) {
			throw new IllegalArgumentException("The given level number <"
					+ number + "> must be positive");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("The given height <" + height
					+ "> must be positive");
		}
		if (width <= 0) {
			throw new IllegalArgumentException("The given width <" + width
					+ "> must be positive");
		}

		this.dungeon = dungeon;
		this.level = number;
		this.height = height;
		this.width = width;
		this.elements = new Element[width][height];

		init();
	}

	public void init() {
		if (log.isDebugEnabled()) {
			log.debug("Initializing level " + level + " ...");
		}

		// Placer des murs autour du niveau et des vides à l'intérieur
		surround(ElementFactory.WALL_FACTORY);
		fill(ElementFactory.FLOOR_FACTORY);

		if (log.isInfoEnabled()) {
			log.info("Initialized level " + level);
		}
	}

	/**
	 * Supprime tous les éléments du niveau.
	 */
	public void clear() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				final Element removed = elements[x][y];

				if (removed != null) {
					// Détacher l'élément du niveau
					removed.setLevel(null);
					removed.setPosition(null);

					elements[x][y] = null;
				}
			}
		}
	}

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
			log.debug("Filled level " + level + " with " + factory.getType()
					+ " elements");
		}
	}

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
			log.debug("Surrounded level " + level + " with "
					+ factory.getType() + " elements");
		}
	}

	private void checkX(int x) {
		if ((x < 0) || (x > width - 1)) {
			throw new IllegalArgumentException(
					"The given x must be in range [0-" + (width - 1) + "]");
		}
	}

	private void checkY(int y) {
		if ((y < 0) || (y > height - 1)) {
			throw new IllegalArgumentException(
					"The given y must be in range [0-" + (height - 1) + "]");
		}
	}
	
	public Element getElement(int x, int y) {
		return getElement(x, y, true);
	}

	public Element getElement(int x, int y, boolean fail) {
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

	public int getLevel() {
		return level;
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
		if (element == null) {
			throw new IllegalArgumentException("The given element is null");
		}

		final Element removed = elements[x][y];

		if (removed != null) {
			// Détacher l'élément du niveau
			removed.setLevel(null);
			removed.setPosition(null);

			elements[x][y] = null;

			if (removed instanceof ClockListener) {
				Clock.getInstance().unregister((ClockListener) element);
			}
		}

		// Attacher l'élément au niveau
		element.setLevel(this);
		element.setPosition(new Position(x, y, level));

		// Permuter les éléments
		elements[x][y] = element;

		if (element instanceof ClockListener) {
			// Référencer le ClockListener
			Clock.getInstance().register((ClockListener) element);
		}
	}

	public Dungeon getDungeon() {
		return dungeon;
	}

	/**
	 * Retourne la luminosité ambiante propre au niveau.
	 * 
	 * @return un entier positif ou nul dans l'intervalle [0-255].
	 */
	public int getAmbiantLight() {
		return ambiantLight;
	}

	/**
	 * Définit la luminosité ambiante propre au niveau.
	 * 
	 * @param ambiantLight
	 *            un entier positif ou nul dans l'intervalle [0-255].
	 */
	public void setAmbiantLight(int ambiantLight) {
		if ((ambiantLight < 0) || (ambiantLight > Constants.MAX_LIGHT)) {
			throw new IllegalArgumentException("The given ambiant light <"
					+ ambiantLight + "> must be in range [0-"
					+ Constants.MAX_LIGHT + "]");
		}

		this.ambiantLight = ambiantLight;
	}

	public int getExperienceMultiplier() {
		return experienceMultiplier;
	}

	public void setExperienceMultiplier(int experienceMultiplier) {
		if (experienceMultiplier <= 0) {
			throw new IllegalArgumentException("The experience multiplier <"
					+ experienceMultiplier + "> must be positive");
		}

		this.experienceMultiplier = experienceMultiplier;
	}

	/**
	 * Indique si la {@link Position} donnée est située sur ce niveau et est
	 * valide (ses valeurs de x et y sont valides).
	 * 
	 * @param position
	 *            la {@link Position} à tester.
	 * @return si la {@link Position} donnée est située sur ce niveau et est
	 *         valide (ses valeurs de x et y sont valides).
	 */
	public boolean contains(Position position) {
		Validate.notNull(position, "The given position is null");

		if (position.z != level) {
			// C'est une position sur un niveau différent
			return false;
		}

		if ((position.x < 0) || (position.x > width - 1)) {
			// Valeur x hors intervalle
			return false;
		}

		if ((position.y < 0) || (position.y > height - 1)) {
			// Valeur y hors intervalle
			return false;
		}

		// Position située sur ce niveau et valide
		return true;
	}

	public void validate() throws ValidationException {
		// Tous les éléments doivent être positionnés !
		for (int x = 0; x < width; x++) {
			final boolean wallX = (x == 0) || (x == width - 1);

			for (int y = 0; y < height; y++) {
				final boolean wallY = (y == 0) || (y == height - 1);

				final Element element = elements[x][y];

				if (element == null) {
					// Il ne doit y avoir aucun élément nul
					throw new ValidationException("The element at [" + x + ","
							+ y + "] isn't set");
				}

				if (wallX || wallY) {
					// Il doit y avoir des "murs" tout autour du niveau
					if (!element.isConcrete()) {
						throw new ValidationException("The element at [" + x
								+ "," + y + "] must be concrete");
					}
				}

				element.validate();
			}
		}
	}

	public String draw() {
		final StringBuilder builder = new StringBuilder(1024);

		for (int x = 0; x < width; x++) {
			if (x == 0) {
				builder.append("+");

				for (int y = 0; y < height; y++) {
					builder.append("----+");
				}

				builder.append("\n");
			}

			builder.append("|");

			for (int y = 0; y < height; y++) {
				final Element element = getElement(x, y);

				builder.append(" ");

				if (element != null) {
					// Pour alléger le résultat généré, les sols sont
					// représentés comme ".."
					if (Type.FLOOR.equals(element.getType())) {
						builder.append("..");
					} else {
						builder.append(element.getCaption());
					}
				} else {
					builder.append("??");
				}

				builder.append(" |");
			}

			builder.append("\n");
			builder.append("+");

			for (int y = 0; y < height; y++) {
				builder.append("----+");
			}

			// Fin de rangée
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

	// TODO Méthode de récupération des projectiles du niveau
}