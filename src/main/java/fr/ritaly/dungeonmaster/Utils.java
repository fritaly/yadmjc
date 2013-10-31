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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;

/**
 * Utility class.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Utils {

	/**
	 * Returns a random value within the specified range [min, max].
	 *
	 * @param min
	 *            the range's lower bound. Can't be negative.
	 * @param max
	 *            the range's upper bound. Can't be negative.
	 * @return a random integer within the specified range.
	 */
	public static int random(int min, int max) {
		Validate.isTrue(min >= 0, String.format("The given min %d must be positive", min));
		Validate.isTrue(max >= 0, String.format("The given max %d must be positive", max));
		Validate.isTrue(min < max, String.format("The given min %d must be lesser than the max %d", min, max));

		return min + RandomUtils.nextInt(max + 1 - min);
	}

	public static int random(int max) {
		return random(0, max);
	}

	/**
	 * Borne la valeur donn�e dans l'intervalle d�fini par les valeurs min et
	 * max donn�es.
	 *
	 * @param value
	 *            la valeur � borner.
	 * @param min
	 *            la borne min de l'intervalle de valeur autoris�.
	 * @param max
	 *            la borne max de l'intervalle de valeur autoris�.
	 * @return la valeur born�e.
	 */
	public static int bind(int value, int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("The given min <" + min
					+ "> must be greater than the max <" + max + ">");
		}

		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}

		// Valeur d�j� dans l'intervalle
		return value;
	}

	/**
	 * Tells whether the given value is inside the specified range [min, max].
	 *
	 * @param value
	 *            an integer representing the value to test.
	 * @param min
	 *            the range's lower bound.
	 * @param max
	 *            the range's upper bound.
	 * @return whether the given value is inside the specified range.
	 */
	public static boolean inside(int value, int min, int max) {
		Validate.isTrue(min < max, String.format("The given min %d must be lesser than the given max %d", min, max));

		return ((min <= value) && (value <= max));
	}

	/**
	 * Returns the distance between 2 points whose respective coordinates are
	 * [x1,y1] and [x2,y2] as a float.<br>
	 * <br>
	 * Note: The returned distance is <b>not</b> the Manhattan distance.
	 *
	 * @param x1
	 *            the x coordinate for the first point.
	 * @param y1
	 *            the y coordinate for the first point.
	 * @param x2
	 *            the x coordinate for the second point.
	 * @param y2
	 *            the y coordinate for the second point.
	 * @return the distance as a float.
	 */
	public static double distance(int x1, int y1, int x2, int y2) {
		final int x = (x2 - x1);
		final int y = (y2 - y1);

		return Math.sqrt((x * x) + (y * y));
	}

	public static double volume(double distance) {
		// Tous les 3 m�tres, le volume est divis� par deux
		return 1 / (Math.pow(2, (distance / 3)));
	}

	public static double attenuation(double distance) {
		return 1 - volume(distance);
	}

	// TODO
	public static double angle(Direction direction, int x1, int y1, int x2,
			int y2) {

		final double angle = angle(x1, y1, x2, y2);

		switch (direction) {
		case NORTH:
			return (Math.PI / 2) - angle;
		case EAST:
			return angle;
		case WEST:
			return (Math.PI) + angle;
		case SOUTH:
			return (3 * Math.PI / 2) - angle;
		default:
			throw new UnsupportedOperationException();
		}
	}

	public static double angle(int x1, int y1, int x2, int y2) {
		// Composantes du vecteur ?
		final int x = x2 - x1;
		final int y = y2 - y1;

		// Module du vecteur ?
		final double hypothenus = Math.sqrt((x * x) + (y * y));

		if (y > 0) {
			return Math.acos(x / hypothenus);
		} else {
			return -Math.acos(x / hypothenus);
		}
	}
}