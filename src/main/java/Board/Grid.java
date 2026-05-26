package Board;

import Unit.Unit;

import java.util.*;

public class Grid {
    private final Cell[][] grid;
    private final int rows;
    private final int columns;
    private final int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new Cell[rows][columns];
    }

    public void loadMap(int mapIndex) {
        Maps map = new Maps(mapIndex);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid[i][j] = new Cell(i, j, map.getMap()[i][j]);
            }
        }
    }

    public Cell getCell(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public Cell getCell(Unit unit) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Cell current = grid[r][c];
                if (current != null && current.isOccupied() && current.getUnit() == unit) {
                    return current;
                }
            }
        }
        return null;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < rows && y >= 0 && y < columns;
    }

    // Wrapper class
    private static class PathNode implements Comparable<PathNode> {
        Cell cell;
        int cost;
        PathNode parent;

        public PathNode(Cell cell, int cost, PathNode parent) {
            this.cell = cell;
            this.cost = cost;
            this.parent = parent;
        }

        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    /**
     * @param start    Origin cell
     * @param end      Target cell for path-finding (null = flood-fill mode)
     * @param mpLimit  Movement budget (-1 = unlimited, used for path-finding)
     * @return Map of every reached cell to its best cost
     */
    private Map<Cell, PathNode> runDijkstra(Cell start, Cell end, int mpLimit) {
        PriorityQueue<PathNode> queue = new PriorityQueue<>();
        Map<Cell, PathNode> bestNodes = new HashMap<>();

        PathNode startNode = new PathNode(start, 0, null);
        bestNodes.put(start, startNode);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            Cell currentCell = current.cell;

            // Early exit when we've found the target (path-finding mode)
            if (end != null && currentCell.equals(end)) {
                break;
            }

            for (int[] dir : direction) {
                int nextX = currentCell.getRow() + dir[0];
                int nextY = currentCell.getCol() + dir[1];
                Cell neighbor = getCell(nextX, nextY);

                if (neighbor == null) continue;

                // In path-finding mode allow stepping onto the occupied end cell;
                // in flood-fill mode skip all occupied cells.
                boolean isEndCell = (end != null && neighbor.equals(end));
                if (neighbor.isOccupied() && !isEndCell) continue;

                int moveCost = neighbor.getTerrainCost();
                if (moveCost == Integer.MAX_VALUE) continue; // impassable

                int totalCost = current.cost + moveCost;

                // Respect the MP budget in flood-fill mode
                if (mpLimit >= 0 && totalCost > mpLimit) continue;

                PathNode existing = bestNodes.get(neighbor);
                if (existing == null || totalCost < existing.cost) {
                    PathNode newNode = new PathNode(neighbor, totalCost, current);
                    bestNodes.put(neighbor, newNode);
                    queue.add(newNode);
                }
            }
        }

        return bestNodes;
    }

    /**
     * Calculate the shortest path between starting cell and end cell using Dijkstra.
     *
     * @param start Origin cell of the unit
     * @param end   Destination cell
     * @return List of Cells representing the best path to take
     */
    public List<Cell> calculatePathDijkstra(Cell start, Cell end) {
        Map<Cell, PathNode> bestNodes = runDijkstra(start, end, -1);

        PathNode endNode = bestNodes.get(end);
        if (endNode == null) return new ArrayList<>();
        return pathConstruct(endNode);
    }

    /**
     * Turn PathNode into List of Cells to represent path.
     */
    private List<Cell> pathConstruct(PathNode targetNode) {
        List<Cell> path = new ArrayList<>();
        PathNode current = targetNode;

        while (current != null) {
            path.add(current.cell);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    /**
     * Calculate all reachable Cells within the unit's MP limit.
     *
     * @param startCell Origin cell of the Unit
     * @param mpLimit   MP of the Unit
     * @return a list of all Cells the unit can reach within the movement limit
     */
    public List<Cell> getReachableCells(Cell startCell, int mpLimit) {
        Map<Cell, PathNode> bestNodes = runDijkstra(startCell, null, mpLimit);

        // Remove the start cell so the unit cannot "move" to its own tile
        bestNodes.remove(startCell);

        return new ArrayList<>(bestNodes.keySet());
    }
}