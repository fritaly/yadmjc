package fr.ritaly.dungeonmaster.ai.astar2;

import java.util.List;
import java.util.Random;

public class RunMaze {

	public static void main(String[] args) {
		int goalX = 0, goalY = 0;

		List<Square> path = null;

		final Random random = new Random();

		do {
			final Maze maze = new Maze(10, 10);

			final int startX = random.nextInt(10);
			final int startY = random.nextInt(10);

			do {
				goalX = random.nextInt(10);
				goalY = random.nextInt(10);
			} while (goalX == startX && goalY == startY);

			path = new PathFinder(maze).findBestPath(startX, startY, goalX, goalY);
		} while (path == null);
	}
}