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

import junit.framework.TestCase;
import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Orientation;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.actuator.SimpleActuator;
import fr.ritaly.dungeonmaster.actuator.TestActuator;
import fr.ritaly.dungeonmaster.actuator.TriggerAction;
import fr.ritaly.dungeonmaster.actuator.TriggerType;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.champion.Champion.Name;
import fr.ritaly.dungeonmaster.champion.ChampionFactory;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.item.Torch;

public class FloorSwitchTest extends TestCase {

	public FloorSwitchTest() {
	}

	public FloorSwitchTest(String name) {
		super(name);
	}

	public void testActuatorTriggeredWhenAnyItemDropped() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | F | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final TestActuator actuator = new TestActuator();

		final Level level1 = dungeon.createLevel(1, 5, 5);

		final FloorSwitch floorSwitch = new FloorSwitch();
		level1.setElement(2, 2, floorSwitch);

		floorSwitch.addActuator(TriggerType.ITEM_DROPPED, actuator);

		// --- Initial state
		assertFalse(actuator.isTriggered());

		// --- Drop an item in NW - the actuator must be triggered
		level1.getElement(2, 2).addItem(new Torch(), Sector.NORTH_WEST);
		Clock.getInstance().tick();
		assertTrue(actuator.isTriggered());
		actuator.reset();

		// --- Drop another item - the actuator must not be triggered
		assertFalse(actuator.isTriggered());

		level1.getElement(2, 2).addItem(new Torch(), Sector.NORTH_WEST);
		Clock.getInstance().tick();
		assertFalse(actuator.isTriggered());
	}

	public void testActuatorTriggeredWhenAnyItemPickedUp() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | F | . | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		// FIXME Implement an actuator that triggers when multiple conditions are met

		Dungeon dungeon = new Dungeon();

		final TestActuator actuator = new TestActuator();

		final Level level1 = dungeon.createLevel(1, 5, 5);

		final FloorSwitch floorSwitch = new FloorSwitch();
		level1.setElement(2, 2, floorSwitch);

		floorSwitch.addActuator(TriggerType.ITEM_PICKED_UP, actuator);

		// --- Initial state
		final Torch torch1 = new Torch();
		final Torch torch2 = new Torch();

		assertFalse(actuator.isTriggered());
		level1.getElement(2, 2).addItem(torch1, Sector.NORTH_WEST);
		level1.getElement(2, 2).addItem(torch2, Sector.NORTH_WEST);
		assertEquals(2, level1.getElement(2, 2).getItemCount());
		Clock.getInstance().tick();
		assertFalse(actuator.isTriggered());

		// --- Pick an item which isn't the last one - the actuator must not be triggered
		assertEquals(torch2, level1.getElement(2, 2).removeItem(Sector.NORTH_WEST));
		assertEquals(1, level1.getElement(2, 2).getItemCount());
		Clock.getInstance().tick();
		assertFalse(actuator.isTriggered());

		// --- Pick the last item - the actuator must be triggered
		assertEquals(torch1, level1.getElement(2, 2).removeItem(Sector.NORTH_WEST));
		assertEquals(0, level1.getElement(2, 2).getItemCount());
		Clock.getInstance().tick();
		assertTrue(actuator.isTriggered());
	}

	public void testActuatorTriggeredWhenPartyStepsOff() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | F | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final TestActuator actuator = new TestActuator();

		final Level level1 = dungeon.createLevel(1, 5, 5);

		final FloorSwitch floorSwitch = new FloorSwitch();
		level1.setElement(2, 2, floorSwitch);

		floorSwitch.addActuator(TriggerType.PARTY_STEPPED_OFF, actuator);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 3, 1), party);

		// --- Initial state
		assertEquals(new Position(2, 3, 1), dungeon.getParty().getPosition());
		assertFalse(actuator.isTriggered());

		// --- The party moves forward - nothing happens
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		Clock.getInstance().tick();

		assertFalse(actuator.isTriggered());

		// --- The party moves forward and triggers the actuator
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 1, 1), dungeon.getParty().getPosition());

		Clock.getInstance().tick();

		assertTrue(actuator.isTriggered());
	}

	public void testActuatorTriggeredWhenPartyStepsOn() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | . | . | W |
		// +---+---+---+---+---+
		// | W | . | F | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final TestActuator actuator = new TestActuator();

		final Level level1 = dungeon.createLevel(1, 5, 5);

		final FloorSwitch floorSwitch = new FloorSwitch();
		level1.setElement(2, 2, floorSwitch);

		floorSwitch.addActuator(TriggerType.PARTY_STEPPED_ON, actuator);

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 3, 1), party);

		// --- Initial state
		assertEquals(new Position(2, 3, 1), dungeon.getParty().getPosition());
		assertFalse(actuator.isTriggered());

		// --- The party moves forward and triggers the actuator
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		Clock.getInstance().tick();

		// --- Final situation
		assertTrue(actuator.isTriggered());
	}

	public void testPressurePadTriggeringDoor() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | W | D | W | W |
		// +---+---+---+---+---+
		// | W | . | F | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Door door = new Door(Door.Style.WOODEN, Orientation.NORTH_SOUTH);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(1, 1, new Wall());
		level1.setElement(2, 1, door);
		level1.setElement(3, 1, new Wall());

		final FloorSwitch floorSwitch = new FloorSwitch();
		level1.setElement(2, 2, floorSwitch);

		floorSwitch.addActuator(TriggerType.PARTY_STEPPED_ON, new SimpleActuator(2, TriggerAction.TOGGLE, door));
		floorSwitch.addActuator(TriggerType.PARTY_STEPPED_OFF, new SimpleActuator(2, TriggerAction.TOGGLE, door));

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 3, 1), party);

		// --- Initial state
		assertEquals(new Position(2, 3, 1), dungeon.getParty().getPosition());
		assertEquals(Door.State.CLOSED, door.getState());
		assertEquals(Door.Motion.IDLE, door.getMotion());

		// --- The party moves forward - the door must open
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// Let the door open
		Clock.getInstance().tick(18);

		assertEquals(Door.Motion.IDLE, door.getMotion());
		assertEquals(Door.State.OPEN, door.getState());

		// --- The party moves backwards - the door must close
		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 3, 1), dungeon.getParty().getPosition());

		// Let the door close
		Clock.getInstance().tick(18);

		assertEquals(Door.Motion.IDLE, door.getMotion());
		assertEquals(Door.State.CLOSED, door.getState());
	}

	public void testPressurePadTriggeringPit() {
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+
		// | W | . | I | . | W |
		// +---+---+---+---+---+
		// | W | . | F | . | W |
		// +---+---+---+---+---+
		// | W | . | P | . | W |
		// +---+---+---+---+---+
		// | W | W | W | W | W |
		// +---+---+---+---+---+

		Dungeon dungeon = new Dungeon();

		final Pit pit = new Pit(false, false);

		final Level level1 = dungeon.createLevel(1, 5, 5);
		level1.setElement(2, 1, pit);

		final FloorSwitch floorSwitch = new FloorSwitch();
		level1.setElement(2, 2, floorSwitch);

		floorSwitch.addActuator(TriggerType.PARTY_STEPPED_ON, new SimpleActuator(2, TriggerAction.TOGGLE, pit));
		floorSwitch.addActuator(TriggerType.PARTY_STEPPED_OFF, new SimpleActuator(2, TriggerAction.TOGGLE, pit));

		Party party = new Party();
		party.addChampion(ChampionFactory.getFactory().newChampion(Name.TIGGY));

		dungeon.setParty(new Position(2, 3, 1), party);

		// --- Initial state
		assertEquals(new Position(2, 3, 1), dungeon.getParty().getPosition());
		assertTrue(pit.isClosed());

		// --- The party moves forward - the pit must open
		assertTrue(dungeon.moveParty(Move.FORWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 2, 1), dungeon.getParty().getPosition());

		// Let the pit open
		Clock.getInstance().tick(2);

		assertTrue(pit.isOpen());

		// --- The party moves backwards - the pit must close
		assertTrue(dungeon.moveParty(Move.BACKWARD, true, AudioClip.STEP));
		assertEquals(new Position(2, 3, 1), dungeon.getParty().getPosition());

		// Let the pit open
		Clock.getInstance().tick(2);

		assertTrue(pit.isClosed());
	}

	@Override
	protected void setUp() throws Exception {
		Clock.getInstance().reset();
	}
}