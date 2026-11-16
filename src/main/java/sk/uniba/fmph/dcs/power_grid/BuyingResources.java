package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface BuyingResources {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> buyResources(
            PlayerColor playerColor,
            List<Resource> resourcesToBuy);

    String getState();
}
