package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface representing the BuyingResources phase functionality
 * required by GameController.
 */
public interface InterfaceBuyingResources {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> buyResources(
            PlayerColor player,
            List<Resource> resources);

    String getState();
}
