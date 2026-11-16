package sk.uniba.fmph.dcs.power_grid;

import java.util.List;
import java.util.Optional;

public interface PlayerBoardFacade {

    boolean hasEnoughMoney(int amount);

    void spendMoney(int amount);

    Optional<RequestError> buyResources(List<Resource> resourcesToBuy);

    Optional<RequestError> usePowerPlants(
            PlayerColor playerColor,
            List<PowerPlantNumber> powerPlantNumbers,
            List<Resource> resourcesToBurn);

    int outstandingMoney();
}
