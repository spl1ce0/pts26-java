package sk.uniba.fmph.dcs.power_grid;

import java.util.List;
import java.util.Optional;

public interface PlayerResources {

    Optional<RequestError> removePowerPlant(
            PowerPlantNumber powerPlantNumber,
            List<Resource> resourcesToRemove);

    int numberOfConnectableCities();

    Optional<RequestError> buyResources(List<Resource> resourcesToBuy);

    Variant<Integer, RequestError> usePowerPlant(
            List<PowerPlantNumber> powerPlantNumbers,
            List<Resource> resourcesToBurn);
}
