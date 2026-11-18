package sk.uniba.fmph.dcs.power_grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VariantTest {

    @Test
    public void testVariantOfA() {
        Variant<String, Integer> variant = Variant.ofA("Success");
        assertTrue(variant.isA());
        assertFalse(variant.isB());
        assertEquals("Success", variant.getA());
        assertTrue(variant.toString().contains("Success"));
    }

    @Test
    public void testVariantOfB() {
        Variant<String, Integer> variant = Variant.ofB(404);
        assertFalse(variant.isA());
        assertTrue(variant.isB());
        assertEquals(Integer.valueOf(404), variant.getB());
        assertTrue(variant.toString().contains("404"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetAWhenIsB() {
        Variant<String, Integer> variant = Variant.ofB(500);
        variant.getA();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBWhenIsA() {
        Variant<String, Integer> variant = Variant.ofA("Hello");
        variant.getB();
    }

    @Test
    public void testEqualsAndHashCode() {
        Variant<String, Integer> v1 = Variant.ofA("Test");
        Variant<String, Integer> v2 = Variant.ofA("Test");
        Variant<String, Integer> v3 = Variant.ofB(10);

        assertEquals(v1, v1);
        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
        assertNotEquals(v1, null);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testOfANull() {
        Variant.ofA(null);
    }

    @Test(expected = NullPointerException.class)
    public void testOfBNull() {
        Variant.ofB(null);
    }
}
