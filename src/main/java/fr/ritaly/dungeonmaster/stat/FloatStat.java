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
public class FloatStat extends Stat<Float> {

	public FloatStat(String owner, String name) {
		super(owner, name);
	}

	public FloatStat(String owner, String name, Float initialValue) {
		super(owner, name, initialValue);
	}

	@Override
	protected Float create(float value) {
		return Float.valueOf(value);
	}

	public FloatStat(String owner, String name, Float initialValue,
			Float minValue, Float maxValue) {

		super(owner, name, initialValue, minValue, maxValue);
	}

	public FloatStat(String owner, String name, Float initialValue,
			Float maxValue) {

		super(owner, name, initialValue, maxValue);
	}
}
