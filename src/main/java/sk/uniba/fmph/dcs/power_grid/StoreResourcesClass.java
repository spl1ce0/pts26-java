package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of StoreResources checking if resources fit in power plants.
 */
public final class StoreResourcesClass implements StoreResources {

    @Override
    public boolean canStoreResources(final List<Resource> resources, final List<PowerPlant> powerplants) {
        if (resources == null || powerplants == null) {
            throw new IllegalArgumentException("Lists cannot be null.");
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

    private boolean canStoreExclusive(final Resource resource, final int count, final List<PowerPlant> powerplants) {
        int capacity = 0;
        for (PowerPlant plant : powerplants) {
            if (plant.acceptsResource(resource)) {
                capacity += plant.maxStorageCapacity();
            }
        }
        return count <= capacity;
    }

    private boolean canStoreCoalAndOil(final int coalCount, final int oilCount, final List<PowerPlant> powerplants) {
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
