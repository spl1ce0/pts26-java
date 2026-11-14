package sk.uniba.fmph.dcs.power_grid;

import java.util.List;
import java.util.Optional;

public interface PlayerPowerPlants {

    PowerPlantNumber bestPowerPlant();

    Optional<RequestError> removePowerPlant(PowerPlantNumber ppn, List<Resource> newResources);

    int numberOfConnectableCities(List<Resource> resources);

    boolean canStoreResources(List<Resource> resources);

    Variant<Integer, RequestError> usePowerPlant(List<PowerPlantNumber> ppns, List<Resource> resourcesToBurn);
}
