package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BuyingResourcesClass implements BuyingResources {

    private List<PlayerColor> playerOrder;

    public BuyingResourcesClass() {
        this.playerOrder = new ArrayList<>();
    }

    @Override
    public void startPhase(final List<PlayerColor> playerOrder) {
        this.playerOrder = new ArrayList<>(playerOrder);
        Collections.reverse(this.playerOrder);
    }

    @Override
    public Variant<PhaseStatus, RequestError> buyResources(final PlayerColor player, final List<Resource> removedResources) {
        // TODO: Implement according to team integration
        return Variant.ofA(PhaseStatus.PHASE_CONTINUES);
    }

    @Override
    public String getState() {
        return "{}";
    }
}
