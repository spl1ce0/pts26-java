package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of UsePowerPlants validating fuel burning and calculating supplied towns.
 */
public final class UsePowerPlantsClass implements UsePowerPlants {

    @Override
    public Variant<RequestError, Integer> numberOfSuppliableTowns(
            final List<Resource> resources, final List<PowerPlant> powerplants) {
        if (resources == null || powerplants == null) {
            throw new IllegalArgumentException("Lists cannot be null.");
        }
        if (powerplants.isEmpty()) {
            if (resources.isEmpty()) {
                return Variant.ofB(0);
            }
            return Variant.ofA(new RequestError("No power plants specified to burn resources."));
        }

        int totalRequired = calculateTotalRequiredResources(powerplants);
        int totalCities = calculateTotalCities(powerplants);

        if (resources.size() != totalRequired) {
            return Variant.ofA(new RequestError(
                    "Resource count mismatch: required " + totalRequired + ", but got " + resources.size()));
        }
        if (!matchesRequirements(resources, powerplants)) {
            return Variant.ofA(new RequestError(
                    "Provided resources do not match the required fuel types for the selected power plants."));
        }

        return Variant.ofB(totalCities);
    }

    private int calculateTotalRequiredResources(final List<PowerPlant> powerplants) {
        int total = 0;
        for (PowerPlant plant : powerplants) {
            total += plant.resourceAmount();
        }
        return total;
    }

    private int calculateTotalCities(final List<PowerPlant> powerplants) {
        int total = 0;
        for (PowerPlant plant : powerplants) {
            total += plant.numberOfCities();
        }
        return total;
    }

    private boolean matchesRequirements(final List<Resource> resources, final List<PowerPlant> powerplants) {
        int coal = Collections.frequency(resources, Resource.COAL);
        int oil = Collections.frequency(resources, Resource.OIL);
        int garbage = Collections.frequency(resources, Resource.GARBAGE);
        int uranium = Collections.frequency(resources, Resource.URANIUM);

        return checkExclusiveRequirement(Resource.GARBAGE, garbage, powerplants)
                && checkExclusiveRequirement(Resource.URANIUM, uranium, powerplants)
                && checkCoalAndOilRequirements(coal, oil, powerplants);
    }

    private boolean checkExclusiveRequirement(final Resource resource, final int count, final List<PowerPlant> powerplants) {
        int needed = 0;
        for (PowerPlant plant : powerplants) {
            if (plant.acceptsResource(resource)) {
                needed += plant.resourceAmount();
            }
        }
        return count == needed;
    }

    private boolean checkCoalAndOilRequirements(final int coalCount, final int oilCount, final List<PowerPlant> powerplants) {
        int coalOnlyNeeded = 0;
        int oilOnlyNeeded = 0;

        for (PowerPlant plant : powerplants) {
            List<Resource> accepted = plant.acceptedResources();
            if (accepted.contains(Resource.COAL) && !accepted.contains(Resource.OIL)) {
                coalOnlyNeeded += plant.resourceAmount();
            } else if (accepted.contains(Resource.OIL) && !accepted.contains(Resource.COAL)) {
                oilOnlyNeeded += plant.resourceAmount();
            }
        }

        return coalCount >= coalOnlyNeeded && oilCount >= oilOnlyNeeded;
    }
}
