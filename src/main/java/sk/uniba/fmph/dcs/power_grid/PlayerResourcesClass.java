package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PlayerResourcesClass implements PlayerResources {

    private final List<Resource> resources;
    private final PlayerPowerPlants playerPowerPlants;
    private final ResourceMarket resourceMarket;
    private final ResourcePool resourcePool;

    public PlayerResourcesClass(
            final PlayerPowerPlants playerPowerPlants,
            final ResourceMarket resourceMarket,
            final ResourcePool resourcePool) {
        this(playerPowerPlants, resourceMarket, resourcePool, List.of());
    }

    public PlayerResourcesClass(
            final PlayerPowerPlants playerPowerPlants,
            final ResourceMarket resourceMarket,
            final ResourcePool resourcePool,
            final List<Resource> startingResources) {
        this.playerPowerPlants = Objects.requireNonNull(playerPowerPlants, "PlayerPowerPlants cannot be null");
        this.resourceMarket = Objects.requireNonNull(resourceMarket, "ResourceMarket cannot be null");
        this.resourcePool = Objects.requireNonNull(resourcePool, "ResourcePool cannot be null");
        Objects.requireNonNull(startingResources, "Starting resources cannot be null");
        this.resources = new ArrayList<>(startingResources);
    }

    public List<Resource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    @Override
    public Optional<RequestError> buyResources(final List<Resource> resourcesToBuy) {
        Objects.requireNonNull(resourcesToBuy, "Resources list cannot be null");
        if (resourcesToBuy.isEmpty()) {
            return Optional.empty();
        }

        List<Resource> combined = new ArrayList<>(this.resources);
        combined.addAll(resourcesToBuy);

        if (!playerPowerPlants.canStoreResources(combined)) {
            return Optional.of(new RequestError("Player power plants cannot store the requested resources."));
        }

        resourceMarket.takeResources(resourcesToBuy);
        this.resources.addAll(resourcesToBuy);
        return Optional.empty();
    }

    @Override
    public Variant<Integer, RequestError> usePowerPlant(
            final List<PowerPlantNumber> ppns,
            final List<Resource> resourcesToBurn) {
        Objects.requireNonNull(ppns, "Power plant numbers cannot be null");
        Objects.requireNonNull(resourcesToBurn, "Resources to burn cannot be null");

        if (!hasResources(this.resources, resourcesToBurn)) {
            return Variant.ofB(new RequestError("Player does not possess the resources required to burn."));
        }

        Variant<Integer, RequestError> result = playerPowerPlants.usePowerPlant(ppns, resourcesToBurn);
        if (result.isB()) {
            return result;
        }

        removeResources(this.resources, resourcesToBurn);
        resourcePool.putResources(resourcesToBurn);
        return result;
    }

    @Override
    public Optional<RequestError> removePowerPlant(
            final PowerPlantNumber ppn,
            final List<Resource> removedResources) {
        Objects.requireNonNull(ppn, "Power plant number cannot be null");
        Objects.requireNonNull(removedResources, "Removed resources list cannot be null");

        if (!hasResources(this.resources, removedResources)) {
            return Optional.of(new RequestError("Player does not possess the resources specified for removal."));
        }

        List<Resource> remainingResources = subtractResources(this.resources, removedResources);
        Optional<RequestError> error = playerPowerPlants.removePowerPlant(ppn, remainingResources);
        if (error.isPresent()) {
            return error;
        }

        removeResources(this.resources, removedResources);
        resourcePool.putResources(removedResources);
        return Optional.empty();
    }

    @Override
    public int numberOfConnectableCities() {
        return playerPowerPlants.numberOfConnectableCities(Collections.unmodifiableList(this.resources));
    }

    private static Map<Resource, Integer> count(final List<Resource> list) {
        Map<Resource, Integer> counts = new EnumMap<>(Resource.class);
        for (Resource resource : list) {
            counts.merge(resource, 1, Integer::sum);
        }
        return counts;
    }

    private static boolean hasResources(final List<Resource> available, final List<Resource> required) {
        Map<Resource, Integer> availableCounts = count(available);
        Map<Resource, Integer> requiredCounts = count(required);
        for (Map.Entry<Resource, Integer> entry : requiredCounts.entrySet()) {
            if (availableCounts.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static void removeResources(final List<Resource> source, final List<Resource> toRemove) {
        for (Resource r : toRemove) {
            source.remove(r);
        }
    }

    private static List<Resource> subtractResources(final List<Resource> source, final List<Resource> toSubtract) {
        List<Resource> copy = new ArrayList<>(source);
        for (Resource r : toSubtract) {
            copy.remove(r);
        }
        return copy;
    }
}
