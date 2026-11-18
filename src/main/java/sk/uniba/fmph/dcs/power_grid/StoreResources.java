package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

/**
 * Interface for verifying resource storage validity.
 */
public interface StoreResources {

    boolean canStoreResources(List<Resource> resources, List<PowerPlant> powerplants);
}
