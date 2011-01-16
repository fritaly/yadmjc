package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.HashSet;
import java.util.Set;

public class Square {

	final int x;
	final int y;

	private double parentCost; // cost of getting from parent square to this node

	private Maze maze;
	private Set<Square> adjacencies = new HashSet<Square>();

	private Square parent;

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

	public Square getParent() {
		return parent;
	}

	public void setParent(Square parent) {
		this.parent = parent;
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

	public double getPassThrough(Square goal, int x, int y) {
		if ((this.x == x) && (this.y == y)) {
			return 0.0;
		}

		return getLocalCost(goal, x, y) + getParentCost(x, y);
	}

	public double getLocalCost(Square goal, int x, int y) {
		if ((this.x == x) && (this.y == y)) {
			return 0.0;
		}

		 // cost of getting from this square to goal
		return 1.0 * (Math.abs(x - goal.getX()) + Math.abs(y - goal.getY()));
	}

	public double getParentCost(int x, int y) {
		if ((this.x == x) && (this.y == y)) {
			return 0.0;
		}

		if (parentCost == 0.0) {
			parentCost = 1.0 + .5 * (parent.getParentCost(x, y) - 1.0);
		}

		return parentCost;
	}
	
	private boolean isAdjacent() {
		return (Math.random() > .5); 
	}
}