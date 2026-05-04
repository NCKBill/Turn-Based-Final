package Controller;

import Unit.Unit;
import java.util.*;

public interface AIStrategy {
    void executeTurn(Unit self, List<Unit> unitList, GameManager gameManager);
}
