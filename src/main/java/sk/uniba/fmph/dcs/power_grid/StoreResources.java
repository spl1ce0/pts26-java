package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface StoreResources {
    boolean canStoreResources(List<Resource> resources, List<PowerPlant> powerplants);
}
