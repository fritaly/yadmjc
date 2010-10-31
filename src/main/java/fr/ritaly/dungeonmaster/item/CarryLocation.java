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
 * Enumération des "endroits" où un {@link Item} peut être porté (cf
 * {@link #HEAD}, {@link #NECK}, {@link #TORSO}, {@link #LEGS}, {@link #FEET},
 * {@link #HANDS}), utilisé (cf {@link #CONSUMABLE}) ou rangé (cf
 * {@link #QUIVER1}, {@link #QUIVER2}, {@link #POUCH}, {@link #CHEST},
 * {@link #BACKPACK}). Les différentes combinaisons possible de
 * {@link CarryLocation} sont énumérées sous forme de {@link CarryLocations}.
 *
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum CarryLocation {
	// --- Consommable --- //
	CONSUMABLE,
	// --- Parties du corps --- //
	HEAD,
	NECK,
	TORSO,
	LEGS,
	FEET,
	// --- Inventaire --- //
	QUIVER1,
	QUIVER2,
	POUCH,
	HANDS,
	CHEST,
	BACKPACK;
}