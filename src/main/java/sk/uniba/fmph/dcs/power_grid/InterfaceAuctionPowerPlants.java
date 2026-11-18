package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface representing the AuctionPowerPlants phase functionality
 * required by GameController.
 */
public interface InterfaceAuctionPowerPlants {

    void startPhase(List<PlayerColor> playerOrder);

    Variant<PhaseStatus, RequestError> selectPowerPlantToBid(
            PlayerColor player,
            PowerPlantNumber ppn,
            int firstBid);

    Variant<PhaseStatus, RequestError> doNotWantPowerPlant(PlayerColor player);

    Variant<PhaseStatus, RequestError> bidForCurrentPowerPlant(PlayerColor player, int bid);

    Variant<PhaseStatus, RequestError> doNotBidForCurrentPowerPlant(PlayerColor player);

    String getState();
}
