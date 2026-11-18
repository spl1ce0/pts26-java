package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Interface representing PlayerPowerPlants functionality for PlayerBoardFacade and PlayerResources.
 */
public interface InterfacePlayerPowerPlants {

    /**
     * Returns the highest numbered power plant owned by the player.
     *
     * @return best power plant number or null if none owned
     */
    default PowerPlantNumber bestPowerPlant() {
        return null;
    }

    /**
     * Receives a power plant card.
     *
     * @param powerPlant the power plant
     * @return true if added within limit, false otherwise
     */
    default boolean receivePowerPlant(final PowerPlant powerPlant) {
        return true;
    }

    /**
     * Receives a power plant by number.
     *
     * @param powerPlantNumber the power plant number
     * @return true if added within limit, false otherwise
     */
    default boolean receivePowerPlant(final PowerPlantNumber powerPlantNumber) {
        return true;
    }

    /**
     * Checks if resources can be stored across owned plants.
     *
     * @param resources the resources
     * @return true if can store, false otherwise
     */
    boolean canStoreResources(List<Resource> resources);

    /**
     * Returns the number of connectable cities with the given resources.
     *
     * @param resources the resources
     * @return number of connectable cities
     */
    int numberOfConnectableCities(List<Resource> resources);

    /**
     * Activates power plants to burn resources.
     *
     * @param powerPlantNumbers power plants to use
     * @param resourcesToBurn   resources to burn
     * @return variant of powered cities or error
     */
    default Variant<Integer, RequestError> usePowerPlant(
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        return Variant.ofA(0);
    }

    /**
     * Removes a power plant and reassigns remaining resources.
     *
     * @param powerPlantNumber   power plant to remove
     * @param remainingResources remaining resources
     * @return optional error if removal fails
     */
    Optional<RequestError> removePowerPlant(
            PowerPlantNumber powerPlantNumber,
            List<Resource> remainingResources);

    /**
     * Returns the list of owned power plants.
     *
     * @return list of power plants
     */
    default List<PowerPlant> getPowerPlants() {
        return Collections.emptyList();
    }
}
