package Controller;

import Board.Cell;
import Unit.Unit;

// interface for PlayerController and AIController
public interface Controller {
    void takeTurn(Unit unit);
    void handleInput(Cell clickedCell, Unit activeUnit); // New method for UI events
}