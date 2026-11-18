package sk.uniba.fmph.dcs.power_grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

/**
 * Tests for PowerPlant.
 */
public class PowerPlantTest {

    @Test
    public void testGettersAndToString() {
        PowerPlantNumber ppn = new PowerPlantNumber(7);
        PowerPlant plant = new PowerPlant(ppn, List.of(Resource.OIL), 3, 4);

        assertEquals(ppn, plant.getNumber());
        assertEquals(ppn, plant.number());
        assertEquals(List.of(Resource.OIL), plant.getAcceptedResources());
        assertEquals(List.of(Resource.OIL), plant.acceptedResources());
        assertEquals(3, plant.getResourceAmount());
        assertEquals(3, plant.resourceAmount());
        assertEquals(4, plant.getNumberOfCities());
        assertEquals(4, plant.numberOfCities());
        assertEquals(6, plant.maxStorageCapacity());
        assertTrue(plant.acceptsResource(Resource.OIL));
        assertFalse(plant.acceptsResource(Resource.COAL));
        assertEquals(7, plant.getNumber().getNumber());
        assertEquals(7, plant.getNumber().getValue());
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
        PowerPlant plant1 = new PowerPlant(new PowerPlantNumber(13), List.of(Resource.COAL), 2, 1);
        PowerPlant plant2 = new PowerPlant(new PowerPlantNumber(13), List.of(Resource.COAL), 2, 1);
        PowerPlant plant3 = new PowerPlant(new PowerPlantNumber(14), List.of(Resource.GARBAGE), 2, 2);

        assertEquals(plant1, plant1);
        assertEquals(plant1, plant2);
        assertNotEquals(plant1, plant3);
        assertNotEquals(plant1, null);

        assertEquals(plant1.hashCode(), plant2.hashCode());
    }
}
