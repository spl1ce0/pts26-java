package sk.uniba.fmph.dcs.power_grid;

<<<<<<< HEAD
import java.util.List;
import java.util.Optional;

/**
 * Interface representing the player board facade capabilities needed by other game phases.
=======
/**
 * Interface for PlayerBoardFacade as seen by BuildingHouses.
>>>>>>> origin/feature/power-grid-game-adapter
 */
public interface InterfacePlayerBoardFacade {

    /**
<<<<<<< HEAD
     * Checks whether the player has at least the specified amount of money.
     *
     * @param amount the money amount to check
     * @return true if player has enough money, false otherwise
=======
     * Returns true if the player has at least the given amount of money.
     *
     * @param amount the amount to check
     * @return true if player has enough money
>>>>>>> origin/feature/power-grid-game-adapter
     */
    boolean hasEnoughMoney(int amount);

    /**
<<<<<<< HEAD
     * Spends the specified amount of money from the player's account.
     *
     * @param amount the money amount to spend
=======
     * Deducts the given amount from the player's treasury.
     *
     * @param amount the amount to spend
>>>>>>> origin/feature/power-grid-game-adapter
     */
    void spendMoney(int amount);

    /**
     * Returns the number of cities this player has connected so far.
     *
     * @return count of connected cities
     */
<<<<<<< HEAD
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
=======
    int numberOfConnectedCities();
>>>>>>> origin/feature/power-grid-game-adapter
}
