package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;

public class StoreResourcesClass implements StoreResources {

    @Override
    public boolean canStoreResources(List<Resource> resources, List<PowerPlant> powerplants) {
        if (resources == null || powerplants == null) {
            throw new IllegalArgumentException("As listas não podem ser null.");
        }
        if (resources.isEmpty()) {
            return true;
        }
        if (powerplants.isEmpty()) {
            return false;
        }

        int coal = Collections.frequency(resources, Resource.COAL);
        int oil = Collections.frequency(resources, Resource.OIL);
        int garbage = Collections.frequency(resources, Resource.GARBAGE);
        int uranium = Collections.frequency(resources, Resource.URANIUM);

        return canStoreExclusive(Resource.GARBAGE, garbage, powerplants)
                && canStoreExclusive(Resource.URANIUM, uranium, powerplants)
                && canStoreCoalAndOil(coal, oil, powerplants);
    }

    private boolean canStoreExclusive(Resource resource, int count, List<PowerPlant> powerplants) {
        int capacity = 0;
        for (PowerPlant plant : powerplants) {
            if (plant.acceptsResource(resource)) {
                capacity += plant.maxStorageCapacity();
            }
        }
        return count <= capacity;
    }

    private boolean canStoreCoalAndOil(int coalCount, int oilCount, List<PowerPlant> powerplants) {
        int coalOnlyCapacity = 0;
        int oilOnlyCapacity = 0;
        int hybridCapacity = 0;

        for (PowerPlant plant : powerplants) {
            List<Resource> accepted = plant.acceptedResources();
            int capacity = plant.maxStorageCapacity();

            if (accepted.contains(Resource.COAL) && accepted.contains(Resource.OIL)) {
                hybridCapacity += capacity;
            } else if (accepted.contains(Resource.COAL)) {
                coalOnlyCapacity += capacity;
            } else if (accepted.contains(Resource.OIL)) {
                oilOnlyCapacity += capacity;
            }
        }

        int excessCoal = Math.max(0, coalCount - coalOnlyCapacity);
        int excessOil = Math.max(0, oilCount - oilOnlyCapacity);

        return (excessCoal + excessOil) <= hybridCapacity;
    }
}