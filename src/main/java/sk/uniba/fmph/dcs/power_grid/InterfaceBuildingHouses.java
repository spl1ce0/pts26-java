package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface representing the BuildingHouses phase functionality
 * required by GameController.
 */
public interface InterfaceBuildingHouses {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> buyHouses(
            PlayerColor player,
            List<String> townNames);

    String getState();
}
