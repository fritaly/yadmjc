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

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum FormRune implements Rune {
	VEN(4, 6, 8, 10, 12, 14),
	EW(5, 7, 10, 12, 15, 17),
	KATH(6, 9, 12, 15, 18, 21),
	IR(7, 10, 14, 17, 21, 24),
	BRO(9, 13, 18, 22, 27, 31),
	GOR(9, 13, 18, 22, 27, 31);

	private final int[] costs;

	private FormRune(int... costs) {
		if (costs.length != 6) {
			throw new IllegalArgumentException(
					"Invalid array length (6 expected)");
		}

		this.costs = costs;
	}

	@Override
	public int getCost() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCost(PowerRune powerRune) {
		Validate.isTrue(powerRune != null, "The given power rune is null");

		return costs[powerRune.ordinal()];
	}

	@Override
	public Type getType() {
		return Type.FORM;
	}

	@Override
	public int getId() {
		// id dans l'intervalle [1-6]
		return ordinal() + 1;
	}
}
