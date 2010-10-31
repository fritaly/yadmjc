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
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum PowerRune implements Rune {
	LO, UM, ON, EE, PAL, MON;

	@Override
	public int getCost() {
		return getPowerLevel();
	}

	@Override
	public int getCost(PowerRune powerRune) {
		// N'a pas de sens pour un PowerRune
		throw new UnsupportedOperationException();
	}

	@Override
	public Type getType() {
		return Type.POWER;
	}

	public int getPowerLevel() {
		return ordinal() + 1;
	}

	@Override
	public int getId() {
		// id dans l'intervalle [1-6]
		return ordinal() + 1;
	}

	/**
	 * Retourne le multiplicateur de difficulté appliqué quand un sort est
	 * invoqué avec ce rune de puissance.
	 * 
	 * @return un entier positif.
	 */
	public int getDifficultyMultiplier() {
		switch (this) {
		case LO:
			return 8;
		case UM:
			return 12;
		case ON:
			return 16;
		case EE:
			return 20;
		case PAL:
			return 24;
		case MON:
			return 28;

		default:
			throw new UnsupportedOperationException();
		}
	}
}
