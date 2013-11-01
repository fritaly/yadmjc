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
 * Enumerates the 6 power runes from the weakest to the strongest rune.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum PowerRune implements Rune {
	LO, UM, ON, EE, PAL, MON;

	@Override
	public int getCost() {
		return getPowerLevel();
	}

	@Override
	public int getCost(PowerRune powerRune) {
		throw new UnsupportedOperationException("Method only supported for a non-power rune");
	}

	@Override
	public Type getType() {
		return Type.POWER;
	}

	/**
	 * TODO Javadoc this method
	 */
	public int getPowerLevel() {
		return ordinal() + 1;
	}

	@Override
	public int getId() {
		// The id is always within [1,6]
		return ordinal() + 1;
	}

	/**
	 * Returns the difficulty multiplier associated to this power rune. The
	 * stronger the power rune, the more difficult casting a spell. This method
	 * quantifies how much more difficult it is to cast a spell depending on the
	 * power rune used.
	 *
	 * @return a positive integer representing the relative difficulty to cast a
	 *         spell when using this power rune.
	 */
	public int getDifficultyMultiplier() {
		// LO (8), UM (12), ON (16), EE (20), PAL (24), MON (28)
		return 8 + ordinal() * 4;
	}
}
