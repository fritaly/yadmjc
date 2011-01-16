package fr.ritaly.dungeonmaster.ai.astar2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class PathFinder {
	
	private final Maze maze;
	
	private final List<Square> opened = new ArrayList<Square>();
	
	private final List<Square> closed = new ArrayList<Square>();
	
	private final List<Square> bestList = new ArrayList<Square>();
	
	private final Map<Square, Square> parents = new LinkedHashMap<Square, Square>();
	
	public PathFinder(Maze maze) {
		Validate.notNull(maze);
		
		this.maze = maze;
	}

	public List<Square> findBestPath(int startX, int startY, int endX, int endY) {
		final Square start = maze.getSquare(startX, startY);
		final Square goal = maze.getSquare(endX, endY);

		System.out.println("Calculating best path...");
		
		for (Square adjacency : start.getAdjacencies()) {
			parents.put(adjacency, start);
			
			if (!((adjacency.x == startX) && (adjacency.y == startY))) {
				opened.add(adjacency);
			}
		}

		while (opened.size() > 0) {
			final Square best = findBestPassThrough(goal, startX, startY);
			
			opened.remove(best);
			closed.add(best);
			
			if ((best.x == endX) && (best.y == endY)) {
				System.out.println("Found Goal");
				populateBestList(goal, startX, startY);
				
				maze.draw(startX, startY, endX, endY, bestList);
				
				return bestList;
			} else {
				final Set<Square> neighbors = best.getAdjacencies();
				
				for (Square neighbor : neighbors) {
					if (opened.contains(neighbor)) {
						final Square temp = new Square(neighbor.getX(),
								neighbor.getY(), maze);
						
						parents.put(temp, best);
						
						if (getPassThrough(temp, goal, startX, startY) >= getPassThrough(
								neighbor, goal, startX, startY)) {
							
							continue;
						}
					}

					if (closed.contains(neighbor)) {
						final Square temp = new Square(neighbor.getX(),
								neighbor.getY(), maze);
						parents.put(temp, best);
						
						if (getPassThrough(temp, goal, startX, startY) >= getPassThrough(
								neighbor, goal, startX, startY)) {

							continue;
						}
					}
					
					parents.put(neighbor, best);

					opened.remove(neighbor);
					closed.remove(neighbor);
					opened.add(0, neighbor);
				}
			}
		}

		return null;
	}
	
	private Square findBestPassThrough(Square goal, int startX, int startY) {
		Square best = null;
		for (Square square : opened) {
			if (best == null
					|| getPassThrough(square, goal, startX, startY) < getPassThrough(best, goal, startX, startY)) {
				best = square;
			}
		}

		return best;
	}
	
	private void populateBestList(Square square, int startX, int startY) {
		bestList.add(square);
		
		final Square parent = parents.get(square);
		
		if (!((parent.x == startX) && (parent.y == startY))) {
			populateBestList(parents.get(square), startX, startY);
		}
	}
	
	private double getPassThrough(Square square, Square goal, int x, int y) {
		if ((square.x == x) && (square.y == y)) {
			return 0.0;
		}

		return getLocalCost(square, goal, x, y) + getParentCost(square, x, y);
	}
	
	private double getLocalCost(Square square, Square goal, int x, int y) {
		if ((square.x == x) && (square.y == y)) {
			return 0.0;
		}

		 // cost of getting from this square to goal
		return 1.0 * (Math.abs(x - goal.getX()) + Math.abs(y - goal.getY()));
	}
	
	private double getParentCost(Square square, int x, int y) {
		if ((square.x == x) && (square.y == y)) {
			return 0.0;
		}

		return 1.0 + .5 * (getParentCost(parents.get(square), x, y) - 1.0);
	}
}