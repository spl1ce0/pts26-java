package sk.uniba.fmph.dcs.power_grid;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for PowerPlantNumber.
 */
public class PowerPlantNumberTest {

    @Test
    public void testGettersAndValue() {
        PowerPlantNumber ppn = new PowerPlantNumber(15);
        assertEquals(15, ppn.getNumber());
        assertEquals(15, ppn.getValue());
        assertEquals("15", ppn.toString());
    }

    @Test
    public void testComparable() {
        PowerPlantNumber p3 = new PowerPlantNumber(3);
        PowerPlantNumber p5 = new PowerPlantNumber(5);
        PowerPlantNumber p3Again = new PowerPlantNumber(3);

        assertTrue(p3.compareTo(p5) < 0);
        assertTrue(p5.compareTo(p3) > 0);
        assertEquals(0, p3.compareTo(p3Again));
    }

    @Test
    public void testEqualsAndHashCode() {
        PowerPlantNumber p1 = new PowerPlantNumber(7);
        PowerPlantNumber p2 = new PowerPlantNumber(7);
        PowerPlantNumber p3 = new PowerPlantNumber(8);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }
}
