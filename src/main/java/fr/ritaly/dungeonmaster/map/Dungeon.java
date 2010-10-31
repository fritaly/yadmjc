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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.DeferredCommand;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Side;
import fr.ritaly.dungeonmaster.Teleport;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Dungeon implements ClockListener {

	public Dungeon() {
		Clock.getInstance().register(this);
	}

	private final Log log = LogFactory.getLog(Dungeon.class);

	private final Map<Integer, Level> levels = new HashMap<Integer, Level>();

	private Party party;

	public int getLevelCount() {
		return levels.size();
	}

	public Level getCurrentLevel() {
		if (hasParty()) {
			return getLevel(getParty().getPosition().z);
		}

		return null;
	}

	public List<Level> getLevels() {
		final ArrayList<Level> list = new ArrayList<Level>(levels.values());

		Collections.sort(list, new Comparator<Level>() {
			@Override
			public int compare(Level level1, Level level2) {
				return level1.getLevel() - level2.getLevel();
			}
		});

		return list;
	}

	public Element getCurrentElement() {
		if (hasParty()) {
			return getElement(getParty().getPosition());
		}

		return null;
	}

	public Level getLevel(int level) {
		return levels.get(level);
	}

	public Party getParty() {
		return party;
	}

	public boolean hasParty() {
		return (party != null);
	}

	public void setParty(Position position, Party party) {
		if (position == null) {
			throw new IllegalStateException("The given position is null");
		}
		if (party == null) {
			throw new IllegalStateException("The given party is null");
		}

		setParty(position.x, position.y, position.z, party);
	}

	public void setParty(int x, int y, int z, Party party) {
		if (this.party != null) {
			throw new IllegalStateException("There is already a party set");
		}
		if (party == null) {
			throw new IllegalStateException("The given party is null");
		}

		final Position position = new Position(x, y, z);

		if (log.isDebugEnabled()) {
			log.debug("Installing party at " + position + " ...");
		}

		// Récupérer l'élément en premier (permet de valider le triplet (x,y,z))
		final Element element = getElement(x, y, z);

		// Vérifier que le groupe peut s'y placer !
		if (!element.isTraversable(party)) {
			throw new IllegalArgumentException("The " + element.getType()
					+ " element at " + position
					+ " can't be occupied by the party");
		}

		this.party = party;
		this.party.setPosition(position);
		this.party.setDungeon(this);

		// Initialiser le listener audio
		SoundSystem.getInstance().setListener(party);

		// "Placer" le groupe sur l'endroit cible (le faire marcher dessus)
		element.partySteppedOn(party);

		if (log.isInfoEnabled()) {
			log.info("Party installed at " + position);
		}
	}

	public Element getElement(Position position) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		return getElement(position.x, position.y, position.z);
	}

	public void setElement(int x, int y, int z, Element element) {
		if (element == null) {
			throw new IllegalArgumentException("The given element is null");
		}

		final Level level = getLevel(z);

		if (level == null) {
			throw new IllegalArgumentException("There is no level <" + z + ">");
		}

		level.setElement(x, y, element);
	}

	public void setElement(Position position, Element element) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}
		if (element == null) {
			throw new IllegalArgumentException("The given element is null");
		}

		setElement(position.x, position.y, position.z, element);
	}

	public Element getElement(int x, int y, int z) {
		final Level level = getLevel(z);

		if (level == null) {
			throw new IllegalArgumentException("There is no level <" + z + ">");
		}

		// Rechercher l'élément cible
		final Element element = level.getElement(x, y);

		if (element == null) {
			throw new IllegalArgumentException("There is no element at [" + z
					+ ":" + x + "," + y + "]");
		}

		return element;
	}

	public Level createLevel(int number, int height, int width) {
		if (number <= 0) {
			throw new IllegalArgumentException("The level number <" + number
					+ "> must be positive");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("The given height <" + height
					+ "> must be positive");
		}
		if (width <= 0) {
			throw new IllegalArgumentException("The given width <" + width
					+ "> must be positive");
		}

		if (levels.containsKey(number)) {
			throw new IllegalArgumentException(
					"There is already a level with number <" + number + ">");
		}

		final Level level = new Level(this, number, height, width);

		levels.put(number, level);

		return level;
	}

	public void setLevel(int number, Level level) {
		if (level == null) {
			throw new IllegalArgumentException("The given level is null");
		}

		levels.put(number, level);
	}

	@Override
	public boolean clockTicked() {
		if (!partyMoves.isEmpty()) {
			final DeferredCommand command = partyMoves.getFirst();

			if (!command.clockTicked()) {
				// L'action s'est déclenchée. La supprimer l'entrée de la liste
				partyMoves.removeFirst();
			}
		}

		return true;
	}

	private final LinkedList<DeferredCommand> partyMoves = new LinkedList<DeferredCommand>();

	public boolean moveParty(final Move move, boolean now) {
		return moveParty(move, now, AudioClip.STEP);
	}

	public boolean moveParty(final Move move, boolean now, final AudioClip clip) {
		if (move == null) {
			throw new IllegalArgumentException("The given move is null");
		}
		if (clip == null) {
			throw new IllegalArgumentException("The given clip is null");
		}
		if (party == null) {
			throw new IllegalStateException("The party isn't defined");
		}

		if (now) {
			return movePartyNow(move, clip);
		} else {
			// Mettre en file d'attente la commande
			final int delay = party.getMoveSpeed().getValue();

			partyMoves.add(new DeferredCommand("Dungeon.PartyMover", delay) {

				@Override
				protected void run() {
					if (log.isDebugEnabled()) {
						log.debug("Running delayed command. Moving party ...");
					}

					movePartyNow(move, clip);

					if (log.isDebugEnabled()) {
						log.debug("Ran delayed command");
					}
				}
			});

			if (log.isDebugEnabled()) {
				log.debug("Queued party move <" + move + "> [delay=" + delay
						+ " ticks]");
			}

			return true;
		}
	}

	private boolean movePartyNow(Move move, AudioClip clip) {
		if (move == null) {
			throw new IllegalArgumentException("The given move is null");
		}
		if (clip == null) {
			throw new IllegalArgumentException("The given clip is null");
		}
		if (this.party == null) {
			throw new IllegalStateException("There is no party set");
		}

		// Note: Un déplacement ne peut modifier la direction de regard et la
		// position en même temps (seule une téléportation le peut)

		if (log.isDebugEnabled()) {
			log.debug("Moving party [" + move + "] ...");
		}

		if (!move.changesPosition()) {
			// Pas de changement de position, c'est sa direction de regard qui
			// change
			party.turn(move);

			if (log.isInfoEnabled()) {
				log.info("Party remains still");
			}

			final Element element = getElement(getParty().getPosition());

			// Notification (pour les escaliers entre autres)
			element.partyTurned();

			return true;
		}

		// Le groupe va changer de position

		// Emplacement initial ?
		final Element sourceElement = getElement(party.getPosition());

		if (log.isDebugEnabled()) {
			log.debug("Source element = " + sourceElement);
		}

		// Direction de déplacement ?
		final Direction moveDirection = move.getMoveDirection(party
				.getLookDirection());

		if (log.isDebugEnabled()) {
			log.debug("Move direction = " + moveDirection);
		}

		// Position finale ? Le demander à l'élément source (pour les escaliers
		// entre autres)
		final Teleport teleport = sourceElement.getTeleport(moveDirection);

		if (log.isDebugEnabled()) {
			log.debug("Teleport = " + teleport);
		}

		// Emplacement final ?
		final Element destinationElement = getElement(teleport.getPosition());

		if (log.isDebugEnabled()) {
			log.debug("Destination element = " + destinationElement);
		}

		// Position traversable ?
		if (!destinationElement.isTraversable(party)) {
			// Le groupe se cogne dans le mur

			SoundSystem.getInstance().play(AudioClip.BONG);

			// Appliquer des dégâts aux champions. Champions concernés ?
			final Set<Champion> champions;

			switch (move) {
			case FORWARD:
				champions = getParty().getChampions(Side.FRONT, false);
				break;
			case BACKWARD:
				champions = getParty().getChampions(Side.REAR, false);
				break;
			case LEFT:
				champions = getParty().getChampions(Side.LEFT, false);
				break;
			case RIGHT:
				champions = getParty().getChampions(Side.RIGHT, false);
				break;
			default:
				// Dans les autres cas, les champions ne peuvent se blesser !!
				champions = null;
				break;
			}
			
			if (champions != null) {
				for (Champion champion : champions) {
					// Blesser chaque champion
					champion.hit(Utils.random(10, 30));
				}
			}

			if (log.isInfoEnabled()) {
				log.info("Party bumped into " + destinationElement.getType());
			}

			return false;
		}

		// Position occupée ?
		if (!destinationElement.isEmpty()) {
			// Oui, le groupe ne peut bouger
			if (log.isInfoEnabled()) {
				log.info("Party can't move since destination isn't free");
			}

			return false;
		}

		// Quitter la position actuelle
		sourceElement.partySteppedOff();

		// Changer la direction du groupe (si nécessaire)
		getParty().setLookDirection(teleport.getDirection());

		// Déplacement du groupe
		party.setPosition(teleport.getPosition());

		// Jouer le son demandé
		SoundSystem.getInstance().play(clip);

		// Occuper la position cible
		destinationElement.partySteppedOn(party);

		if (log.isInfoEnabled()) {
			log.info("Party moved");
		}

		return true;
	}

	public boolean teleportParty(Teleport teleport, boolean silent) {
		if (teleport == null) {
			throw new IllegalArgumentException("The given teleport is null");
		}

		return teleportParty(teleport.getPosition(), teleport.getDirection(),
				silent);
	}

	public boolean teleportParty(Position destination, Direction direction,
			boolean silent) {

		if (destination == null) {
			throw new IllegalArgumentException("The given position is null");
		}
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}
		if (this.party == null) {
			throw new IllegalStateException("There is no party set");
		}
		if (destination.equals(this.party.getPosition())) {
			throw new IllegalArgumentException(
					"The party is already at the given destication "
							+ destination);
		}

		if (log.isDebugEnabled()) {
			log.debug("Teleporting party to " + destination + " ...");
		}

		// Le groupe va changer de position

		// Emplacement initial ?
		final Element sourceElement = getElement(party.getPosition());

		if (log.isDebugEnabled()) {
			// log.debug("Source position = " + party.getPosition());
			log.debug("Source element = " + sourceElement.getId());
		}

		// Emplacement final ?
		final Element destinationElement = getElement(destination);

		if (log.isDebugEnabled()) {
			log.debug("Destination element = " + destinationElement.getId());
		}

		// Position traversable ?
		if (!destinationElement.isTraversable(party)) {
			// Ne doit pas arriver, c'est qu'il y a un défaut de conception du
			// niveau
			throw new IllegalArgumentException("The given destination "
					+ destination + " can't be traversed by the party");
		}

		// Position occupée ?
		if (!destinationElement.isEmpty()) {
			// TODO Tuer les monstre sur la destination ?

			// Oui, le groupe ne peut bouger
			if (log.isInfoEnabled()) {
				log.info("Party can't move since destination " + destination
						+ " isn't free");
			}

			return false;
		}

		// Quitter la position actuelle
		sourceElement.partySteppedOff();

		// Faire tourner le groupe
		party.setLookDirection(direction);

		// Déplacement du groupe
		party.setPosition(destination);

		if (!silent) {
			// Jouer le son demandé
			SoundSystem.getInstance().play(AudioClip.TELEPORT);
		}

		// Occuper la position cible
		destinationElement.partySteppedOn(party);

		if (log.isInfoEnabled()) {
			log.info("Teleported party");
		}

		return true;
	}

	public void validate() throws ValidationException {
		for (Level level : levels.values()) {
			level.validate();
		}
	}

	/**
	 * Retourne la luminosité totale en prennant en compte celle générée par les
	 * {@link Champion}s (amulettes, torches, sorts...) et celle naturelle du
	 * niveau sur lequel sont situés les {@link Champion}s.
	 * 
	 * @return un entier positif ou nul dans l'intervalle [0-255].
	 */
	public int getLight() {
		int light = 0;

		// Contribution des champions ?
		final Party party = getParty();

		if (party != null) {
			light += party.getLight();
		}

		// Lumière naturelle du niveau ?
		light += getLevel(party.getPosition().z).getAmbiantLight();

		// TODO 7 niveaux de lumière possibles (enum)
		return Utils.bind(light, 0, 255);
	}
}