package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.List;

public final class BureaucracyClass implements Bureaucracy {

    private List<PlayerColor> playerOrder;

    public BureaucracyClass() {
        this.playerOrder = new ArrayList<>();
    }

    @Override
    public void startPhase(final List<PlayerColor> playerOrder) {
        this.playerOrder = new ArrayList<>(playerOrder);
    }

    @Override
    public Variant<PhaseStatus, RequestError> usePowerPlants(final PlayerColor player, final List<PowerPlantNumber> ppns, final List<Resource> resourcesToBurn) {
        // TODO: Implement according to team integration
        return Variant.ofA(PhaseStatus.PHASE_CONTINUES);
    }

    @Override
    public String getState() {
        return "{}";
    }
}
