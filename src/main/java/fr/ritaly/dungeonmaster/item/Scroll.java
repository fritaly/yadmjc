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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.body.BodyPart;

/**
 * A scroll. Scrolls provide hints during the game to solve riddles.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Scroll extends Item {

	/**
	 * The scroll's lines of text.
	 */
	private final List<String> text;

	public Scroll(final List<String> text) {
		super(Type.SCROLL);

		Validate.notNull(text, "The given list of text lines is null");
		Validate.isTrue(!text.isEmpty(), "The given list of text lines is empty");

		this.text = new ArrayList<String>(text);
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

	public List<String> getText() {
		return Collections.unmodifiableList(text);
	}
}