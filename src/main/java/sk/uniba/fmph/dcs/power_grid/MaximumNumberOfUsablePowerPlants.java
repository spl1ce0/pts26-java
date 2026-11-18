package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface MaximumNumberOfUsablePowerPlants {
    int numberOfSuppliableTowns(List<Resource> resources, List<PowerPlant> powerplants);
}
