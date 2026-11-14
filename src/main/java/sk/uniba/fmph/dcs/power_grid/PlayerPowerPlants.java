package sk.uniba.fmph.dcs.power_grid;

import java.util.List;
import java.util.Optional;

public interface PlayerPowerPlants {

    boolean canStoreResources(List<Resource> resources);

    int numberOfConnectableCities(List<Resource> resources);

    Variant<Integer, RequestError> usePowerPlant(
            List<PowerPlantNumber> powerPlantNumbers,
            List<Resource> resourcesToBurn);

    Optional<RequestError> removePowerPlant(
            PowerPlantNumber powerPlantNumber,
            List<Resource> remainingResources);
}
