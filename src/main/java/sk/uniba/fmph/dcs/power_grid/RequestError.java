package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;

public final class RequestError {

    private final String description;

    public RequestError(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String description() {
        return description;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RequestError other)) {
            return false;
        }
        return Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }

    @Override
    public String toString() {
        return "RequestError{" + "description='" + description + '\'' + '}';
    }
}
