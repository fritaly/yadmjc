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
package fr.ritaly.dungeonmaster.ai;

/**
 * Enumeration des types d'attaque possibles.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum AttackType {
	/**
	 * No damage: the creature does not attack champions. This value is set for
	 * the Giggler. This value is used elsewhere for damages when stamina is 0.
	 */
	NONE,

	/**
	 * Fire damage: the attacked champion's 'Anti-Fire' characteristic is used
	 * to compute damage.
	 */
	FIRE,

	/**
	 * Critical damage: The 'Armor Strength' value of the attacked champion's
	 * armor is used to compute damage, but its value is halved. This is never
	 * used in the original game but can be used in custom games. This value is
	 * used elsewhere for example when bumping in a wall.
	 */
	CRITICAL,

	/**
	 * Normal damage: The 'Armor Strength' value of the attacked champion's
	 * armor is used to compute damage.
	 */
	NORMAL,

	/**
	 * Sharp damage: The 'Sharp resistance' value of the attacked champion's
	 * armor is used to compute damage.
	 */
	SHARP,

	/**
	 * Magic damage: the attacked champion's 'Anti-Magic' characteristic is used
	 * to compute damage.
	 */
	MAGIC,

	/**
	 * Psychic damage: the attacked champion's 'Wisdom' characteristic is used
	 * to compute damage.
	 */
	PSYCHIC;
}
