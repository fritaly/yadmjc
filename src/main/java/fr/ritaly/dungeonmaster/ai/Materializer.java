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

import fr.ritaly.dungeonmaster.ClockListener;

/**
 * Some (actually most) creatures are material whereas others are immaterial.
 * However there's a special creature (the {@link Creature.Type#ZYTAZ}) that can
 * be both depending on time (that is, material at instant T and immaterial at
 * instant T+dt). A {@link Materializer} is responsible for telling what the
 * {@link Materiality} of a creature is at a given time. That's the reason why
 * this interface extends {@link ClockListener}.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public interface Materializer extends ClockListener {

	/**
	 * Tells whether the object is currently material.
	 *
	 * @return whether the object is currently material.
	 */
	public boolean isMaterial();

	/**
	 * Tells whether the object is currently immaterial.
	 *
	 * @return whether the object is currently immaterial.
	 */
	public boolean isImmaterial();
}