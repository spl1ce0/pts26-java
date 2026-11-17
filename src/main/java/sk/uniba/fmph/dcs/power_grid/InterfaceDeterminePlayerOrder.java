package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface representing player order management for GameController.
 */
public interface InterfaceDeterminePlayerOrder {

    List<PlayerColor> reorder();

    List<PlayerColor> gameResult();
}
