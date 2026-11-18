package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Public interface for interacting with the Power Grid game engine.
 * Accepts primitive and string representations, converting them for the internal engine,
 * and returning the updated {@link SerializedGameState} or a {@link RequestError}.
 */
public interface PowerGridRulesGameInterface {

    /**
     * Phase 2: Selects a power plant from the current market to initiate an auction.
     *
     * @param playerId         the ID of the bidding player
     * @param powerPlantNumber the number of the selected power plant
     * @param firstBid         the opening bid amount
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> selectPowerPlantToBid(
            int playerId,
            int powerPlantNumber,
            int firstBid);

    /**
     * Phase 2: Player chooses to pass offering a power plant for auction.
     *
     * @param playerId the ID of the passing player
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> doNotWantPowerPlant(int playerId);

    /**
     * Phase 2: Places a higher bid on the power plant currently under auction.
     *
     * @param playerId the ID of the bidding player
     * @param bid      the bid amount
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> bidForCurrentPowerPlant(int playerId, int bid);

    /**
     * Phase 2: Drops out of the auction for the currently active power plant.
     *
     * @param playerId the ID of the dropping player
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> doNotBidForCurrentPowerPlant(int playerId);

    /**
     * Scraps an owned power plant and reassigns or discards resources.
     *
     * @param playerId         the ID of the player
     * @param powerPlantNumber the power plant number to remove
     * @param removedResources encoded resource string (C, O, G, U) from scrapped plant
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> removePowerPlant(
            int playerId,
            int powerPlantNumber,
            String removedResources);

    /**
     * Phase 3: Buys resources from the resource market.
     *
     * @param playerId  the ID of the purchasing player
     * @param resources encoded resource string (C, O, G, U)
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> buyResources(int playerId, String resources);

    /**
     * Phase 4: Builds houses in cities.
     *
     * @param playerId  the ID of the building player
     * @param townNames the list of town names to connect (may be empty)
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> buyHouses(int playerId, List<String> townNames);

    /**
     * Phase 5: Burns resources in power plants to supply electricity to connected cities.
     *
     * @param playerId          the ID of the powering player
     * @param powerPlantNumbers list of power plant numbers to fire
     * @param resourcesToBurn   encoded resource string (C, O, G, U)
     * @return updated game state, or RequestError if invalid
     */
    Variant<SerializedGameState, RequestError> usePowerPlants(
            int playerId,
            List<Integer> powerPlantNumbers,
            String resourcesToBurn);
}
