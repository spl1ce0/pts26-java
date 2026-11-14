package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface ResourcePool {

    boolean canTakeResourcesFromStorageSpace(List<Resource> resources);

    void getFromStorageSpace(List<Resource> resources);

    List<Resource> takeResources(List<Resource> resources);

    void putResources(List<Resource> resources);
}
