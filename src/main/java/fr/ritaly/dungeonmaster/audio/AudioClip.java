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
package fr.ritaly.dungeonmaster.audio;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum AudioClip {
	/**
	 * Son des pas quand le groupe se déplace
	 */
	STEP("step.wav"),

	/**
	 * Son quand une porte est détruite
	 */
	DOOR_BROKEN("doorbreak.wav"),

	/**
	 * Son quand on boit une boisson
	 */
	GLOUPS("gulp.wav"),

	/**
	 * Cri quand un champion mort
	 */
	CHAMPION_DIED("scream.wav"),

	/**
	 * Son quand on frappe sur une porte mais qu'elle ne cède pas
	 */
	CLONK("thunk.wav"),

	/**
	 * Son d'un coup porté dans le vide
	 */
	SWING("swing.wav"),

	/**
	 * Son quand le groupe se cogne dans un mur
	 */
	BONG("bump.wav"),

	/**
	 * Son quand on remplit une outre, une fiole
	 */
	REFILL(null),

	/**
	 * Son de la téléportation
	 */
	TELEPORT("teleport.wav"),

	/**
	 * Son d'une serrure que l'on ouvre
	 */
	LOCK(null),

	/**
	 * Son d'une boule de feu en vol
	 */
	FIRE_BALL("fball.wav"),

	/**
	 * Son d'une boule de feu qui explose;
	 */
	FIRE_BURST("burn.wav"),
	
	SWITCH("switch.wav"),
	
	ALTAR("altar.wav"),
	
	SHOUT(null);
	
	private final String sound;
	
	private AudioClip(String sound) {
		this.sound = sound;
	}

	public String getSound() {
		return sound;
	}
}