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
 * Représente des coordonnées en 3 dimensions dans un donjon.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public final class Position {
	
	public final int x, y, z;

	private final int hash;

	private final String toString;

	// FIXME Masquer ce constructeur pour réutiliser les instances -> valueOf(x, y, z)
	public Position(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;

		this.toString = "[" + z + ":" + x + "," + y + "]";

		int hashCode = 17;

		hashCode = (hashCode * 31) + x;
		hashCode = (hashCode * 31) + y;
		hashCode = (hashCode * 31) + z;

		this.hash = hashCode;
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
	 * Indique si cette {@link Position} est alignée (le long de l'axe de X)
	 * avec la {@link Position} donnée.
	 * 
	 * @param position
	 *            une instance de {@link Position}.
	 * @return si cette {@link Position} est alignée (le long de l'axe de X)
	 *         avec la {@link Position} donnée.
	 */
	public boolean isAlignedX(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		return (this.z == position.z) && (this.x == position.x);
	}

	/**
	 * Indique si cette {@link Position} est alignée (le long de l'axe de Y)
	 * avec la {@link Position} donnée.
	 * 
	 * @param position
	 *            une instance de {@link Position}.
	 * @return si cette {@link Position} est alignée (le long de l'axe de Y)
	 *         avec la {@link Position} donnée.
	 */
	public boolean isAlignedY(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		return (this.z == position.z) && (this.y == position.y);
	}
	
	/**
	 * Indique si cette {@link Position} est alignée avec la {@link Position}
	 * donnée.
	 * 
	 * @param position
	 *            une instance de {@link Position}.
	 * @return si cette {@link Position} est alignée avec la {@link Position}
	 *         donnée.
	 */
	public boolean isAligned(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		return (this.z == position.z)
				&& ((this.x == position.x) || (this.y == position.y));
	}

	public Position towards(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return direction.change(this);
	}

	/**
	 * Retourne une liste contenant les 8 positions entourant cette
	 * {@link Position}.
	 * 
	 * @return une {@link List} de {@link Position}s.
	 */
	public List<Position> getSurroundingPositions() {
		// Rechercher les positions voisines dans un rayon de 1
		return getSurroundingPositions(1);
	}

	/**
	 * Retourne une liste contenant les positions entourant cette
	 * {@link Position} dans le rayon donné.
	 * 
	 * @param radius
	 *            un entier représentant le rayon en nombre de cases voisines.
	 * 
	 * @return une {@link List} de {@link Position}s.
	 */
	public List<Position> getSurroundingPositions(int radius) {
		Validate.isTrue(radius >= 1, "The given radius must be positive");
		
		// Positions entourant la position P dans un rayon de 1 (8, +8)
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		// | 1 | P | 1 |
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		
		// Positions entourant la position P dans un rayon de 2 (20, +12)
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
		
		// Positions entourant la position P dans un rayon de 3 (36, +16)
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
		
		// Positions entourant la position P dans un rayon de 4 (68, +32)
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
		
		// On calcule les positions situées dans le rayon donné
		final List<Position> positions = new ArrayList<Position>(64);
		
//		// Première version non optimisée
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
		
		// Optimisation: vu la symétrie de l'espace exploré, on en explore 1/4
		// et si le point et dans le cercle de rayon donné, on en déduit les 3
		// points associés par symétrie (C4)
		for (int x = 0; x <= radius; x++) {
			for (int y = 0; y <= radius; y++) {
				if ((x == 0) && (y == 0)) {
					// On ignore la position centrale
					continue;
				}
				
				final double distance = Utils.distance(0, 0, x, y);

				if (distance <= radius + 0.5d) {
					positions.add(new Position(this.x + x, this.y + y, this.z));
					
					if (y != 0) {
						// Ne considérer les positions symétriques que si les 2
						// ne sont pas confondues
						positions.add(new Position(this.x + x, this.y - y,
								this.z));
					}
					
					if (x != 0) {
						// Ne considérer les positions symétriques que si les 2
						// ne sont pas confondues
						positions.add(new Position(this.x - x, this.y + y,
								this.z));
						
						if (y != 0) {
							// Ne considérer les positions symétriques que si 
							// les 2 ne sont pas confondues
							positions.add(new Position(this.x - x, this.y - y,
									this.z));
						}
					}
				}
			}
		}
		
		return positions;
	}
	
	/**
	 * Retourne, pour cette {@link Position}, la liste des {@link Position}s qui
	 * lui sont visibles en regardant dans la direction donnée. L'implémentation
	 * courante suppose que la portée visuelle est de 3 lignes en profondeur.
	 * 
	 * @param lookDirection
	 *            la {@link Direction} de regard.
	 * @return une {@link List} de {@link Position}s. Ne retourne jamais null.
	 */
	public List<Position> getVisiblePositions(Direction lookDirection) {
		// FIXME Rajouter un paramètre sightRange
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
			// La créature voit sur 3 rangées de profondeur et sur une rangée de
			// chaque côté
			positions.add(new Position(x - 1, y - 1, z));
			positions.add(new Position(x, y - 1, z));
			positions.add(new Position(x + 1, y - 1, z));

			positions.add(new Position(x - 1, y - 2, z));
			positions.add(new Position(x, y - 2, z));
			positions.add(new Position(x + 1, y - 2, z));

			// Pour la dernière rangée, la créature voit sur 2 rangées de chaque
			// côté
			positions.add(new Position(x - 2, y - 3, z));
			positions.add(new Position(x - 1, y - 3, z));
			positions.add(new Position(x, y - 3, z));
			positions.add(new Position(x + 1, y - 3, z));
			positions.add(new Position(x + 2, y - 3, z));
			break;
		case SOUTH:
			// La créature voit sur 3 rangées de profondeur et sur une rangée de
			// chaque côté
			positions.add(new Position(x - 1, y + 1, z));
			positions.add(new Position(x, y + 1, z));
			positions.add(new Position(x + 1, y + 1, z));

			positions.add(new Position(x - 1, y + 2, z));
			positions.add(new Position(x, y + 2, z));
			positions.add(new Position(x + 1, y + 2, z));

			// Pour la dernière rangée, la créature voit sur 2 rangées de chaque
			// côté
			positions.add(new Position(x - 2, y + 3, z));
			positions.add(new Position(x - 1, y + 3, z));
			positions.add(new Position(x, y + 3, z));
			positions.add(new Position(x + 1, y + 3, z));
			positions.add(new Position(x + 2, y + 3, z));
			break;
		case WEST:
			// La créature voit sur 3 rangées de profondeur et sur une rangée de
			// chaque côté
			positions.add(new Position(x - 1, y - 1, z));
			positions.add(new Position(x - 1, y, z));
			positions.add(new Position(x - 1, y + 1, z));

			positions.add(new Position(x - 2, y - 1, z));
			positions.add(new Position(x - 2, y, z));
			positions.add(new Position(x - 2, y + 1, z));

			// Pour la dernière rangée, la créature voit sur 2 rangées de chaque
			// côté
			positions.add(new Position(x - 3, y - 2, z));
			positions.add(new Position(x - 3, y - 1, z));
			positions.add(new Position(x - 3, y, z));
			positions.add(new Position(x - 3, y + 1, z));
			positions.add(new Position(x - 3, y + 2, z));
			break;
		case EAST:
			// La créature voit sur 3 rangées de profondeur et sur une rangée de
			// chaque côté
			positions.add(new Position(x + 1, y - 1, z));
			positions.add(new Position(x + 1, y, z));
			positions.add(new Position(x + 1, y + 1, z));

			positions.add(new Position(x + 2, y - 1, z));
			positions.add(new Position(x + 2, y, z));
			positions.add(new Position(x + 2, y + 1, z));

			// Pour la dernière rangée, la créature voit sur 2 rangées de chaque
			// côté
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
	 * Retourne les 4 {@link Position}s attaquables depuis cette
	 * {@link Position}.
	 * 
	 * @return une {@link List} de {@link Position}s.
	 */
	public List<Position> getAttackablePositions() {
		// Retourner les positions attaquables situées dans un rayon de 1 pas
		return getAttackablePositions(1);
	}

	/**
	 * Retourne les {@link Position}s attaquables depuis cette {@link Position}
	 * et situées dans un rayon de range pas dans les 4 directions.
	 * 
	 * @return une {@link List} de {@link Position}s.
	 */
	public List<Position> getAttackablePositions(int range) {
		Validate.isTrue(range >= 1, "The given range " + range
				+ " must be positive");
		
		// Positions attaquables directement (range = 1) ou à distance 
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
	 * A partir de cette position, retourne la direction dans laquelle il faut
	 * regarder / se tourner pour pointer vers la position cible donnée.
	 * 
	 * @param targetPosition
	 *            la position cible vers laquelle il faut se tourner.
	 * @return la direction dans laquelle il faut se tourner pour voir la
	 *         position cible donnée.
	 */
	public Direction getDirectionTowards(Position targetPosition) {
		Validate.notNull(targetPosition, "The given direction is null");

		if (this.z != targetPosition.z) {
			// Positions situées sur des niveaux différents, cas non supporté
			return null;
		}

		if (isAlignedX(targetPosition)) {
			// Positions alignées le long de l'axe des X
			if (this.y < targetPosition.y) {
				// Target située en bas de this
				return Direction.SOUTH;
			} else if (this.y > targetPosition.y) {
				// Target située en haut de this
				return Direction.NORTH;
			} else {
				// this et target sont confondus
				return null;
			}
		} else if (isAlignedY(targetPosition)) {
			// Positions alignées le long de l'axe des Y
			if (this.x < targetPosition.x) {
				// Target située à droite de this
				return Direction.EAST;
			} else if (this.x > targetPosition.x) {
				// Target située à gauche de this
				return Direction.WEST;
			} else {
				// this et target sont confondus
				return null;
			}
		} else {
			// Les directions ne sont pas alignées. Déterminer une direction de
			// préférence à une autre
			
			// Valeur de x du vecteur permettant d'aller de this à target
			final int deltaX = targetPosition.x - this.x;
			
			// Valeur de y du vecteur permettant d'aller de this à target
			final int deltaY = targetPosition.y - this.y;
			
			if (deltaX == deltaY) {
				// Impossible de décider de manière non arbitraire, on tire une
				// direction au hasard
				final Direction[] directions = new Direction[2];
				
				// Rappel: deltaX ne peut être nul
				directions[0] = (deltaX > 0) ? Direction.EAST: Direction.WEST; 
				
				// Rappel: deltaY ne peut être nul
				directions[1] = (deltaY > 0) ? Direction.SOUTH: Direction.NORTH;

				return directions[RandomUtils.nextInt(2)];
			}
			
			if (Math.abs(deltaX) > Math.abs(deltaY)) {
				// Direction de préférence le long de l'axe des X
				return (deltaX > 0) ? Direction.EAST: Direction.WEST;
			} else {
				// Direction de préférence le long de l'axe des Y
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
		
//		final long start = System.nanoTime();
//		
//		for (int i = 0; i < 10000; i++) {
//			new Position(1,1,1).getSurroundingPositions(10);
//		}
//		
//		System.out.println("Elapsed: "
//				+ ((System.nanoTime() - start) / 1000000) + " ms");
	}
}