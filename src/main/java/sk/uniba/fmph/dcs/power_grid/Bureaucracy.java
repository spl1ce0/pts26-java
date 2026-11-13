package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface Bureaucracy {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> usePowerPlants(PlayerColor player, List<PowerPlantNumber> ppns, List<Resource> resourcesToBurn);

    String getState();
}
