package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.List;

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

	public Maze(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.elements = new Square[rows][columns];
		
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

	public void draw(int startX, int startY, int endX, int endY, List<Square> path) {
		System.out.println("Drawing maze");
		drawContents(startX, startY, endX, endY, path);
		drawBorder();
	}

	private void drawContents(int startX, int startY, int endX, int endY, List<Square> path) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				drawTop(elements[i][j]);
			}
			System.out.println("+");

			for (int j = 0; j < columns; j++) {
				drawLeft(elements[i][j], startX, startY, endX, endY, path);
			}
			System.out.println("|");
		}
	}

	private void drawLeft(Square square, int startX, int startY, int endX, int endY, List<Square> path) {
		final int x = square.getX();
		final int y = square.getY();

		if (y - 1 < 0) {
			if ((square.x == startX) && (square.y == startY)) {
				System.out.print(CLOSED_LEFT_START);
				return;
			}

			if ((square.x == endX) && (square.y == endY)) {
				System.out.print(CLOSED_LEFT_GOAL);
				return;
			}

			if (path.contains(square)) {
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
				if (path.contains(square)) {
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
		if (path.contains(square)) {
			System.out.print(CLOSED_LEFT_PATH);
			return;
		}
		
		System.out.print(CLOSED_LEFT);
	}

	private void drawTop(Square square) {
		final int x = square.getX();
		final int y = square.getY();

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
}