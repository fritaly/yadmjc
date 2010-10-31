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
package fr.ritaly.dungeonmaster.stat;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class IntStat extends Stat<Integer> {

	public IntStat(String owner, String name) {
		super(owner, name);
	}

	public IntStat(String owner, String name, Integer initialValue) {
		super(owner, name, initialValue);
	}

	@Override
	protected Integer create(float value) {
		// Ne pas générer de dépassement de capacité
		if (value >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (value <= Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}

		return Integer.valueOf((int) value);
	}

	public IntStat(String owner, String name, Integer initialValue,
			Integer minValue, Integer maxValue) {

		super(owner, name, initialValue, minValue, maxValue);
	}

	public IntStat(String owner, String name, Integer initialValue,
			Integer maxValue) {
		
		super(owner, name, initialValue, maxValue);
	}
}
