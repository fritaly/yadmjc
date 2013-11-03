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

import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Utility class providing some convenient methods when dealing with dungeons.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class DungeonUtils {

	/**
	 * Convenient method to build a dungeon level from a given list of lines
	 * (that will be parsed).
	 *
	 * @param input
	 *            a list of lines representing the data to parse. A 'W'
	 *            character denotes a wall. A blank (' ') character denotes a
	 *            floor tile. Other characters are unsupported. Every lines must
	 *            have the same width.
	 * @param dungeon
	 *            the dungeon where the level will be created.
	 * @param levelNumber
	 *            the number of level to build.
	 * @return the built level. Never returns null.
	 */
	public static Level parse(final List<String> input, final Dungeon dungeon, final int levelNumber) {
		Validate.notNull(input, "The given list of lines is null");
		Validate.notNull(dungeon, "The given dungeon is null");
		Validate.isTrue(levelNumber >= 1, String.format("The given level number %d must be positive", levelNumber));

		// All the lines must have the same length
		int length = -1;

		for (String line : input) {
			if (length != -1) {
				if (line.length() != length) {
					throw new IllegalArgumentException("One of the given lines has an invalid length (Actual: " + line.length()
							+ ", Expected: " + length + ")");
				}
			}

			length = line.length();
		}

		// The level's width
		final int width = length;

		// The level's height
		final int height = input.size();

		final Level level = dungeon.createLevel(levelNumber, height, width);

		int y = 0;
		for (String line : input) {
			for (int x = 0; x < line.length(); x++) {
				final char c = line.charAt(x);

				switch (c) {
				case 'W':
					level.setElement(x, y, new Wall());
					break;
				case ' ':
					level.setElement(x, y, new Floor());
					break;
				default:
					throw new UnsupportedOperationException("Unexpected character '" + c + "'");
				}
			}

			y++;
		}

		return level;
	}
}