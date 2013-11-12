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
package fr.ritaly.dungeonmaster.map;

import java.util.List;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Teleport;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Stairs extends DirectedElement {

	/**
	 * Indique si l'escalier est montant ou descendant.
	 */
	private final boolean ascending;

	/**
	 * La position cible de cet escalier, c'est-�-dire l'endroit o� l'on aboutit
	 * en l'empruntant.
	 */
	private final Position destination;

	public Stairs(Direction direction, boolean ascending, Position destination) {
		super(Type.STAIRS, direction);

		Validate.isTrue(destination != null, "The given destination is null");

		this.ascending = ascending;
		this.destination = destination;
	}

	@Override
	public boolean isTraversable(Party party) {
		return true;
	}

	@Override
	public boolean isTraversableByProjectile() {
		return false;
	}

	@Override
	public boolean isTraversable(Creature creature) {
		Validate.notNull(creature);

		// Conditionn� par le fait que la cr�ature peut prendre des escaliers !
		return creature.canTakeStairs();
	}

	@Override
	protected final void afterPartySteppedOn() {
		super.afterPartySteppedOn();

		takeStairs();
	}

	private void takeStairs() {
		// Le groupe est-il dans le sens "descente" ou "mont�e" des escaliers ?
		if (getParty().getDirection().equals(getDirection())) {
			if (log.isDebugEnabled()) {
				log.debug("Party is taking stairs at " + getPosition() + " ...");
			}

			// Oui, le groupe emprunte les escaliers
			final Stairs stairs = (Stairs) getParty().getDungeon().getElement(destination);

			// T�l�porter le groupe � l'endroit destination. La direction finale
			// d�pend de l'escalier cible !!!
			getParty().getDungeon().teleportParty(destination, stairs.getDirection().getOpposite(), true);

			if (log.isDebugEnabled()) {
				log.debug("Party has taken stairs at " + getPosition());
			}
		}
	}

	public final boolean isAscending() {
		return ascending;
	}

	public Position getDestination() {
		return destination;
	}

	@Override
	public void partyTurned() {
		super.partyTurned();

		// Le groupe emprunte-til l'escalier ?
		takeStairs();
	}

	@Override
	public String getSymbol() {
		return "S";
	}

	@Override
	public Teleport getTeleport(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		if (!hasParty()) {
			throw new IllegalStateException("The party isn't on this element");
		}

		// Si le groupe occupe l'escalier et qu'il se d�place dans le sens de
		// l'escalier alors il monte ou descend d'un �tage
		if (direction.equals(getDirection())) {
			// Le groupe prend l'escalier
			final Stairs stairs = (Stairs) getLevel().getDungeon().getElement(destination);

			return new Teleport(destination, stairs.getDirection().getOpposite());
		}

		return new Teleport(getPosition().towards(direction), getParty().getLookDirection());
	}

	@Override
	public void validate() throws ValidationException {
		// Un escalier doit �tre entour�e de deux murs de chaque c�t�
		final List<Element> surroundingElements = getSurroundingElements();

		final Element element1 = surroundingElements.get(0);
		final Element element2 = surroundingElements.get(1);

		if (!element1.isConcrete()) {
			throw new ValidationException("The element at " + element1.getPosition() + " must be concrete [actual=" + element1
					+ "]");
		}
		if (!element2.isConcrete()) {
			throw new ValidationException("The element at " + element2.getPosition() + " must be concrete [actual=" + element2
					+ "]");
		}

		// "Derri�re" l'escalier, il faut �galement un mur vu que
		// l'on ne peut emprunter l'escalier que dans un seul sens
		final Position rearPosition = getPosition().towards(getDirection());

		final Element rearElement = getLevel().getDungeon().getElement(rearPosition);

		if (!rearElement.isConcrete()) {
			throw new ValidationException("The element behind the stairs (at " + rearPosition + ") must be concrete [actual="
					+ rearElement.getType().name() + "]");
		}

		// La destination d'un escalier doit �tre un autre escalier
		final Element target = getLevel().getDungeon().getElement(getDestination());

		if (!(target instanceof Stairs)) {
			throw new ValidationException("The element at " + getDestination() + " must be a " + Type.STAIRS);
		}

		// L'escalier cible doit �tre l'inverse de cet escalier
		// (montant / descendant)
		final Stairs otherStairs = (Stairs) target;

		if (otherStairs.isAscending() == isAscending()) {
			throw new ValidationException("The two stairs at " + getPosition() + " and " + getDestination()
					+ " can't have the same type (both up or both down)");
		}

		// La cible de l'escalier doit se trouver sur un autre niveau
		final int targetLevel = getDestination().z;

		if (targetLevel == getPosition().z) {
			throw new ValidationException("The two stairs at " + getPosition() + " and " + getDestination()
					+ " are on the same level [" + targetLevel + "]");
		}

		// La diff�rence de valeur entre les deux niveaux doit valoir +1 ou -1
		final int difference = targetLevel - getPosition().z;

		if ((difference != -1) && (difference != +1)) {
			throw new ValidationException("The difference of levels for stairs at " + getPosition() + " and " + getDestination()
					+ " must be -1 or +1 [actual=" + difference + "]");
		}

		// La diff�rence de niveau doit �tre compatible avec le sens de
		// l'escalier. Attention au sens de num�rotation des niveaux: les plus
		// profonds ont un num�ro de niveau plus �lev�
		if (isAscending() && (difference == +1)) {
			// L'escalier monte mais le niveau cible est plus bas
			throw new ValidationException("The stairs at " + getPosition() + " are up but the stairs' destination ("
					+ getDestination() + ") is below the stairs' level");
		} else if (!isAscending() && (difference == -1)) {
			// L'escalier descend mais le niveau cible est plus haut
			throw new ValidationException("The stairs at " + getPosition() + " are down but the stairs' destination ("
					+ getDestination() + ") is above the stairs' level");
		}
	}

	// FIXME Un projectile peut-il "prendre" les escaliers ?

	@Override
	public boolean isFluxCageAllowed() {
		return true;
	}

}