package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface Bureaucracy extends InterfaceBureaucracy {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> usePowerPlants(
            PlayerColor playerColor,
            List<PowerPlantNumber> powerPlantNumbers,
            List<Resource> resourcesToBurn);

    String getState();
}
