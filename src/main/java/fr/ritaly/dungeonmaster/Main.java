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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.item.Torch;
import fr.ritaly.dungeonmaster.magic.ElementRune;
import fr.ritaly.dungeonmaster.magic.FormRune;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Door;
import fr.ritaly.dungeonmaster.map.Dungeon;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Main {

	private static Log log = LogFactory.getLog(Main.class);

	public static void main(String[] args) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Start");
		}
		
		SoundSystem
				.getInstance()
				.init(
						new File(
								"src\\main\\resources\\sound"));

		log.debug("Building dungeon ...");

		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH);

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 10, 10);
		dungeon.getLevel(1).setElement(3, 3, door);

		door.open();

		Clock.getInstance().setPeriod(166);
		Clock.getInstance().start();

		log.info("Dungeon built");

		if (true) {
			log.debug("Creating party ...");

			final Champion champion = ChampionFactory.getFactory().newChampion(
					Name.WUUF);
			champion.getStats().getHealth().value(10);
			champion.getStats().getMana().value(10);

			final Champion champion2 = ChampionFactory.getFactory()
					.newChampion(Name.TIGGY);

			final Party party = new Party();
			party.addChampion(champion);
			party.addChampion(champion2);

			champion.getBody().getWeaponHand().putOn(new Torch());

			if (true) {
				// champion.getBody().getWeaponHand().disable();

				log.info("Max load = " + champion.getMaxLoad() + " Kg");

				champion2.getStats().getHealth().value(200);

				log.info("Party created");

				dungeon.setParty(5, 5, 1, party);

				for (int i = 0; i < 4; i++) {
					dungeon.moveParty(Move.TURN_LEFT, false);
				}
				for (int i = 0; i < 5; i++) {
					dungeon.moveParty(Move.FORWARD, false);
				}

				// party.removeChampion(champion);

				champion2.getBody().getShieldHand().putOn(
						ItemFactory.getFactory().newItem(Item.Type.TORCH));
				champion2.getBody().getShieldHand()
						.putOn(
								ItemFactory.getFactory().newItem(
										Item.Type.WATER_FLASK));

				for (int i = 0; i < 50; i++) {
					champion.gainExperience(Skill.NINJA, 50);
				}

				Spell spell = champion2.cast(PowerRune.LO, ElementRune.FUL,
						FormRune.IR);

				spell = champion.cast(PowerRune.LO, ElementRune.FUL);
			}
		}

		Thread.sleep(10000);

		// Clock.getInstance().pause();
		//
		// Thread.sleep(500);
		//
		// Clock.getInstance().resume();
		//
		// Thread.sleep(500);

		Clock.getInstance().stop();

		if (log.isInfoEnabled()) {
			log.info("End");
		}
	}
}
