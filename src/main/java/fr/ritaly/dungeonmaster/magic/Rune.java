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
package fr.ritaly.dungeonmaster.magic;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public interface Rune {

	/**
	 * Enumération des différents types (familles) de runes existants.
	 */
	public static enum Type {
		POWER, ELEMENT, FORM, ALIGNMENT;
	}

	/**
	 * Retourne le coût (en nombre de points de mana) nécessaire à l'invocation
	 * du rune. Cette méthode ne fonctionne que pour un {@link PowerRune}.
	 * 
	 * @return un entier positif représentant un nombre de points de mana.
	 */
	public int getCost();

	/**
	 * Retourne le coût (en nombre de points de mana) nécessaire à l'invocation
	 * du rune pour le power rune donné. Cette méthode ne fonctionne que pour un
	 * {@link ElementRune}, {@link FormRune} ou {@link AlignmentRune}.
	 * 
	 * @param powerRune
	 *            un {@link PowerRune} représentant la puissance d'invocation du
	 *            rune.
	 * @return un entier positif représentant un nombre de points de mana.
	 */
	public int getCost(PowerRune powerRune);

	/**
	 * Retourne le type du rune, c'est-à-dire la famille de runes à laquelle il
	 * appartient.
	 * 
	 * @return une instance de {@link Type}.
	 */
	public Type getType();

	/**
	 * Retourne l'identifiant du rune. La valeur retournée est située dans
	 * l'intervalle [1-6].
	 * 
	 * @return un entier identifiant le rune dans sa famille de runes.
	 */
	public int getId();
}
