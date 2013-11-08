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
package fr.ritaly.dungeonmaster.actuator;

/**
 * Enumerates the different types of triggers.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum TriggerType {

	/**
	 * When a party steps on a pressure pad.
	 */
	PARTY_STEPPED_ON,

	/**
	 * When a party steps off a pressure pad.
	 */
	PARTY_STEPPED_OFF,

	/**
	 * When an item is dropped on a pressure pad.
	 */
	ITEM_DROPPED,

	/**
	 * When an item is picked off a pressure pad.
	 */
	ITEM_PICKED_UP,

	/**
	 * When a creature steps on a pressure pad.
	 */
	CREATURE_STEPPED_ON,

	/**
	 * When a creature steps off a pressure pad.
	 */
	CREATURE_STEPPED_OFF;
}
