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

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;
import fr.ritaly.dungeonmaster.map.Pit;

public class ActionTest extends TestCase {

	public ActionTest() {
	}

	public ActionTest(String name) {
		super(name);
	}

	public void testSuccessfullyUsingAnActionIncreasesXp() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.gainExperience(Skill.AIR, 1000000);

		// The sceptre of lyt has the light action
		final Item sceptre = ItemFactory.getFactory().newItem(Item.Type.SCEPTRE_OF_LYT);
		tiggy.getBody().getWeaponHand().putOn(sceptre);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(1, 1, 1), party);

		// Store the xp value before using the action
		final int xp = tiggy.getExperience(Action.LIGHT.getSkill()).getPoints();

		assertTrue(sceptre.perform(Action.LIGHT));

		// Ensure the xp increased due to the action
		assertTrue(xp < tiggy.getExperience(Action.LIGHT.getSkill()).getPoints());
	}

	public void testUsingAnActionConsumesStamina() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.gainExperience(Skill.AIR, 1000000);

		// The sceptre of lyt has the light action
		final Item sceptre = ItemFactory.getFactory().newItem(Item.Type.SCEPTRE_OF_LYT);
		tiggy.getBody().getWeaponHand().putOn(sceptre);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(1, 1, 1), party);

		// Store the stamina value before using the action
		final int stamina = tiggy.getStats().getStamina().value();

		assertTrue(sceptre.perform(Action.LIGHT));

		// Ensure the stamina decreased due to the action
		assertEquals(stamina - Action.LIGHT.getStamina(), tiggy.getStats().getStamina().value());
	}

	public void testLightAction() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.gainExperience(Skill.AIR, 1000000);

		// The sceptre of lyt has the light action
		final Item sceptre = ItemFactory.getFactory().newItem(Item.Type.SCEPTRE_OF_LYT);
		tiggy.getBody().getWeaponHand().putOn(sceptre);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(1, 1, 1), party);

		// Store the light value before using the action
		final int light = tiggy.getSpells().getLight().value();

		assertTrue(sceptre.perform(Action.LIGHT));

		// Ensure the light increased due to the action
		assertTrue(light < tiggy.getSpells().getLight().value());
	}

	public void testBlockAction() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);

		// The buckler has the block action
		final Item buckler = ItemFactory.getFactory().newItem(Item.Type.BUCKLER);
		tiggy.getBody().getWeaponHand().putOn(buckler);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(1, 1, 1), party);

		// Store the shield value before using the action
		final int shield = tiggy.getStats().getShield().value();

		// When the block action succeds, the champion's defense increases for a
		// short duration
		boolean success = false;

		for (int i = 0; i < 20; i++) {
			success = buckler.perform(Action.BLOCK);

			if (success) {
				break;
			}
		}

		assertTrue("The action BLOCK didn't succeed despite 20 attempts", success);

		// Ensure the shield increased due to the block action
		assertEquals(shield + Action.BLOCK.getShieldModifier(), tiggy.getStats().getShield().value());
	}

	public void testHealAction() {
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+
		// | W | P | W |
		// +---+---+---+
		// | W | W | W |
		// +---+---+---+

		final Dungeon dungeon = new Dungeon();
		dungeon.createLevel(1, 3, 3);

		// Create 4 champions with a health of 400 and a max health of 500
		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getStats().getHealth().baseMaxValue(500);
		tiggy.getStats().getHealth().baseValue(400);

		final Champion daroou = ChampionFactory.getFactory().newChampion(Name.DAROOU);
		daroou.getStats().getHealth().baseMaxValue(500);
		daroou.getStats().getHealth().baseValue(400);

		final Champion halk = ChampionFactory.getFactory().newChampion(Name.HALK);
		halk.getStats().getHealth().baseMaxValue(500);
		halk.getStats().getHealth().baseValue(400);

		final Champion wuuf = ChampionFactory.getFactory().newChampion(Name.WUUF);
		wuuf.getStats().getHealth().baseMaxValue(500);
		wuuf.getStats().getHealth().baseValue(400);

		// The cross of Neta has the heal action
		final Item crossOfNeta = ItemFactory.getFactory().newItem(Item.Type.CROSS_OF_NETA);
		tiggy.getBody().getWeaponHand().putOn(crossOfNeta);

		final Party party = new Party();
		party.addChampion(tiggy);
		party.addChampion(daroou);
		party.addChampion(halk);
		party.addChampion(wuuf);

		dungeon.setParty(new Position(1, 1, 1), party);

		// Store the initial health for each champion
		final int tiggyHealth = tiggy.getStats().getHealth().value();
		final int daroouHealth = daroou.getStats().getHealth().value();
		final int halkHealth = halk.getStats().getHealth().value();
		final int wuufHealth = wuuf.getStats().getHealth().value();

		// The action heals all the champions in the party
		assertTrue(crossOfNeta.perform(Action.HEAL));

		// The champions' health must have increased
		assertTrue(tiggy.getStats().getHealth().value() > tiggyHealth);
		assertTrue(daroou.getStats().getHealth().value() > daroouHealth);
		assertTrue(halk.getStats().getHealth().value() > halkHealth);
		assertTrue(wuuf.getStats().getHealth().value() > wuufHealth);
	}

	public void testThrowAction() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | T | . | . | <-+---+---+-N-+ P | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		final Item dagger = ItemFactory.getFactory().newItem(Item.Type.DAGGER);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(dagger);

		final Party party = new Party(tiggy);
		party.setDirection(Direction.WEST);

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 10, 10);

		dungeon.setParty(8, 5, 1, party);

		// --- No item on the floor initially
		final Element target = level1.getElement(1, 5);
		final Element neighbour = level1.getElement(7, 5);

		assertFalse(target.hasItems());

		// --- Use the THROW action
		dagger.perform(Action.THROW);

		// --- A projectile must have been created on the neighbor position in SE
		assertTrue(neighbour.hasProjectiles());
		assertEquals(1, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(Sector.SOUTH_EAST));

		assertFalse(target.hasProjectiles());

		// --- The item must have left the champion's weapon hand
		assertTrue(tiggy.getBody().getWeaponHand().isEmpty());

		// Let the projectile move
		Clock.getInstance().tick(3);

		// --- Ensure the projectile moved to the sector SW
		assertTrue(neighbour.hasProjectiles());
		assertEquals(1, neighbour.getProjectiles().size());
		assertNotNull(neighbour.getProjectiles().get(Sector.SOUTH_WEST));

		assertFalse(target.hasProjectiles());

		// Wait enough to let the projectile hit the opposite wall
		Clock.getInstance().tick(60);

		// The projectile must have disappeared
		assertFalse(neighbour.hasProjectiles());
		assertFalse(target.hasProjectiles());

		// ... and dropped the dagger on the floor
		assertEquals(1, target.getItemCount());

		assertFalse(target.getItems(Sector.SOUTH_WEST).isEmpty());
		assertEquals(1, target.getItemCount(Sector.SOUTH_WEST));
		assertEquals(Item.Type.DAGGER, target.getItems(Sector.SOUTH_WEST).iterator().next().getType());
	}

	public void testFluxCageAction() throws Throwable {
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | F | P | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | . | . | . | . | . | . | . | . | W |
		// +---+---+---+---+---+---+---+---+---+---+
		// | W | W | W | W | W | W | W | W | W | W |
		// +---+---+---+---+---+---+---+---+---+---+

		// The firestaff can create flux cages
		final Item firestaff = ItemFactory.getFactory().newItem(Item.Type.THE_FIRESTAFF_COMPLETE);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(firestaff);

		final Party party = new Party(tiggy);
		party.setDirection(Direction.WEST);

		final Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 10, 10);

		dungeon.setParty(8, 5, 1, party);

		// --- No flux cage initially
		final Element neighbour = level1.getElement(7, 5);

		assertFalse(neighbour.hasFluxCage());

		// --- Use the FLUX_CAGE action
		firestaff.perform(Action.FLUX_CAGE);

		// --- A flux cage must have appeared on the neighbour position
		assertTrue(neighbour.hasFluxCage());

		// Wait enough to let the flux cage disappear
		Clock.getInstance().tick(60);

		assertFalse(neighbour.hasFluxCage());
	}

	public void testClimbDownAction() {
		// Level1:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | P | I | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// Level2:
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(3, 2, new Pit());

		dungeon.createLevel(2, 5, 5);

		// The rope has the CLIMB_DOWN action
		final MiscItem rope = new MiscItem(Item.Type.ROPE);

		final Champion tiggy = ChampionFactory.getFactory().newChampion(Name.TIGGY);
		tiggy.getBody().getWeaponHand().putOn(rope);

		final Party party = new Party();
		party.addChampion(tiggy);

		dungeon.setParty(new Position(2, 2, 1), party);

		// --- Initial state
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// The party can only climb down if it's facing a pit
		assertEquals(Direction.NORTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN)); // The action fails

		// The action fails again for the same reason
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.WEST, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));

		// The action fails again for the same reason
		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.SOUTH, party.getLookDirection());
		assertFalse(rope.perform(Action.CLIMB_DOWN));

		// --- The party climbs down through the open pit. The champion's health
		// must remain constant
		final int health = tiggy.getStats().getHealth().value();

		assertTrue(dungeon.moveParty(Move.TURN_LEFT, true, AudioClip.STEP));
		assertEquals(Direction.EAST, party.getLookDirection());
		assertTrue(rope.perform(Action.CLIMB_DOWN));

		assertEquals(new Position(3, 2, 2), dungeon.getParty().getPosition());

		assertEquals(health, tiggy.getStats().getHealth().value());
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}