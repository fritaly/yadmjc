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
package fr.ritaly.dungeonmaster.item;

/**
 * Enumerates the locations where items can be carried (or used) by a champion.
 * Those locations are grouped into combos known as {@link CarryLocations}.<br>
 * <br>
 * Source: <a href="http://dmweb.free.fr/?q=node/886">Technical Documentation -
 * Dungeon Master and Chaos Strikes Back Items properties</a>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum CarryLocation {

	/**
	 * Special carry location corresponding to the champion's mouth. An item
	 * stored here is eaten / swallowed by the champion.
	 */
	CONSUMABLE,

	// --- Locations corresponding to the champion's body parts --- //

	HEAD,
	NECK,
	TORSO,
	LEGS,
	FEET,

	// --- Locations corresponding to the champion's inventory --- //

	/**
	 * There are 2 distinct locations for the quiver because those 2 locations cannot accept the same items.
	 */
	QUIVER1,

	/**
	 * There are 2 distinct locations for the quiver because those 2 locations cannot accept the same items.
	 */
	QUIVER2,

	POUCH,
	HANDS,
	CHEST,
	BACKPACK;
}