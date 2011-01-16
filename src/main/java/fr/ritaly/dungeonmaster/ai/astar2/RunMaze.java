package fr.ritaly.dungeonmaster.ai.astar2;

import java.util.Random;

public class RunMaze {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Maze maze;
		
		do {
			maze = new Maze(10, 10);
			
			Random random = new Random();
			int goalX = 0, goalY = 0;
			while (goalX == 0 && goalY == 0) {
				goalX = random.nextInt(10);
				goalY = random.nextInt(10);
			}
			
			maze.setStartAndGoal(0, 0, goalX, goalY);
			maze.draw();
		} while (!maze.findBestPath());
	}

}
