package Board;
import Unit.Unit;

/**
 * Represents a single square in the game
 * Each Cell is capable of holding a Unit/Terrain
 */
public class Cell {
    private Unit occupant;
    private int row;
    private int col;
    private String terrainType;

    public Cell(int row, int col, String terrainType) {
        this.row = row;
        this.col = col;
        this.terrainType = terrainType;
        this.occupant = null; // empty cell at the start
    }

    /**
     * Checks if the cell currently holds a unit
     */
    public boolean isOccupied() {
        return occupant != null;
    }

    public Unit getUnit() {
        return occupant;
    }

    public void setUnit(Unit occupant) {
        this.occupant = occupant;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(String terrainType) {
        this.terrainType = terrainType;
    }
}