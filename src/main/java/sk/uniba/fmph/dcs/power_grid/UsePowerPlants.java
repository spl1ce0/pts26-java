package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface UsePowerPlants {
    Variant<RequestError, Integer> numberOfSuppliableTowns(List<Resource> resources, List<PowerPlant> powerplants);
}
