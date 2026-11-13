package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class PlayerResourcesClass implements PlayerResources {

    private final List<Resource> resources;

    public PlayerResourcesClass() {
        this.resources = new ArrayList<>();
    }

    public PlayerResourcesClass(final List<Resource> startingResources) {
        this.resources = new ArrayList<>(startingResources);
    }

    public List<Resource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    @Override
    public Optional<RequestError> removePowerPlant(final PowerPlantNumber ppn, final List<Resource> removedResources) {
        // TODO: Implement according to team integration
        return Optional.empty();
    }

    @Override
    public int numberOfConnectableCities() {
        // TODO: Implement according to team integration
        return 0;
    }

    @Override
    public Optional<RequestError> buyResources(final List<Resource> resources) {
        // TODO: Implement according to team integration
        return Optional.empty();
    }

    @Override
    public Variant<Integer, RequestError> usePowerPlant(final List<PowerPlantNumber> ppns, final List<Resource> resourcesToBurn) {
        // TODO: Implement according to team integration
        return Variant.ofA(0);
    }
}
