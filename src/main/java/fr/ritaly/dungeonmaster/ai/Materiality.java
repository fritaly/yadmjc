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
 * Enumerates the possible "materialities".<br>
 * <br>
 * Some creatures are said "material" and can be wounded by using a regular
 * weapon. Some others are said "immaterial" (for instance ghosts) and can only
 * be killed by using special weapons. There's at least one creature (the zytaz)
 * that can be both but not at the same time.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public enum Materiality {

	/**
	 * The materiality of a material creature (most objects and creatures in the
	 * game are material).
	 */
	MATERIAL,

	/**
	 * The materiality of an immaterial creature (for creatures like ghost,
	 * zytaz, etc) which can traverse walls.
	 */
	IMMATERIAL;
}
