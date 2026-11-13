package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;

public final class PowerPlantNumber implements Comparable<PowerPlantNumber> {

    private final int number;

    public PowerPlantNumber(final int number) {
        this.number = number;
    }

    public int getValue() {
        return number;
    }

    public int number() {
        return number;
    }

    @Override
    public int compareTo(final PowerPlantNumber o) {
        return Integer.compare(this.number, o.number);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PowerPlantNumber other)) {
            return false;
        }
        return number == other.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
}
