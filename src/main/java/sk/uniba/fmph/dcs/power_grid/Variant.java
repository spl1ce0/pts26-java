package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;

public final class Variant<FirstType, SecondType> {

    private final FirstType firstValue;
    private final SecondType secondValue;

    private Variant(final FirstType firstValue, final SecondType secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public static <FirstType, SecondType> Variant<FirstType, SecondType> ofA(final FirstType firstValue) {
        Objects.requireNonNull(firstValue, "First value cannot be null");
        return new Variant<>(firstValue, null);
    }

    public static <FirstType, SecondType> Variant<FirstType, SecondType> ofB(final SecondType secondValue) {
        Objects.requireNonNull(secondValue, "Second value cannot be null");
        return new Variant<>(null, secondValue);
    }

    public boolean isA() {
        return firstValue != null;
    }

    public boolean isB() {
        return secondValue != null;
    }

    public FirstType getA() {
        if (!isA()) {
            throw new IllegalStateException("Cannot retrieve first value because this Variant contains second value");
        }
        return firstValue;
    }

    public SecondType getB() {
        if (!isB()) {
            throw new IllegalStateException("Cannot retrieve second value because this Variant contains first value");
        }
        return secondValue;
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof Variant<?, ?> otherVariant)) {
            return false;
        }
        return Objects.equals(firstValue, otherVariant.firstValue)
                && Objects.equals(secondValue, otherVariant.secondValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstValue, secondValue);
    }

    @Override
    public String toString() {
        return isA() ? "Variant[A=" + firstValue + "]" : "Variant[B=" + secondValue + "]";
    }
}
