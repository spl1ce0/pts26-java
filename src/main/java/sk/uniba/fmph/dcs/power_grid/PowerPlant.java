package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Power Plant card in Power Grid with fuel requirements and capacity.
 */
public final class PowerPlant {

    private final PowerPlantNumber number;
    private final List<Resource> acceptedResources;
    private final int resourceAmount;
    private final int numberOfCities;

    /**
     * Constructs a PowerPlant.
     *
     * @param number            the power plant number
     * @param acceptedResources the accepted resource types
     * @param resourceAmount    the resource cost to operate
     * @param numberOfCities    the number of cities powered
     */
    public PowerPlant(
            final PowerPlantNumber number,
            final List<Resource> acceptedResources,
            final int resourceAmount,
            final int numberOfCities) {
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

    /**
     * Returns the power plant number.
     *
     * @return the power plant number
     */
    public PowerPlantNumber number() {
        return number;
    }

    /**
     * Returns the power plant number (bean getter).
     *
     * @return the power plant number
     */
    public PowerPlantNumber getNumber() {
        return number;
    }

    /**
     * Returns the unmodifiable list of accepted resources.
     *
     * @return list of accepted resources
     */
    public List<Resource> acceptedResources() {
        return Collections.unmodifiableList(acceptedResources);
    }

    /**
     * Returns the list of accepted resources (bean getter).
     *
     * @return list of accepted resources
     */
    public List<Resource> getAcceptedResources() {
        return acceptedResources();
    }

    /**
     * Returns the required resource amount.
     *
     * @return resource amount
     */
    public int resourceAmount() {
        return resourceAmount;
    }

    /**
     * Returns the required resource amount (bean getter).
     *
     * @return resource amount
     */
    public int getResourceAmount() {
        return resourceAmount;
    }

    /**
     * Returns the number of cities powered.
     *
     * @return number of cities
     */
    public int numberOfCities() {
        return numberOfCities;
    }

    /**
     * Returns the number of cities powered (bean getter).
     *
     * @return number of cities
     */
    public int getNumberOfCities() {
        return numberOfCities;
    }

    /**
     * Returns maximum storage capacity for this power plant (2 * resourceAmount).
     *
     * @return maximum storage capacity
     */
    public int maxStorageCapacity() {
        return 2 * resourceAmount;
    }

    /**
     * Checks if this power plant accepts the given resource.
     *
     * @param resource the resource to check
     * @return true if accepted, false otherwise
     */
    public boolean acceptsResource(final Resource resource) {
        return acceptedResources.contains(resource);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof PowerPlant otherPlant)) {
            return false;
        }
        return resourceAmount == otherPlant.resourceAmount
                && numberOfCities == otherPlant.numberOfCities
                && Objects.equals(number, otherPlant.number)
                && Objects.equals(acceptedResources, otherPlant.acceptedResources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, acceptedResources, resourceAmount, numberOfCities);
    }

    @Override
    public String toString() {
        return "PowerPlant{"
                + "number=" + number
                + ", acceptedResources=" + acceptedResources
                + ", resourceAmount=" + resourceAmount
                + ", numberOfCities=" + numberOfCities
                + '}';
    }
}
