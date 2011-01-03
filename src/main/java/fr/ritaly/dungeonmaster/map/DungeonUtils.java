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

public class DungeonUtils {

	public static Level parse(List<String> input, Dungeon dungeon, int levelNumber) {
		Validate.notNull(input);
		Validate.notNull(dungeon);
		Validate.isTrue(levelNumber >= 1);

		// Toutes les lignes doivent avoir une même longueur
		int length = -1;

		for (String line : input) {
			if (length != -1) {
				if (line.length() != length) {
					throw new IllegalArgumentException(
							"One of the given lines has an invalid length (Actual: "
									+ line.length() + ", Expected: " + length
									+ ")");
				}
			}

			length = line.length();
		}
		
		// Largeur du niveau
		final int width = length;
		
		// Hauteur du niveau
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
					throw new UnsupportedOperationException();
				}
			}
			
			y++;
		}
		
		return level;
	}
}