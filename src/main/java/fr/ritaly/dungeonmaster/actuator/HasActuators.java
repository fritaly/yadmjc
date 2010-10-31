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

import fr.ritaly.dungeonmaster.map.EventType;

/**
 * An object which handles an {@link Actuator} per event type.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public interface HasActuators {

	/**
	 * Returnt the {@link Actuator} mapped to the given event type.
	 * 
	 * @param eventType
	 *            an {@link EventType}. Can't be null.
	 * @return an {@link Actuator} or null if none is mapped to the given event
	 *         type.
	 */
	public Actuator getActuator(EventType eventType);

	/**
	 * Sets the {@link Actuator} mapped to the given event type.
	 * 
	 * @param eventType
	 *            an {@link EventType}.
	 * @param actuator
	 *            an {@link Actuator}.
	 */
	public void setActuator(EventType eventType, Actuator actuator);

	public void addActuator(EventType eventType, Actuator actuator);

	public void clearActuator(EventType eventType);
}