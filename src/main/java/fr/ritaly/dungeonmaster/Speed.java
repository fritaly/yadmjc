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

/**
 * Enumerates the possible move speeds. Each speed is associated to a value
 * representing the relative move speed.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Speed {
	SLOW(8),
	NORMAL(4),
	FAST(2),

	/**
	 * Special speed used for dead champions. A dead champion doesn't affect the
	 * party's speed and is considered to be moving at (Faster Than) Light speed
	 * :)
	 */
	UNDEFINED(0);

	private final int value;

	private Speed(int value) {
		this.value = value;
	}

	/**
	 * Returns the integer value associated to this move speed. The higher the
	 * value, the slower the speed.
	 *
	 * @return an integer value.
	 */
	public int getValue() {
		return value;
	}
}
