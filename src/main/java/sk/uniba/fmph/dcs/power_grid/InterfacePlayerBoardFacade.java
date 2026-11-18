package sk.uniba.fmph.dcs.power_grid;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the player board facade capabilities needed by other game phases.
 */
public interface InterfacePlayerBoardFacade {

    /**
     * Checks whether the player has at least the specified amount of money.
     *
     * @param amount the money amount to check
     * @return true if player has enough money, false otherwise
     */
    boolean hasEnoughMoney(int amount);

    /**
     * Spends the specified amount of money from the player's account.
     *
     * @param amount the money amount to spend
     */
    void spendMoney(int amount);

    /**
     * Returns the number of cities this player has connected so far.
     *
     * @return count of connected cities
     */
    default int numberOfConnectedCities() {
        return 0;
    }

    /**
     * Buys resources and stores them on the player's power plants.
     *
     * @param resourcesToBuy the resources to buy
     * @return Optional error if buying fails, empty on success
     */
    default Optional<RequestError> buyResources(final List<Resource> resourcesToBuy) {
        return Optional.empty();
    }

    /**
     * Powers cities by burning resources on chosen power plants.
     *
     * @param playerColor       the player color
     * @param powerPlantNumbers the power plant numbers to use
     * @param resourcesToBurn   the resources to burn
     * @return Optional error if usage fails, empty on success
     */
    default Optional<RequestError> usePowerPlants(
            final PlayerColor playerColor,
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        return Optional.empty();
    }

    /**
     * Returns the player's current money balance.
     *
     * @return current money
     */
    default int outstandingMoney() {
        return 0;
    }
}
