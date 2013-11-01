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
package fr.ritaly.dungeonmaster.magic;

/**
 * A rune is a magical "word" invoked when casting a spell. There are 4 rune
 * types used for casting spells.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public interface Rune {

	/**
	 * Enumerates the possible rune types. A type defines a family of 6 runes.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Type {
		POWER, ELEMENT, FORM, ALIGNMENT;
	}

	/**
	 * Returns the cost (in mana points) necessary for invoking this rune. This
	 * method is only relevant for a power rune.
	 *
	 * @return a positive integer representing a number of mana points.
	 */
	public int getCost();

	/**
	 * Returns the cost (in mana points) necessary for invoking this rune with
	 * the given power rune. This method is only relevant for a non-power rune
	 * (that is, an element, form or alignment rune).
	 *
	 * @param powerRune
	 *            the power rune used for invoking the rune.
	 * @return a positive integer representing a number of mana points.
	 */
	public int getCost(PowerRune powerRune);

	/**
	 * Returns the rune type (that is the family of runes this rune belongs to).
	 *
	 * @return the rune type. Never returns null.
	 */
	public Type getType();

	/**
	 * Returns the rune identifier (inside its family of runes) as an integer.
	 * The returned value is within [1,6]. The identifier represents the 1-based
	 * ordinal of this rune in its family.
	 *
	 * @return an integer within [1,6] identifying this rune inside its family.
	 */
	public int getId();
}
