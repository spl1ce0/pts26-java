package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;
import java.util.Optional;

public final class Variant<A, B> {

    private final A a;
    private final B b;

    private Variant(final A a, final B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Variant<A, B> ofA(final A a) {
        Objects.requireNonNull(a, "Value for A cannot be null");
        return new Variant<>(a, null);
    }

    public static <A, B> Variant<A, B> ofB(final B b) {
        Objects.requireNonNull(b, "Value for B cannot be null");
        return new Variant<>(null, b);
    }

    public boolean isA() {
        return a != null;
    }

    public boolean isB() {
        return b != null;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public Optional<A> optA() {
        return Optional.ofNullable(a);
    }

    public Optional<B> optB() {
        return Optional.ofNullable(b);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Variant<?, ?> other)) {
            return false;
        }
        return Objects.equals(a, other.a) && Objects.equals(b, other.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return isA() ? "Variant[A=" + a + "]" : "Variant[B=" + b + "]";
    }
}
