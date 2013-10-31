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
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public interface Rune {

	/**
	 * Enum�ration des diff�rents types (familles) de runes existants.
	 */
	public static enum Type {
		POWER, ELEMENT, FORM, ALIGNMENT;
	}

	/**
	 * Retourne le co�t (en nombre de points de mana) n�cessaire � l'invocation
	 * du rune. Cette m�thode ne fonctionne que pour un {@link PowerRune}.
	 * 
	 * @return un entier positif repr�sentant un nombre de points de mana.
	 */
	public int getCost();

	/**
	 * Retourne le co�t (en nombre de points de mana) n�cessaire � l'invocation
	 * du rune pour le power rune donn�. Cette m�thode ne fonctionne que pour un
	 * {@link ElementRune}, {@link FormRune} ou {@link AlignmentRune}.
	 * 
	 * @param powerRune
	 *            un {@link PowerRune} repr�sentant la puissance d'invocation du
	 *            rune.
	 * @return un entier positif repr�sentant un nombre de points de mana.
	 */
	public int getCost(PowerRune powerRune);

	/**
	 * Retourne le type du rune, c'est-�-dire la famille de runes � laquelle il
	 * appartient.
	 * 
	 * @return une instance de {@link Type}.
	 */
	public Type getType();

	/**
	 * Retourne l'identifiant du rune. La valeur retourn�e est situ�e dans
	 * l'intervalle [1-6].
	 * 
	 * @return un entier identifiant le rune dans sa famille de runes.
	 */
	public int getId();
}
