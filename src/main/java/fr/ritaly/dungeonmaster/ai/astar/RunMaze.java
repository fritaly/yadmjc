package fr.ritaly.dungeonmaster.ai.astar;

import java.util.List;
import java.util.Random;

import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.ai.astar2.Maze;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;

public class RunMaze {

	public static void main(String[] args) {
		int startX = 0, startY = 0;
		int goalX = 0, goalY = 0;
		
		List<Element> path = null;
		
		do {
			Dungeon dungeon = new Dungeon();
			Level level = new Level(dungeon, 1, 10, 10);
			
			Random random = new Random();
			
			startX = random.nextInt(10);
			startY = random.nextInt(10);
			
			do {
				goalX = random.nextInt(10);
				goalY = random.nextInt(10);
			} while (goalX == startX && goalY == startY);

			path = new PathFinder(level, Materiality.MATERIAL).findBestPath(
					startX, startY, goalX, goalY);
		} while (path == null);
	}

}
