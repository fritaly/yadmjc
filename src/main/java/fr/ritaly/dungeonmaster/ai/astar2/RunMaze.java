package fr.ritaly.dungeonmaster.ai.astar2;
public class RunMaze {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Maze maze;
		
		do {
			maze = new Maze(10, 10);
			maze.draw();
		} while (!maze.findBestPath());
	}

}
