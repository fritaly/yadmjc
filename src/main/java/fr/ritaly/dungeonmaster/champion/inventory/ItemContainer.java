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
package fr.ritaly.dungeonmaster.champion.inventory;

import java.util.List;

import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public interface ItemContainer {

	/**
	 * Retourne le nombre d'objets que cet {@link ItemContainer} peut contenir.
	 * 
	 * @return un entier positif.
	 */
	public int getCapacity();

	public void addChangeListener(ChangeListener listener);

	public void removeChangeListener(ChangeListener listener);

	/**
	 * Retourne le nombre actuel d'objets dans le conteneur.
	 * 
	 * @return un entier positif ou nul dans l'intervalle [0-capacity].
	 */
	public int getItemCount();

	/**
	 * Retourne les {@link Item} contenus par cet {@link ItemContainer} sous
	 * forme de {@link List}.
	 * 
	 * @return une {@link List} de {@link Item}. Ne retourne jamais null.
	 */
	public List<Item> getItems();

	/**
	 * Indique si cet {@link ItemContainer} est plein.
	 * 
	 * @return si cet {@link ItemContainer} est plein.
	 * @see #getCapacity()
	 * @see #getItemCount()
	 */
	public boolean isFull();

	/**
	 * Indique si cet {@link ItemContainer} est vide.
	 * 
	 * @return si cet {@link ItemContainer} est vide.
	 * @see #getItemCount()
	 */
	public boolean isEmpty();

	/**
	 * Retourne un {@link Item} tiré au hasard parmi ceux de cet
	 * {@link ItemContainer}. Cette méthode ne supprime pas l'objet du
	 * conteneur.
	 * 
	 * @return un {@link Item} ou null si le conteneur est vide.
	 */
	public Item getRandom();

	/**
	 * Tente d'ajouter l'objet donné à cet {@link ItemContainer} et retourne
	 * l'index auquel a été ajouté l'objet si l'opération a réussi autrement -1.
	 * 
	 * @param item
	 *            un {@link Item} à ajouter.
	 * @return un entier représentant l'index auquel a été ajouté l'objet si
	 *         l'opération a réussi autrement -1.
	 */
	public int add(Item item);

	/**
	 * Supprime tous les objets de cet {@link ItemContainer} et les retourne
	 * sous forme de {@link List}.
	 * 
	 * @return une {@link List} de {@link Item}. Ne retourne jamais null.
	 */
	public List<Item> removeAll();

	/**
	 * Supprime l'objet d'index donné et le retourne.
	 * 
	 * @param index
	 *            un entier positif ou nul représentant l'index de l'objet à
	 *            retirer.
	 * @return une instance de {@link Item} ou null s'il n'y a aucun objet à
	 *         l'index donné.
	 */
	public Item remove(int index);

	/**
	 * Supprime un {@link Item} tiré au hasard parmi ceux de cet
	 * {@link ItemContainer} et le retourne.
	 * 
	 * @return un {@link Item} ou null si le conteneur est vide.
	 */
	public Item removeRandom();

	/**
	 * Tente de supprimer l'objet donné et retourne si l'opération a réussi.
	 * 
	 * @param item
	 *            une instance de {@link Item} à supprimer du conteneur.
	 * @return si l'opération a réussi.
	 */
	public boolean remove(Item item);

	public Item set(int index, Item item);

	/**
	 * Retourne le poids total des objets contenus par cet {@link Item}.
	 * 
	 * @return un float représentant le poids des objets en kilogrammes.
	 */
	public float getTotalWeight();
}