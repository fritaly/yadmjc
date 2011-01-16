package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.HashSet;
import java.util.Set;

public class Square {

	final int x;
	final int y;

	private Maze maze;
	private Set<Square> adjacencies = new HashSet<Square>();

	public Square(int x, int y, Maze maze) {
		this.x = x;
		this.y = y;
		this.maze = maze;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Set<Square> getAdjacencies() {
		return adjacencies;
	}

	public void calculateAdjacencies() {
		// int top = x - 1;
		int bottom = x + 1;
		// int left = y - 1;
		int right = y + 1;

		if (bottom < maze.getRows()) {
			if (isAdjacent()) {
				maze.getSquare(bottom, y).adjacencies.add(this);
				this.adjacencies.add(maze.getSquare(bottom, y));
			}
		}

		if (right < maze.getColumns()) {
			if (isAdjacent()) {
				maze.getSquare(x, right).adjacencies.add(this);
				this.adjacencies.add(maze.getSquare(x, right));
			}
		}
	}

	private boolean isAdjacent() {
		return (Math.random() > .5); 
	}
}