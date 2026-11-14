package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PlayerResourcesClass implements PlayerResources {

    private final Map<Resource, Integer> resourceCounts = new EnumMap<>(Resource.class);
    private final PlayerPowerPlants playerPowerPlants;
    private final ResourceMarket resourceMarket;
    private final ResourcePool resourcePool;

    public PlayerResourcesClass(
            final PlayerPowerPlants playerPowerPlants,
            final ResourceMarket resourceMarket,
            final ResourcePool resourcePool) {
        this.playerPowerPlants = Objects.requireNonNull(playerPowerPlants, "PlayerPowerPlants cannot be null");
        this.resourceMarket = Objects.requireNonNull(resourceMarket, "ResourceMarket cannot be null");
        this.resourcePool = Objects.requireNonNull(resourcePool, "ResourcePool cannot be null");

        for (Resource resourceType : Resource.values()) {
            this.resourceCounts.put(resourceType, 0);
        }
    }

    public List<Resource> getResources() {
        return Collections.unmodifiableList(toResourceList(this.resourceCounts));
    }

    @Override
    public Optional<RequestError> buyResources(final List<Resource> resourcesToBuy) {
        Objects.requireNonNull(resourcesToBuy, "Resources list cannot be null");
        if (resourcesToBuy.isEmpty()) {
            return Optional.empty();
        }

        Map<Resource, Integer> toBuyCounts = countOccurrences(resourcesToBuy);
        Map<Resource, Integer> projectedCounts = new EnumMap<>(this.resourceCounts);
        for (Map.Entry<Resource, Integer> entry : toBuyCounts.entrySet()) {
            projectedCounts.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        List<Resource> projectedResourcesList = toResourceList(projectedCounts);
        if (!playerPowerPlants.canStoreResources(projectedResourcesList)) {
            return Optional.of(new RequestError("Player power plants cannot store the requested resources."));
        }

        resourceMarket.takeResources(resourcesToBuy);
        for (Map.Entry<Resource, Integer> entry : toBuyCounts.entrySet()) {
            this.resourceCounts.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        return Optional.empty();
    }

    @Override
    public Variant<Integer, RequestError> usePowerPlant(
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        Objects.requireNonNull(powerPlantNumbers, "Power plant numbers cannot be null");
        Objects.requireNonNull(resourcesToBurn, "Resources to burn cannot be null");

        Map<Resource, Integer> burnCounts = countOccurrences(resourcesToBurn);
        if (!hasSufficientResources(burnCounts)) {
            return Variant.ofB(new RequestError("Player does not possess the resources required to burn."));
        }

        Variant<Integer, RequestError> productionResult =
                playerPowerPlants.usePowerPlant(powerPlantNumbers, resourcesToBurn);
        if (productionResult.isB()) {
            return productionResult;
        }

        deductResources(burnCounts);
        resourcePool.putResources(resourcesToBurn);
        return productionResult;
    }

    @Override
    public Optional<RequestError> removePowerPlant(
            final PowerPlantNumber powerPlantNumber,
            final List<Resource> resourcesToRemove) {
        Objects.requireNonNull(powerPlantNumber, "Power plant number cannot be null");
        Objects.requireNonNull(resourcesToRemove, "Resources to remove list cannot be null");

        Map<Resource, Integer> removalCounts = countOccurrences(resourcesToRemove);
        if (!hasSufficientResources(removalCounts)) {
            return Optional.of(new RequestError("Player does not possess the resources specified for removal."));
        }

        Map<Resource, Integer> remainingCounts = new EnumMap<>(this.resourceCounts);
        for (Map.Entry<Resource, Integer> entry : removalCounts.entrySet()) {
            remainingCounts.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }

        List<Resource> remainingResourcesList = toResourceList(remainingCounts);
        Optional<RequestError> removalError =
                playerPowerPlants.removePowerPlant(powerPlantNumber, remainingResourcesList);
        if (removalError.isPresent()) {
            return removalError;
        }

        deductResources(removalCounts);
        resourcePool.putResources(resourcesToRemove);
        return Optional.empty();
    }

    @Override
    public int numberOfConnectableCities() {
        return playerPowerPlants.numberOfConnectableCities(getResources());
    }

    private boolean hasSufficientResources(final Map<Resource, Integer> requiredResourceCounts) {
        for (Map.Entry<Resource, Integer> entry : requiredResourceCounts.entrySet()) {
            Resource resourceType = entry.getKey();
            int requiredQuantity = entry.getValue();
            int availableQuantity = this.resourceCounts.getOrDefault(resourceType, 0);
            if (availableQuantity < requiredQuantity) {
                return false;
            }
        }
        return true;
    }

    private void deductResources(final Map<Resource, Integer> resourcesToDeduct) {
        for (Map.Entry<Resource, Integer> entry : resourcesToDeduct.entrySet()) {
            Resource resourceType = entry.getKey();
            int quantityToDeduct = entry.getValue();
            this.resourceCounts.merge(resourceType, -quantityToDeduct, Integer::sum);
            assert this.resourceCounts.get(resourceType) >= 0;
        }
    }

    private static Map<Resource, Integer> countOccurrences(final List<Resource> resourceList) {
        Map<Resource, Integer> countsMap = new EnumMap<>(Resource.class);
        for (Resource resourceType : resourceList) {
            countsMap.merge(resourceType, 1, Integer::sum);
        }
        return countsMap;
    }

    private static List<Resource> toResourceList(final Map<Resource, Integer> countsMap) {
        List<Resource> resourceList = new ArrayList<>();
        for (Resource resourceType : Resource.values()) {
            int quantity = countsMap.getOrDefault(resourceType, 0);
            for (int index = 0; index < quantity; index++) {
                resourceList.add(resourceType);
            }
        }
        return resourceList;
    }
}
