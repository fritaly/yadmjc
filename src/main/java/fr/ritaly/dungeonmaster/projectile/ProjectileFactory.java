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
package fr.ritaly.dungeonmaster.projectile;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.map.Dungeon;

/**
 * A factory of {@link Projectile}s.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public interface ProjectileFactory {

	/**
	 * Creates and returns a new {@link Projectile} from given parameters.
	 * 
	 * @param dungeon
	 *            the {@link Dungeon} where the projectile will be created.
	 * @param position
	 *            the {@link Position} where the projectile will be created.
	 * @param direction
	 *            the {@link Direction} the created projectile will be pointing
	 *            to.
	 * @param subCell
	 *            the {@link SubCell} where the projectile will be created.
	 * @return a new instance of {@link Projectile}.
	 */
	public Projectile createProjectile(Dungeon dungeon, Position position,
			Direction direction, SubCell subCell);
}
