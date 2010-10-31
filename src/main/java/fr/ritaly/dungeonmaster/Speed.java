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

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum Speed {
	SLOW,
	NORMAL,
	FAST,
	/**
	 * Valeur spéciale signifiant vitesse "non définie" (dans le cas d'un
	 * champion mort par exemple).
	 */
	UNDEFINED;

	public int getValue() {
		switch (this) {
		case FAST:
			return 2;
		case NORMAL:
			return 4;
		case SLOW:
			return 8;
		case UNDEFINED:
			return 0;
		default:
			throw new UnsupportedOperationException();
		}
	}
}
