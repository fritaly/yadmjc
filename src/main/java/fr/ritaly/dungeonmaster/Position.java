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

	/**
	 * The z coordinate of this position. Represents the level this position is
	 * on.
	 */
	public final int z;

	private final int hash;

	private final String toString;

	// FIXME Create a factory method valueOf(x,y,z) to reuse instances ?
	public Position(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;

		int hashCode = 17;

		hashCode = (hashCode * 31) + x;
		hashCode = (hashCode * 31) + y;
		hashCode = (hashCode * 31) + z;

		this.hash = hashCode;
		this.toString = String.format("[%d:%d,%d]", z, x, y);
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

			return (this.x == position.x) && (this.y == position.y) && (this.z == position.z);
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

		return (this.z == position.z) && ((this.x == position.x) || (this.y == position.y));
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
		// Return the surrounding positions within 1 step
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
	 * Returns the positions visible from this position when looking at towards
	 * the given direction. The sight range for champions is typically 3 steps
	 * forward. The returned list always contains 11 positions corresponding to
	 * the theorically visible positions. Nothing guarantees those positions do
	 * exist in the actual dungeon (for instance if the party is looking at a
	 * wall).<br>
	 * <br>
	 * Example when looking north:<br>
	 * <br>
	 * <table cellspacing="1" cellpadding="5" border="1">
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>V</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>P</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * </table>
	 *
	 * @param lookDirection
	 *            the direction of look. Can't be null.
	 * @return a list of 11 positions. Never returns null.
	 */
	public List<Position> getVisiblePositions(Direction lookDirection) {
		// FIXME Add a sightRange parameter ? Because creatures can have a longer sight range
		Validate.notNull(lookDirection, "The given direction is null");

		final List<Position> positions = new ArrayList<Position>(11);

		// The positions visible from P when looking towards North
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
			positions.add(new Position(x - 1, y - 1, z));
			positions.add(new Position(x, y - 1, z));
			positions.add(new Position(x + 1, y - 1, z));

			positions.add(new Position(x - 1, y - 2, z));
			positions.add(new Position(x, y - 2, z));
			positions.add(new Position(x + 1, y - 2, z));

			positions.add(new Position(x - 2, y - 3, z));
			positions.add(new Position(x - 1, y - 3, z));
			positions.add(new Position(x, y - 3, z));
			positions.add(new Position(x + 1, y - 3, z));
			positions.add(new Position(x + 2, y - 3, z));
			break;
		case SOUTH:
			positions.add(new Position(x - 1, y + 1, z));
			positions.add(new Position(x, y + 1, z));
			positions.add(new Position(x + 1, y + 1, z));

			positions.add(new Position(x - 1, y + 2, z));
			positions.add(new Position(x, y + 2, z));
			positions.add(new Position(x + 1, y + 2, z));

			positions.add(new Position(x - 2, y + 3, z));
			positions.add(new Position(x - 1, y + 3, z));
			positions.add(new Position(x, y + 3, z));
			positions.add(new Position(x + 1, y + 3, z));
			positions.add(new Position(x + 2, y + 3, z));
			break;
		case WEST:
			positions.add(new Position(x - 1, y - 1, z));
			positions.add(new Position(x - 1, y, z));
			positions.add(new Position(x - 1, y + 1, z));

			positions.add(new Position(x - 2, y - 1, z));
			positions.add(new Position(x - 2, y, z));
			positions.add(new Position(x - 2, y + 1, z));

			positions.add(new Position(x - 3, y - 2, z));
			positions.add(new Position(x - 3, y - 1, z));
			positions.add(new Position(x - 3, y, z));
			positions.add(new Position(x - 3, y + 1, z));
			positions.add(new Position(x - 3, y + 2, z));
			break;
		case EAST:
			positions.add(new Position(x + 1, y - 1, z));
			positions.add(new Position(x + 1, y, z));
			positions.add(new Position(x + 1, y + 1, z));

			positions.add(new Position(x + 2, y - 1, z));
			positions.add(new Position(x + 2, y, z));
			positions.add(new Position(x + 2, y + 1, z));

			positions.add(new Position(x + 3, y - 2, z));
			positions.add(new Position(x + 3, y - 1, z));
			positions.add(new Position(x + 3, y, z));
			positions.add(new Position(x + 3, y + 1, z));
			positions.add(new Position(x + 3, y + 2, z));
			break;
		default:
			throw new UnsupportedOperationException("Unsupported direction " + lookDirection);
		}

		return positions;
	}

	/**
	 * Returns the 4 neighbour positions that can be directly attacked from this
	 * position.
	 *
	 * Schematically:<br>
	 * <br>
	 * <table cellspacing="1" cellpadding="5" border="1">
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>X</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>X</td>
	 * <td>P</td>
	 * <td>X</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>X</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * </table>
	 *
	 * @return a list of 4 positions. Never returns null.
	 */
	public List<Position> getAttackablePositions() {
		// Returns the attackable positions within a range of 1
		return getAttackablePositions(1);
	}

	/**
	 * Returns the positions that can be attacked from this position within the
	 * given range.
	 *
	 * Schematically for a range of 3 steps:<br>
	 * <br>
	 * <table cellspacing="1" cellpadding="5" border="1">
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>3</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>2</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>1</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>2</td>
	 * <td>1</td>
	 * <td>P</td>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>3</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>1</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>2</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * <tr>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>3</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * <td>&nbsp;</td>
	 * </tr>
	 * </table>
	 *
	 * @param range
	 *            an integer representing a range as a number of steps. Must be
	 *            positive.
	 *
	 * @return a list of 4 positions. Never returns null.
	 */
	public List<Position> getAttackablePositions(int range) {
		Validate.isTrue(range >= 1, String.format("The given range %d must be positive", range));

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
	 * From this position, returns the direction into which one should look to
	 * point towards the given position.
	 *
	 * @param targetPosition
	 *            the target position to look at. Can't be null.
	 * @return a direction (north, south, etc) where the given target position
	 *         is relative to this position or null if both positions aren't
	 *         located on the same level or are equal.
	 */
	public Direction getDirectionTowards(Position targetPosition) {
		Validate.notNull(targetPosition, "The given direction is null");

		if (this.z != targetPosition.z) {
			// The 2 positions aren't on the same level
			return null;
		}

		if (isAlignedX(targetPosition)) {
			// The positions have the same x coordinate
			if (this.y < targetPosition.y) {
				// The target is below this position
				return Direction.SOUTH;
			} else if (this.y > targetPosition.y) {
				// The target is above this position
				return Direction.NORTH;
			} else {
				// Same (x,y,z) coordinates
				return null;
			}
		} else if (isAlignedY(targetPosition)) {
			// The positions have the same y coordinate
			if (this.x < targetPosition.x) {
				// The target is on the right of this position
				return Direction.EAST;
			} else if (this.x > targetPosition.x) {
				// The target is on the left of this position
				return Direction.WEST;
			} else {
				// Same (x,y,z) coordinates
				return null;
			}
		} else {
			// The 2 positions aren't aligned. Choose one preferred direction

			// Difference of x between the 2 positions ?
			final int deltaX = targetPosition.x - this.x;

			// Difference of y between the 2 positions ?
			final int deltaY = targetPosition.y - this.y;

			if (deltaX == deltaY) {
				// We can't decide, pick one direction randomly
				final Direction[] directions = new Direction[2];

				// Rappel: deltaX can't be zero
				directions[0] = (deltaX > 0) ? Direction.EAST: Direction.WEST;

				// Rappel: deltaY can't be zero
				directions[1] = (deltaY > 0) ? Direction.SOUTH: Direction.NORTH;

				// Toss one direction
				return directions[RandomUtils.nextInt(2)];
			}

			if (Math.abs(deltaX) > Math.abs(deltaY)) {
				// The preferred direction is along the x axis
				return (deltaX > 0) ? Direction.EAST: Direction.WEST;
			} else {
				// The preferred direction is along the y axis
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