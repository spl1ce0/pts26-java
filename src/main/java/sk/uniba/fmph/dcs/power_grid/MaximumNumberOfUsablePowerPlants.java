package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface for calculating the maximum number of suppliable towns.
 */
public interface MaximumNumberOfUsablePowerPlants {

    int numberOfSuppliableTowns(List<Resource> resources, List<PowerPlant> powerplants);

    default int numberOfSuppliebleTowns(List<Resource> resources, List<PowerPlant> powerplants) {
        return numberOfSuppliableTowns(resources, powerplants);
    }
}
