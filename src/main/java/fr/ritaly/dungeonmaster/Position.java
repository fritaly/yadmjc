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

import org.apache.commons.lang.Validate;

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
	 * Indique si cette {@link Position} est proche de la {@link Position}
	 * donnée. Deux {@link Position}s sont proches quand le nombre de mouvements
	 * nécessaire pour passer de l'une à l'autre est égal à 1.
	 * 
	 * @param position
	 *            une instance de {@link Position}.
	 * @return
	 */
	public boolean isCloseTo(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		final boolean sameX = (this.x == position.x);
		final boolean sameY = (this.y == position.y);
		final boolean sameZ = (this.z == position.z);
		final boolean nearX = (Math.abs(this.x - position.x) == 1);
		final boolean nearY = (Math.abs(this.y - position.y) == 1);

		return sameZ
				&& ((sameX && nearY) || (nearX && sameY) || (nearX && nearY));
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

		return this.x == position.x;
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

		return this.y == position.y;
	}

	public Position towards(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		return direction.change(this);
	}

	public Position getUpper() {
		return towards(Direction.UP);
	}

	public Position getLower() {
		return towards(Direction.DOWN);
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
		
		final List<Position> positions = new ArrayList<Position>(16);
		
		// Positions entourant la position P dans un rayon de 1
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		// | 1 | P | 1 |
		// +---+---+---+
		// | 1 | 1 | 1 |
		// +---+---+---+
		
		for (int a = this.x - 1; a <= this.x + 1; a++) {
			for (int b = this.y - 1; b <= this.y + 1; b++) {
				positions.add(new Position(a, b, this.z));
			}
		}
			
		// Retirer la position courante
		positions.remove(this);
			
		if (radius == 1) {
			return positions;
		}
		
		// Positions entourant la position P dans un rayon de 2
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
		
		positions.add(new Position(this.x-2, this.y-1, this.z));
		positions.add(new Position(this.x-2, this.y, this.z));
		positions.add(new Position(this.x-2, this.y+1, this.z));
		
		positions.add(new Position(this.x+2, this.y-1, this.z));
		positions.add(new Position(this.x+2, this.y, this.z));
		positions.add(new Position(this.x+2, this.y+1, this.z));
		
		positions.add(new Position(this.x-1, this.y-2, this.z));
		positions.add(new Position(this.x, this.y-2, this.z));
		positions.add(new Position(this.x+1, this.y-2, this.z));
		
		positions.add(new Position(this.x-1, this.y+2, this.z));
		positions.add(new Position(this.x, this.y+2, this.z));
		positions.add(new Position(this.x+1, this.y+2, this.z));
		
		if (radius == 2) {
			return positions;
		}
		
		// Positions entourant la position P dans un rayon de 3
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
		
		positions.add(new Position(this.x-3, this.y-1, this.z));
		positions.add(new Position(this.x-3, this.y, this.z));
		positions.add(new Position(this.x-3, this.y+1, this.z));
		
		positions.add(new Position(this.x+3, this.y-1, this.z));
		positions.add(new Position(this.x+3, this.y, this.z));
		positions.add(new Position(this.x+3, this.y+1, this.z));
		
		positions.add(new Position(this.x-2, this.y-2, this.z));
		positions.add(new Position(this.x-2, this.y+2, this.z));
		
		positions.add(new Position(this.x+2, this.y-2, this.z));
		positions.add(new Position(this.x+2, this.y+2, this.z));
		
		positions.add(new Position(this.x-1, this.y-3, this.z));
		positions.add(new Position(this.x, this.y-3, this.z));
		positions.add(new Position(this.x+1, this.y-3, this.z));
		
		positions.add(new Position(this.x-1, this.y+3, this.z));
		positions.add(new Position(this.x, this.y+3, this.z));
		positions.add(new Position(this.x+1, this.y+3, this.z));
		
		if (radius == 3) {
			return positions;
		}
		
		throw new UnsupportedOperationException("Unsupported radius " + radius);
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
		case SOUTH:
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
		case WEST:
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
		case EAST:
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
		Validate.isTrue(range > 1, "The given range " + range
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
}