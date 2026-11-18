package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;

/**
 * Represents the unique number / identification of a power plant.
 */
public final class PowerPlantNumber implements Comparable<PowerPlantNumber> {

    private final int number;

    public PowerPlantNumber(final int number) {
        this.number = number;
    }

    public int getValue() {
        return number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public int compareTo(final PowerPlantNumber other) {
        return Integer.compare(this.number, other.number);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PowerPlantNumber other)) {
            return false;
        }
        return this.number == other.number;
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
