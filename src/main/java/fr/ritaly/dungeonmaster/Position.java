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
package fr.ritaly.dungeonmaster;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;

/**
 * A position is a triplet of integers [x,y,z] representing a location inside
 * the game. This class is immutable to avoid nasty side effects when sharing
 * position objects.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Position {

	/** The x coordinate of this position. */
	public final int x;

	/** The y coordinate of this position. */
	public final int y;

	/** The z coordinate of this position. */
	public final int z;

	private final int hash;

	private final String toString;

	// FIXME Masquer ce constructeur pour r�utiliser les instances -> valueOf(x, y, z)
	public Position(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;

		int hashCode = 17;

		hashCode = (hashCode * 31) + x;
		hashCode = (hashCode * 31) + y;
		hashCode = (hashCode * 31) + z;

		this.hash = hashCode;

		this.toString = new StringBuilder(16).append('[').append(z).append(':')
				.append(x).append(',').append(y).append(']').toString();
	}

	@Override
	public final String toString() {
		return toString;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof Position) {
			final Position position = (Position) obj;

			return (this.x == position.x) && (this.y == position.y)
					&& (this.z == position.z);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	/**
	 * Returns whether this position and the given one have the same x and z
	 * coordinates.
	 *
	 * @param position
	 *            a position to test. Can't be null.
	 * @return whether this position and the given one have the same x and z
	 *         coordinates.
	 */
	public boolean isAlignedX(Position position) {
		Validate.notNull(position, "The given position is null");

		return (this.z == position.z) && (this.x == position.x);
	}

	/**
	 * Returns whether this position and the given one have the same y and z
	 * coordinates.
	 *
	 * @param position
	 *            a position to test. Can't be null.
	 * @return whether this position and the given one have the same y and z
	 *         coordinates.
	 */
	public boolean isAlignedY(Position position) {
		Validate.notNull(position, "The given position is null");

		return (this.z == position.z) && (this.y == position.y);
	}

	/**
	 * Returns whether this position and the given one have the same z and (x or
	 * y) coordinates.
	 *
	 * @param position
	 *            a position to test. Can't be null.
	 * @return whether this position and the given one have the same z and (x or
	 *         y) coordinates.
	 */
	public boolean isAligned(Position position) {
		Validate.notNull(position, "The given position is null");

		return (this.z == position.z)
				&& ((this.x == position.x) || (this.y == position.y));
	}

	public Position towards(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return direction.change(this);
	}

	/**
	 * Returns a list containing the 8 positions surrounding this one.
	 *
	 * @return a list of 8 positions. Never returns null.
	 */
	public List<Position> getSurroundingPositions() {
		// Rechercher les positions voisines dans un rayon de 1
		return getSurroundingPositions(1);
	}

	/**
	 * Returns a list containing the surrounding positions within the given
	 * radius.
	 *
	 * @param radius
	 *            a positive integer representing the radius around this
	 *            position used for selecting the surrounding positions.
	 *
	 * @return a list of surrounding positions. Never returns null.
	 */
	public List<Position> getSurroundingPositions(int radius) {
		Validate.isTrue(radius >= 1, "The given radius must be positive");

		// Positions surrounding the position P within a radius of 1 (8, +8)
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		// | 1 | P | 1 |
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+

		// Positions surrounding the position P within a radius of 2 (20, +12)
		// +---+---+---+---+---+
		// |   | 2 | 2 | 2 |   |
		// +---+---+---+---+---+
		// | 2 | 1 | 1 | 1 | 2 |
		// +---+---+---+---+---+
		// | 2 | 1 | P | 1 | 2 |
		// +---+---+---+---+---+
		// | 2 | 1 | 1 | 1 | 2 |
		// +---+---+---+---+---+
		// |   | 2 | 2 | 2 |   |
		// +---+---+---+---+---+

		// Positions surrounding the position P within a radius of 3 (36, +16)
		// +---+---+---+---+---+---+---+
		// |   |   | 3 | 3 | 3 |   |   |
		// +---+---+---+---+---+---+---+
		// |   | 3 | 2 | 2 | 2 | 3 |   |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | P | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | 1 | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// |   | 3 | 2 | 2 | 2 | 3 |   |
		// +---+---+---+---+---+---+---+
		// |   |   | 3 | 3 | 3 |   |   |
		// +---+---+---+---+---+---+---+

		// Positions surrounding the position P within a radius of 4 (68, +32)
		// +---+---+---+---+---+---+---+---+---+
		// |   |   | 4 | 4 | 4 | 4 | 4 |   |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   | 4 | 4 | 3 | 3 | 3 | 4 | 4 |   |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 4 | 3 | 2 | 2 | 2 | 3 | 4 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 3 | 2 | 1 | P | 1 | 2 | 3 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 3 | 2 | 1 | 1 | 1 | 2 | 3 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// | 4 | 4 | 3 | 2 | 2 | 2 | 3 | 4 | 4 |
		// +---+---+---+---+---+---+---+---+---+
		// |   | 4 | 4 | 3 | 3 | 3 | 4 | 4 |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   |   | 4 | 4 | 4 | 4 | 4 |   |   |
		// +---+---+---+---+---+---+---+---+---+

		// Compute the positions inside the given radius
		final List<Position> positions = new ArrayList<Position>(64);

//		// First non-optimized version
//		for (int x = this.x - radius; x <= this.x + radius; x++) {
//			for (int y = this.y - radius; y <= this.y + radius; y++) {
//				final Position position = new Position(x, y, 1);
//
//				if (this.equals(position)) {
//					continue;
//				}
//
//				final double distance = Utils.distance(this.x, this.y,
//						position.x, position.y);
//
//				if (distance <= radius + 0.5d) {
//					positions.add(position);
//				}
//			}
//		}

//		// Optimization: Use the symmetry to only explore 1/4th of the total
//		// space. Test one point and if within the radius, infer the 3 others
//		for (int x = 0; x <= radius; x++) {
//			for (int y = 0; y <= radius; y++) {
//				if ((x == 0) && (y == 0)) {
//					// On ignore la position centrale
//					continue;
//				}
//
//				final double distance = Utils.distance(0, 0, x, y);
//
//				if (distance <= radius + 0.5d) {
//					positions.add(new Position(this.x + x, this.y + y, this.z));
//
//					if (y != 0) {
//						positions.add(new Position(this.x + x, this.y - y,
//								this.z));
//					}
//
//					if (x != 0) {
//						positions.add(new Position(this.x - x, this.y + y,
//								this.z));
//
//						if (y != 0) {
//							positions.add(new Position(this.x - x, this.y - y,
//									this.z));
//						}
//					}
//				}
//			}
//		}

		// Optimization: Use the symmetry to only explore 1/4th of the total
		// space. Test one point and if within the radius, infer the 3 others
		// Optimization 2: On parcourt l'espace de l'extèrieur vers l'intérieur
		// et dès qu'on découvre un point dans le cercle, on prend de suite tous
		// les points "plus proches" pour une même valeur de x
		// Optimization 3: Méthode Utils.distance(int, int, int, int) inlinée
		// Optimization 4: Constructeur Position(int, int, int) optimisé
		for (int x = radius; x >= 0; x--) {
			boolean inside = false;

			for (int y = radius; y >= 0; y--) {
				if ((x == 0) && (y == 0)) {
					// On ignore la position centrale
					continue;
				}

				if (!inside) {
					final double distance = Math.sqrt((x * x) + (y * y));

					inside = (distance <= radius + 0.5d);
				}

				if (inside) {
					positions.add(new Position(this.x + x, this.y + y, this.z));

					if (y != 0) {
						positions.add(new Position(this.x + x, this.y - y, this.z));
					}

					if (x != 0) {
						positions.add(new Position(this.x - x, this.y + y, this.z));

						if (y != 0) {
							positions.add(new Position(this.x - x, this.y - y, this.z));
						}
					}
				}
			}
		}

		return positions;
	}

	/**
	 * TODO Translate this javadoc comment to english
	 *
	 * Retourne, pour cette {@link Position}, la liste des {@link Position}s qui
	 * lui sont visibles en regardant dans la direction donn�e. L'impl�mentation
	 * courante suppose que la port�e visuelle est de 3 lignes en profondeur.
	 *
	 * @param lookDirection
	 *            la {@link Direction} de regard.
	 * @return une {@link List} de {@link Position}s. Ne retourne jamais null.
	 */
	public List<Position> getVisiblePositions(Direction lookDirection) {
		// FIXME Rajouter un param�tre sightRange
		Validate.notNull(lookDirection, "The given direction is null");

		final List<Position> positions = new ArrayList<Position>();

		// Positions visibles depuis la position P en regardant vers le nord
		// +---+---+---+---+---+---+---+---+---+
		// |   |   |   |   |   |   |   |   |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   |   | V | V | V | V | V |   |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   |   |   | V | V | V |   |   |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   |   |   | V | V | V |   |   |   |
		// +---+---+---+---+---+---+---+---+---+
		// |   |   |   |   | P |   |   |   |   |
		// +---+---+---+---+---+---+---+---+---+

		switch (lookDirection) {
		case NORTH:
			// La cr�ature voit sur 3 rang�es de profondeur et sur une rang�e de
			// chaque c�t�
			positions.add(new Position(x - 1, y - 1, z));
			positions.add(new Position(x, y - 1, z));
			positions.add(new Position(x + 1, y - 1, z));

			positions.add(new Position(x - 1, y - 2, z));
			positions.add(new Position(x, y - 2, z));
			positions.add(new Position(x + 1, y - 2, z));

			// Pour la derni�re rang�e, la cr�ature voit sur 2 rang�es de chaque
			// c�t�
			positions.add(new Position(x - 2, y - 3, z));
			positions.add(new Position(x - 1, y - 3, z));
			positions.add(new Position(x, y - 3, z));
			positions.add(new Position(x + 1, y - 3, z));
			positions.add(new Position(x + 2, y - 3, z));
			break;
		case SOUTH:
			// La cr�ature voit sur 3 rang�es de profondeur et sur une rang�e de
			// chaque c�t�
			positions.add(new Position(x - 1, y + 1, z));
			positions.add(new Position(x, y + 1, z));
			positions.add(new Position(x + 1, y + 1, z));

			positions.add(new Position(x - 1, y + 2, z));
			positions.add(new Position(x, y + 2, z));
			positions.add(new Position(x + 1, y + 2, z));

			// Pour la derni�re rang�e, la cr�ature voit sur 2 rang�es de chaque
			// c�t�
			positions.add(new Position(x - 2, y + 3, z));
			positions.add(new Position(x - 1, y + 3, z));
			positions.add(new Position(x, y + 3, z));
			positions.add(new Position(x + 1, y + 3, z));
			positions.add(new Position(x + 2, y + 3, z));
			break;
		case WEST:
			// La cr�ature voit sur 3 rang�es de profondeur et sur une rang�e de
			// chaque c�t�
			positions.add(new Position(x - 1, y - 1, z));
			positions.add(new Position(x - 1, y, z));
			positions.add(new Position(x - 1, y + 1, z));

			positions.add(new Position(x - 2, y - 1, z));
			positions.add(new Position(x - 2, y, z));
			positions.add(new Position(x - 2, y + 1, z));

			// Pour la derni�re rang�e, la cr�ature voit sur 2 rang�es de chaque
			// c�t�
			positions.add(new Position(x - 3, y - 2, z));
			positions.add(new Position(x - 3, y - 1, z));
			positions.add(new Position(x - 3, y, z));
			positions.add(new Position(x - 3, y + 1, z));
			positions.add(new Position(x - 3, y + 2, z));
			break;
		case EAST:
			// La cr�ature voit sur 3 rang�es de profondeur et sur une rang�e de
			// chaque c�t�
			positions.add(new Position(x + 1, y - 1, z));
			positions.add(new Position(x + 1, y, z));
			positions.add(new Position(x + 1, y + 1, z));

			positions.add(new Position(x + 2, y - 1, z));
			positions.add(new Position(x + 2, y, z));
			positions.add(new Position(x + 2, y + 1, z));

			// Pour la derni�re rang�e, la cr�ature voit sur 2 rang�es de chaque
			// c�t�
			positions.add(new Position(x + 3, y - 2, z));
			positions.add(new Position(x + 3, y - 1, z));
			positions.add(new Position(x + 3, y, z));
			positions.add(new Position(x + 3, y + 1, z));
			positions.add(new Position(x + 3, y + 2, z));
			break;
		default:
			throw new UnsupportedOperationException("Unsupported direction "
					+ lookDirection);
		}

		return positions;
	}

	/**
	 * TODO Translate this javadoc comment to english
	 *
	 * Retourne les 4 {@link Position}s attaquables depuis cette
	 * {@link Position}.
	 *
	 * @return une {@link List} de {@link Position}s.
	 */
	public List<Position> getAttackablePositions() {
		// Retourner les positions attaquables situ�es dans un rayon de 1 pas
		return getAttackablePositions(1);
	}

	/**
	 * TODO Translate this javadoc comment to english
	 *
	 * Retourne les {@link Position}s attaquables depuis cette {@link Position}
	 * et situ�es dans un rayon de range pas dans les 4 directions.
	 *
	 * @return une {@link List} de {@link Position}s.
	 */
	public List<Position> getAttackablePositions(int range) {
		Validate.isTrue(range >= 1, "The given range " + range
				+ " must be positive");

		// Positions attaquables directement (range = 1) ou � distance
		// (range > 1) depuis la position P

		// +---+---+---+---+---+---+---+
		// |   |   |   | 3 |   |   |   |
		// +---+---+---+---+---+---+---+
		// |   |   |   | 2 |   |   |   |
		// +---+---+---+---+---+---+---+
		// |   |   |   | 1 |   |   |   |
		// +---+---+---+---+---+---+---+
		// | 3 | 2 | 1 | P | 1 | 2 | 3 |
		// +---+---+---+---+---+---+---+
		// |   |   |   | 1 |   |   |   |
		// +---+---+---+---+---+---+---+
		// |   |   |   | 2 |   |   |   |
		// +---+---+---+---+---+---+---+
		// |   |   |   | 3 |   |   |   |
		// +---+---+---+---+---+---+---+

		final List<Position> positions = new ArrayList<Position>(4 * range);

		for (int i = 1; i <= range; i++) {
			positions.add(new Position(x-i, y, z));
			positions.add(new Position(x+i, y, z));
			positions.add(new Position(x, y-i, z));
			positions.add(new Position(x, y+i, z));
		}

		return positions;
	}

	/**
	 * TODO Translate this javadoc comment to english
	 *
	 * A partir de cette position, retourne la direction dans laquelle il faut
	 * regarder / se tourner pour pointer vers la position cible donn�e.
	 *
	 * @param targetPosition
	 *            la position cible vers laquelle il faut se tourner.
	 * @return la direction dans laquelle il faut se tourner pour voir la
	 *         position cible donn�e.
	 */
	public Direction getDirectionTowards(Position targetPosition) {
		Validate.notNull(targetPosition, "The given direction is null");

		if (this.z != targetPosition.z) {
			// Positions situ�es sur des niveaux diff�rents, cas non support�
			return null;
		}

		if (isAlignedX(targetPosition)) {
			// Positions align�es le long de l'axe des X
			if (this.y < targetPosition.y) {
				// Target situ�e en bas de this
				return Direction.SOUTH;
			} else if (this.y > targetPosition.y) {
				// Target situ�e en haut de this
				return Direction.NORTH;
			} else {
				// this et target sont confondus
				return null;
			}
		} else if (isAlignedY(targetPosition)) {
			// Positions align�es le long de l'axe des Y
			if (this.x < targetPosition.x) {
				// Target situ�e � droite de this
				return Direction.EAST;
			} else if (this.x > targetPosition.x) {
				// Target situ�e � gauche de this
				return Direction.WEST;
			} else {
				// this et target sont confondus
				return null;
			}
		} else {
			// Les directions ne sont pas align�es. D�terminer une direction de
			// pr�f�rence � une autre

			// Valeur de x du vecteur permettant d'aller de this � target
			final int deltaX = targetPosition.x - this.x;

			// Valeur de y du vecteur permettant d'aller de this � target
			final int deltaY = targetPosition.y - this.y;

			if (deltaX == deltaY) {
				// Impossible de d�cider de mani�re non arbitraire, on tire une
				// direction au hasard
				final Direction[] directions = new Direction[2];

				// Rappel: deltaX ne peut �tre nul
				directions[0] = (deltaX > 0) ? Direction.EAST: Direction.WEST;

				// Rappel: deltaY ne peut �tre nul
				directions[1] = (deltaY > 0) ? Direction.SOUTH: Direction.NORTH;

				return directions[RandomUtils.nextInt(2)];
			}

			if (Math.abs(deltaX) > Math.abs(deltaY)) {
				// Direction de pr�f�rence le long de l'axe des X
				return (deltaX > 0) ? Direction.EAST: Direction.WEST;
			} else {
				// Direction de pr�f�rence le long de l'axe des Y
				return (deltaY > 0) ? Direction.SOUTH: Direction.NORTH;
			}
		}
	}

	public static void main(String[] args) {
		for (int radius = 1; radius <= 15; radius++) {
			final int width, height = width = (2 * radius) + 1;

			final Position center = new Position((width - 1) / 2,
					(height - 1) / 2, 1);
			final StringBuilder builder = new StringBuilder();

			int insideCount = 0;
			int outsideCount = 0;

			for (int x = 0; x < width; x++) {
				builder.append("+");
				builder.append(StringUtils.repeat("---+", width));
				builder.append("\n");

				builder.append("|");

				for (int y = 0; y < height; y++) {
					final Position position = new Position(x, y, 1);

					if (center.equals(position)) {
						builder.append(" P |");

						continue;
					}

					final double distance = Utils.distance(center.x, center.y,
							position.x, position.y);

					final boolean inside = (distance <= radius + 0.5d);

					if (inside) {
						builder.append(" ")
								.append(Integer.toHexString((int) Math
										.floor(distance))).append(" |");

						insideCount++;
					} else {
						builder.append("   |");

						outsideCount++;
					}

//					System.out.println("Position " + position + " (d: "
//							+ distance + ") -> " + inside);
				}

				builder.append("\n");
			}

			builder.append("+");
			builder.append(StringUtils.repeat("---+", width));
			builder.append("\n");

			System.out.println(builder);
			System.out.println();

			System.out.println("Radius: " + radius + " -> Inside: "
					+ insideCount + ", Outside: " + outsideCount);
		}

//		for (int j = 0; j < 10; j++) {
//			final long start = System.nanoTime();
//
//			final Position position = new Position(1,1,1);
//
//			for (int i = 0; i < 10000; i++) {
//				position.getSurroundingPositions(6);
//			}
//
//			System.out.println("Elapsed: "
//					+ ((System.nanoTime() - start) / 1000000) + " ms");
//		}
	}
}