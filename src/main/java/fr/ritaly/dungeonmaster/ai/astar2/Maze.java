package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Maze {

	private final int rows;
	private final int columns;
	private final Square[][] elements;
	
	private static final String CLOSED_TOP = "+ - ";
	private static final String OPEN_TOP = "+   ";
	private static final String CLOSED_LEFT = "|   ";
	private static final String CLOSED_LEFT_PATH = "| . ";
	private static final String CLOSED_LEFT_START = "| S ";
	private static final String CLOSED_LEFT_GOAL = "| G ";
	private static final String OPEN_LEFT = "    ";
	private static final String OPEN_LEFT_PATH = "  . ";
	private static final String OPEN_LEFT_START = "  S ";
	private static final String OPEN_LEFT_GOAL = "  G ";

	private final List<Square> opened = new ArrayList<Square>();
	private final List<Square> closed = new ArrayList<Square>();
	private final List<Square> bestList = new ArrayList<Square>();

	public Maze(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		elements = new Square[rows][columns];
		createSquares();
		generateAdjacenies();
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}
	
	private void generateAdjacenies() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j].calculateAdjacencies();
			}
		}
	}

	private void createSquares() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = new Square(i, j, this);
			}
		}
	}

	public Square getSquare(int x, int y) {
		return elements[x][y];
	}

	public void draw() {
		System.out.println("Drawing maze");
		drawContents();
		drawBorder();
	}

	private void drawContents() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				drawTop(elements[i][j]);
			}
			System.out.println("+");

			for (int j = 0; j < columns; j++) {
				drawLeft(elements[i][j]);
			}
			System.out.println("|");
		}
	}

	private void drawLeft(Square square) {
		int x = square.getX();
		int y = square.getY();

		if (y - 1 < 0) {
			if ((square.x == startX) && (square.y == startY)) {
				System.out.print(CLOSED_LEFT_START);
				return;
			}

			if ((square.x == endX) && (square.y == endY)) {
				System.out.print(CLOSED_LEFT_GOAL);
				return;
			}

			if (bestList.contains(square)) {
				System.out.print(CLOSED_LEFT_PATH);
				return;
			}
			System.out.print(CLOSED_LEFT);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
			if (neighbor.getX() == x && neighbor.getY() == y - 1) {
				if ((square.x == endX) && (square.y == endY)) {
					System.out.print(OPEN_LEFT_GOAL);
					return;
				}
				if ((square.x == startX) && (square.y == startY)) {
					System.out.print(OPEN_LEFT_START);
					return;
				}
				if (bestList.contains(square)) {
					System.out.print(OPEN_LEFT_PATH);
					return;
				}
				System.out.print(OPEN_LEFT);
				return;
			}
		}

		if ((square.x == endX) && (square.y == endY)) {
			System.out.print(CLOSED_LEFT_GOAL);
			return;
		}
		
		if ((square.x == startX) && (square.y == startY)) {
			System.out.print(CLOSED_LEFT_START);
			return;
		}

		if (bestList.contains(square)) {
			System.out.print(CLOSED_LEFT_PATH);
			return;
		}
		System.out.print(CLOSED_LEFT);

	}

	private void drawTop(Square square) {
		int x = square.getX();
		int y = square.getY();

		if (x == 0) {
			System.out.print(CLOSED_TOP);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
			if (neighbor.getX() == x - 1 && neighbor.getY() == y) {
				System.out.print(OPEN_TOP);
				return;
			}
		}

		System.out.print(CLOSED_TOP);
	}

	private void drawBorder() {
		for (int i = 0; i < columns; i++) {
			System.out.print(CLOSED_TOP);
		}
		System.out.println("+");
	}
	
	private int startX, startY, endX, endY;
	
	public boolean findBestPath(int startX, int startY, int endX, int endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		
		final Square start = elements[startX][startY];
		final Square goal = elements[endX][endY];

		System.out.println("Calculating best path...");
		Set<Square> adjacencies = start.getAdjacencies();
		for (Square adjacency : adjacencies) {
			adjacency.setParent(start);
			if (!((adjacency.x == startX) && (adjacency.y == startY))) {
				opened.add(adjacency);
			}
		}

		while (opened.size() > 0) {
			Square best = findBestPassThrough(goal);
			opened.remove(best);
			closed.add(best);
			if ((best.x == endX) && (best.y == endY)) {
				System.out.println("Found Goal");
				populateBestList(goal);
				draw();
				return true;
			} else {
				Set<Square> neighbors = best.getAdjacencies();
				for (Square neighbor : neighbors) {
					if (opened.contains(neighbor)) {
						Square tmpSquare = new Square(neighbor.getX(),
								neighbor.getY(), this);
						tmpSquare.setParent(best);
						if (tmpSquare.getPassThrough(goal, startX, startY) >= neighbor
								.getPassThrough(goal, startX, startY)) {
							continue;
						}
					}

					if (closed.contains(neighbor)) {
						Square tmpSquare = new Square(neighbor.getX(),
								neighbor.getY(), this);
						tmpSquare.setParent(best);
						if (tmpSquare.getPassThrough(goal, startX, startY) >= neighbor
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

		return false;
	}

	private void populateBestList(Square square) {
		bestList.add(square);
		Square r = square.getParent();
		if (!((r.x == startX) && (r.y == startY))) {
			populateBestList(square.getParent());
		}

		return;
	}

	private Square findBestPassThrough(Square goal) {
		Square best = null;
		for (Square square : opened) {
			if (best == null
					|| square.getPassThrough(goal, startX, startY) < best.getPassThrough(goal, startX, startY)) {
				best = square;
			}
		}

		return best;
	}
}