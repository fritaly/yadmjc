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
 * An object which handles an {@link Actuator}.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public interface HasActuator {

	/**
	 * Returns the handled {@link Actuator}.
	 * 
	 * @return an {@link Actuator} or null.
	 */
	public Actuator getActuator();

	/**
	 * Sets the {@link Actuator} to be handled.
	 * 
	 * @param actuator
	 *            an {@link Actuator} to handle.
	 */
	public void setActuator(Actuator actuator);

	/**
	 * Adds the given {@link Actuator} to the currently handled {@link Actuator}
	 * (if any).
	 * 
	 * @param actuator
	 *            an {@link Actuator}.
	 */
	public void addActuator(Actuator actuator);

	/**
	 * Resets the handled {@link Actuator}.
	 */
	public void clearActuator();
}