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

import org.apache.commons.lang.math.RandomUtils;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Utils {

	/**
	 * Tire une valeur au hasard dans l'intervalle [min-max] donné et la
	 * retourne.
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int random(int min, int max) {
		if (min < 0) {
			throw new IllegalArgumentException("The given min <" + min
					+ "> must be positive");
		}
		if (max < 0) {
			throw new IllegalArgumentException("The given max <" + max
					+ "> must be positive");
		}
		if (min >= max) {
			throw new IllegalArgumentException("The given min <" + min
					+ "> must be greater than the max <" + max + ">");
		}

		return min + RandomUtils.nextInt(max + 1 - min);
	}
	
	public static int random(int max) {
		return random(0, max);
	}

	/**
	 * Borne la valeur donnée dans l'intervalle défini par les valeurs min et
	 * max données.
	 * 
	 * @param value
	 *            la valeur à borner.
	 * @param min
	 *            la borne min de l'intervalle de valeur autorisé.
	 * @param max
	 *            la borne max de l'intervalle de valeur autorisé.
	 * @return la valeur bornée.
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

		// Valeur déjà dans l'intervalle
		return value;
	}

	/**
	 * Indique si la valeur donnée est située dans l'intervalle de valeur min et
	 * max données.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static boolean inside(int value, int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("The given min <" + min
					+ "> must be greater than the max <" + max + ">");
		}

		return ((min <= value) && (value <= max));
	}

	public static double distance(int x1, int y1, int x2, int y2) {
		final int x = (x2 - x1);
		final int y = (y2 - y1);

		return Math.sqrt((x * x) + (y * y));
	}

	public static double volume(double distance) {
		// Tous les 3 mètres, le volume est divisé par deux
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

	public static void main(String[] args) {
		for (int i = 0; i < 6; i++) {
			System.out.println(volume(i) + " - " + attenuation(i));
		}

		System.out.println(angle(0, 0, 5, 0));
		System.out.println(angle(0, 0, 0, 5));
	}
}