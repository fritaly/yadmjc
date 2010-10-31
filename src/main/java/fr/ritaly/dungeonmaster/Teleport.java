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

import org.apache.commons.lang.Validate;

/**
 * Définit une téléportation vers une {@link Position} cible. Lors de la
 * téléportation, la {@link Direction} peut également être modifiée. Une
 * {@link Teleport} représente une généralisation de la notion de déplacement /
 * mouvement.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Teleport {

	private final Position position;

	private final Direction direction;

	public Teleport(Position position, Direction direction) {
		Validate.isTrue(position != null);
		Validate.isTrue(direction != null);

		this.position = position;
		this.direction = direction;
	}

	public Position getPosition() {
		return position;
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return "Teleport[position=" + position + ",direction=" + direction
				+ "]";
	}
}