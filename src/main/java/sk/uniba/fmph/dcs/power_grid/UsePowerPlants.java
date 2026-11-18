package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface for supplying towns with power plants and resources.
 */
public interface UsePowerPlants {

    Variant<RequestError, Integer> numberOfSuppliableTowns(List<Resource> resources, List<PowerPlant> powerplants);
}
