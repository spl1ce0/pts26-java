package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;

/**
 * Dataclass holding the serialized JSON representation of the entire game state.
 */
public final class SerializedGameState {

    private final String state;

    public SerializedGameState(final String state) {
        this.state = Objects.requireNonNull(state, "State cannot be null");
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof SerializedGameState otherState)) {
            return false;
        }
        return Objects.equals(state, otherState.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }

    @Override
    public String toString() {
        return state;
    }
}
