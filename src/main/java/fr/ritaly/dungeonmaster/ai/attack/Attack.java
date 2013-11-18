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
package fr.ritaly.dungeonmaster.ai.attack;

/**
 * An attack.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public interface Attack {

	// TODO Define one implementation class per attack type

	/**
	 * Enumerates the different types of attacks. Types identified in
	 * Attack::ATTACKDATATYPE.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Type {
		WAR_CRY,
		PHYSICAL,
		SPELL,
		HIT_DOOR,
		SHOOT,
		FLIP,
		SHIELD,
		FLUX_CAGE,
		FUSION,
		HEAL,
		WINDOW,
		CLIMB_DOWN,
		FREEZE_LIFE,
		LIGHT,
		THROW,
		DEFAULT;
	}

	/**
	 * Returns the attack's type.
	 *
	 * @return the attack's type.
	 */
	public Type getType();

}