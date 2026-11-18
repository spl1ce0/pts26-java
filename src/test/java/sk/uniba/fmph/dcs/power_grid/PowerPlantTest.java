package sk.uniba.fmph.dcs.power_grid;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for PowerPlant record.
 */
public class PowerPlantTest {

    @Test
    public void testPowerPlantCreationAndGetters() {
        PowerPlantNumber number = new PowerPlantNumber(5);
        List<Resource> accepted = List.of(Resource.COAL, Resource.OIL);
        PowerPlant plant = new PowerPlant(number, accepted, 2, 3);

        assertEquals(number, plant.number());
        assertEquals(accepted, plant.acceptedResources());
        assertEquals(accepted, plant.getAcceptedResources());
        assertEquals(2, plant.resourceAmount());
        assertEquals(3, plant.numberOfCities());
        assertEquals(4, plant.maxStorageCapacity());
        assertTrue(plant.acceptsResource(Resource.COAL));
        assertTrue(plant.acceptsResource(Resource.OIL));
        assertFalse(plant.acceptsResource(Resource.GARBAGE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNumberThrowsException() {
        new PowerPlant(null, List.of(Resource.COAL), 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAcceptedResourcesThrowsException() {
        new PowerPlant(new PowerPlantNumber(3), null, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeResourceAmountThrowsException() {
        new PowerPlant(new PowerPlantNumber(3), List.of(), -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCitiesThrowsException() {
        new PowerPlant(new PowerPlantNumber(3), List.of(), 1, -1);
    }

    @Test
    public void testEqualsAndHashCode() {
        PowerPlant p1 = new PowerPlant(new PowerPlantNumber(4), List.of(Resource.COAL), 2, 2);
        PowerPlant p2 = new PowerPlant(new PowerPlantNumber(4), List.of(Resource.COAL), 2, 2);
        PowerPlant p3 = new PowerPlant(new PowerPlantNumber(5), List.of(Resource.COAL), 2, 2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }
}
