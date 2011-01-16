package fr.ritaly.dungeonmaster.ai.astar2;
public class RunMaze {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Maze maze = new Maze(10, 10);
		maze.draw();
		maze.findBestPath();
	}

}
