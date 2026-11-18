package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a power plant card with fuel requirements and capacity.
 */
public record PowerPlant(PowerPlantNumber number, List<Resource> acceptedResources, int resourceAmount,
                         int numberOfCities) {

    public PowerPlant(final PowerPlantNumber number, final List<Resource> acceptedResources,
                      final int resourceAmount, final int numberOfCities) {
        if (number == null || acceptedResources == null) {
            throw new IllegalArgumentException("Number and acceptedResources cannot be null.");
        }
        if (resourceAmount < 0 || numberOfCities < 0) {
            throw new IllegalArgumentException("Numerical values cannot be negative.");
        }
        this.number = number;
        this.acceptedResources = List.copyOf(acceptedResources);
        this.resourceAmount = resourceAmount;
        this.numberOfCities = numberOfCities;
    }

    @Override
    public List<Resource> acceptedResources() {
        return Collections.unmodifiableList(acceptedResources);
    }

    public List<Resource> getAcceptedResources() {
        return acceptedResources();
    }

    public int maxStorageCapacity() {
        return 2 * resourceAmount;
    }

    public boolean acceptsResource(final Resource resource) {
        return acceptedResources.contains(resource);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PowerPlant other)) {
            return false;
        }
        return resourceAmount == other.resourceAmount
                && numberOfCities == other.numberOfCities
                && Objects.equals(number, other.number)
                && Objects.equals(acceptedResources, other.acceptedResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, acceptedResources, resourceAmount, numberOfCities);
    }

    @Override
    public String toString() {
        return "PowerPlant{" + number + ", " + acceptedResources + ", needs " + resourceAmount + ", powers " + numberOfCities + "}";
    }
}
