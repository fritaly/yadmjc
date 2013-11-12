package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.HashSet;
import java.util.Set;

public class Square {
	final int x;
	final int y;

	private final Maze maze;
	private final Set<Square> adjacencies = new HashSet<Square>();

	public Square(int x, int y, Maze maze) {
		this.x = x;
		this.y = y;
		this.maze = maze;
	}

	public Set<Square> getAdjacencies() {
		return adjacencies;
	}

	void calculateAdjacencies() {
		// int top = x - 1;
		int bottom = x + 1;
		// int left = y - 1;
		int right = y + 1;

		if (bottom < maze.rows) {
			if (isAdjacent()) {
				maze.getSquare(bottom, y).adjacencies.add(this);
				this.adjacencies.add(maze.getSquare(bottom, y));
			}
		}

		if (right < maze.columns) {
			if (isAdjacent()) {
				maze.getSquare(x, right).adjacencies.add(this);
				this.adjacencies.add(maze.getSquare(x, right));
			}
		}
	}

	private boolean isAdjacent() {
		// This random determines if one can move from one square to another
		return (Math.random() > .5);
	}
}