package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.HashSet;
import java.util.Set;

public class Square {

	private int x;
	private int y;
	private boolean start;
	private boolean end;

	private double localCost; // cost of getting from this square to goal
	private double parentCost; // cost of getting from parent square to this node
	private double passThroughCost;// cost of getting from the start to the goal
	// through this square

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

	public void setX(int x) {

		this.x = x;
	}

	public int getY() {

		return y;
	}

	public void setY(int y) {

		this.y = y;
	}

	public boolean isStart() {

		return start;
	}

	public void setStart(boolean start) {

		this.start = start;
	}

	public boolean isEnd() {

		return end;
	}

	public void setEnd(boolean end) {

		this.end = end;
	}

	public Set<Square> getAdjacencies() {

		return adjacencies;
	}

	public void setAdjacencies(Set<Square> adjacencies) {

		this.adjacencies = adjacencies;
	}

	public Square getParent() {

		return parent;
	}

	public void setParent(Square parent) {

		this.parent = parent;
	}

	public void calculateAdjacencies() {

		int top = x - 1;
		int bottom = x + 1;
		int left = y - 1;
		int right = y + 1;

		if (bottom < maze.getRows()) {
			if (isAdjacent()) {
				maze.getSquare(bottom, y).addAdjacency(this);
				this.addAdjacency(maze.getSquare(bottom, y));
			}
		}

		if (right < maze.getColumns()) {
			if (isAdjacent()) {
				maze.getSquare(x, right).addAdjacency(this);
				this.addAdjacency(maze.getSquare(x, right));
			}
		}
	}

	public void addAdjacency(Square square) {

		adjacencies.add(square);
	}
	
	public void removeAdjacency(Square square) {
		adjacencies.remove(square);
	}

	public double getPassThrough(Square goal) {

		if (isStart()) {
			return 0.0;
		}

		return getLocalCost(goal) + getParentCost();
	}

	public double getLocalCost(Square goal) {

		if (isStart()) {
			return 0.0;
		}

		localCost = 1.0 * (Math.abs(x - goal.getX()) + Math.abs(y - goal.getY()));
		return localCost;
	}

	public double getParentCost() {

		if (isStart()) {
			return 0.0;
		}

		if (parentCost == 0.0) {
			parentCost = 1.0 + .5 * (parent.getParentCost() - 1.0);
		}

		return parentCost;
	}
	
	public boolean isAdjacent() {

		if (Math.random() > .5) {
			return true;
		}
		return false;
	}

}
