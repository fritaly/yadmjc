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

	public Position getNorthernPosition() {
		return towards(Direction.NORTH);
	}

	public Position getSouthernPosition() {
		return towards(Direction.SOUTH);
	}

	public Position getWesternPosition() {
		return towards(Direction.WEST);
	}

	public Position getEasternPosition() {
		return towards(Direction.EAST);
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
		final List<Position> positions = new ArrayList<Position>(9);

		for (int a = this.x - 1; a <= this.x + 1; a++) {
			for (int b = this.y - 1; b <= this.y + 1; b++) {
				positions.add(new Position(a, b, this.z));
			}
		}

		// Retirer la position courante
		positions.remove(this);

		return positions;
	}
}