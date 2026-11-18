package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaximumNumberOfUsablePowerPlantsClass implements MaximumNumberOfUsablePowerPlants {

    @Override
    public int numberOfSuppliableTowns(List<Resource> resources, List<PowerPlant> powerplants) {
        if (resources == null || powerplants == null) {
            throw new IllegalArgumentException("Lists cannot be null.");
        }
        if (powerplants.isEmpty()) {
            return 0;
        }

        int availableCoal = Collections.frequency(resources, Resource.COAL);
        int availableOil = Collections.frequency(resources, Resource.OIL);
        int availableGarbage = Collections.frequency(resources, Resource.GARBAGE);
        int availableUranium = Collections.frequency(resources, Resource.URANIUM);

        int maxCities = 0;
        List<List<PowerPlant>> allCombinations = getAllSubsets(powerplants);

        for (List<PowerPlant> subset : allCombinations) {
            int cities = calculateTotalCities(subset);
            if (cities <= maxCities) {
                continue;
            }

            if (canSupplySubset(subset, availableCoal, availableOil, availableGarbage, availableUranium)) {
                maxCities = cities;
            }
        }

        return maxCities;
    }

    private List<List<PowerPlant>> getAllSubsets(List<PowerPlant> powerplants) {
        List<List<PowerPlant>> subsets = new ArrayList<>();
        int n = powerplants.size();
        int totalCombinations = 1 << n;

        for (int mask = 0; mask < totalCombinations; mask++) {
            List<PowerPlant> subset = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(powerplants.get(i));
                }
            }
            subsets.add(subset);
        }
        return subsets;
    }

    private int calculateTotalCities(List<PowerPlant> powerplants) {
        int total = 0;
        for (PowerPlant plant : powerplants) {
            total += plant.numberOfCities();
        }
        return total;
    }

    private boolean canSupplySubset(List<PowerPlant> subset, int coal, int oil, int garbage, int uranium) {
        return checkExclusive(Resource.GARBAGE, garbage, subset)
                && checkExclusive(Resource.URANIUM, uranium, subset)
                && checkCoalAndOil(coal, oil, subset);
    }

    private boolean checkExclusive(Resource resource, int availableCount, List<PowerPlant> subset) {
        int needed = 0;
        for (PowerPlant plant : subset) {
            if (plant.acceptsResource(resource)) {
                needed += plant.resourceAmount();
            }
        }
        return availableCount >= needed;
    }

    private boolean checkCoalAndOil(int availableCoal, int availableOil, List<PowerPlant> subset) {
        int coalOnlyNeeded = 0;
        int oilOnlyNeeded = 0;
        int hybridNeeded = 0;

        for (PowerPlant plant : subset) {
            List<Resource> accepted = plant.acceptedResources();
            if (accepted.contains(Resource.COAL) && accepted.contains(Resource.OIL)) {
                hybridNeeded += plant.resourceAmount();
            } else if (accepted.contains(Resource.COAL)) {
                coalOnlyNeeded += plant.resourceAmount();
            } else if (accepted.contains(Resource.OIL)) {
                oilOnlyNeeded += plant.resourceAmount();
            }
        }
        if (availableCoal < coalOnlyNeeded) {
            return false;
        }
        if (availableOil < oilOnlyNeeded) {
            return false;
        }
        int remainingCoal = availableCoal - coalOnlyNeeded;
        int remainingOil = availableOil - oilOnlyNeeded;

        return (remainingCoal + remainingOil) >= hybridNeeded;
    }


}
