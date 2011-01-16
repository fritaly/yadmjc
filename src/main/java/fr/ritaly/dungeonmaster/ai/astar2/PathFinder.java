package fr.ritaly.dungeonmaster.ai.astar2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class PathFinder {
	
	private final Maze maze;
	
	private final List<Square> opened = new ArrayList<Square>();
	
	private final List<Square> closed = new ArrayList<Square>();
	
	private final List<Square> bestList = new ArrayList<Square>();
	
	public PathFinder(Maze maze) {
		Validate.notNull(maze);
		
		this.maze = maze;
	}

	public List<Square> findBestPath(int startX, int startY, int endX, int endY) {
		final Square start = maze.getSquare(startX, startY);
		final Square goal = maze.getSquare(endX, endY);

		System.out.println("Calculating best path...");
		
		for (Square adjacency : start.getAdjacencies()) {
			adjacency.setParent(start);
			
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
						temp.setParent(best);
						
						if (temp.getPassThrough(goal, startX, startY) >= neighbor
								.getPassThrough(goal, startX, startY)) {
							continue;
						}
					}

					if (closed.contains(neighbor)) {
						final Square temp = new Square(neighbor.getX(),
								neighbor.getY(), maze);
						temp.setParent(best);
						
						if (temp.getPassThrough(goal, startX, startY) >= neighbor
								.getPassThrough(goal, startX, startY)) {
							continue;
						}
					}
					
					neighbor.setParent(best);

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
					|| square.getPassThrough(goal, startX, startY) < best.getPassThrough(goal, startX, startY)) {
				best = square;
			}
		}

		return best;
	}
	
	private void populateBestList(Square square, int startX, int startY) {
		bestList.add(square);
		
		final Square parent = square.getParent();
		
		if (!((parent.x == startX) && (parent.y == startY))) {
			populateBestList(square.getParent(), startX, startY);
		}
	}
}