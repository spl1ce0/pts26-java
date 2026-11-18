package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface representing the Bureaucracy phase functionality
 * required by GameController.
 */
public interface InterfaceBureaucracy {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> usePowerPlants(
            PlayerColor player,
            List<PowerPlantNumber> ppns,
            List<Resource> resourcesToBurn);

    String getState();
}
