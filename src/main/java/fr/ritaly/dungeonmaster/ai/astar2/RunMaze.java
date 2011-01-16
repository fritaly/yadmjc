package fr.ritaly.dungeonmaster.ai.astar2;

import java.util.Random;

public class RunMaze {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Maze maze;
		int startX = 0, startY = 0;
		int goalX = 0, goalY = 0;
		
		do {
			maze = new Maze(10, 10);
			
			Random random = new Random();
			
			startX = random.nextInt(10);
			startY = random.nextInt(10);
			
			do {
				goalX = random.nextInt(10);
				goalY = random.nextInt(10);
			} while (goalX == startX && goalY == startY);
			
			maze.draw(startX, startY, goalX, goalY);
		} while (!maze.findBestPath(startX, startY, goalX, goalY));
	}

}
