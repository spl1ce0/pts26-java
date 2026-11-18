package sk.uniba.fmph.dcs.power_grid;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class Variant<A, B> {

    private final A a;
    private final B b;
    private final boolean isA;

    private Variant(final A a, final B b, final boolean isA) {
        this.a = a;
        this.b = b;
        this.isA = isA;
    }

    public static <A, B> Variant<A, B> ofA(final A a) {
        Objects.requireNonNull(a, "Value of A cannot be null.");
        return new Variant<>(a, null, true);
    }

    public static <A, B> Variant<A, B> ofB(final B b) {
        Objects.requireNonNull(b, "Value of B cannot be null.");
        return new Variant<>(null, b, false);
    }

    public boolean isA() {
        return isA;
    }

    public boolean isB() {
        return !isA;
    }

    public A getA() {
        if (!isA) {
            throw new NoSuchElementException("Variant does not contain value A.");
        }
        return a;
    }

    public B getB() {
        if (isA) {
            throw new NoSuchElementException("Variant does not contain value B.");
        }
        return b;
    }
}