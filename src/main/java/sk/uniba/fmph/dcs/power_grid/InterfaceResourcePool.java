package sk.uniba.fmph.dcs.power_grid;

import java.util.List;

public interface InterfaceResourcePool extends InterfaceResourcePoolPlayerResources {

    void putResources(List<Resource> resources);
}
