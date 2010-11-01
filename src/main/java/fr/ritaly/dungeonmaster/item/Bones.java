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

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Bones extends MiscItem {

	private final Champion champion;

	public Bones(Champion champion) {
		super(Type.BONES);

		Validate.isTrue(champion != null, "The given champion is null");

		this.champion = champion;
	}

	public Bones() {
		super(Type.BONES);

		this.champion = null;
	}

	@Override
	public int getShield() {
		return 0;
	}

	@Override
	public int getAntiMagic() {
		return 0;
	}

	@Override
	public BodyPart.Type getActivationBodyPart() {
		return null;
	}

	public Champion getChampion() {
		return champion;
	}
	
	public boolean hasChampion() {
		return (champion != null);
	}

	@Override
	public String toString() {
		return (champion != null) ? champion.getName() + "'s "
				+ Type.BONES.name() : Type.BONES.name();
	}
}