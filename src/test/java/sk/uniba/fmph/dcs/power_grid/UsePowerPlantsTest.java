package sk.uniba.fmph.dcs.power_grid;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for UsePowerPlantsClass.
 */
public class UsePowerPlantsTest {

    private UsePowerPlants usePowerPlants;

    @Before
    public void setUp() {
        usePowerPlants = new UsePowerPlantsClass();
    }

    private PowerPlant createPlant(final int number, final List<Resource> accepted, final int cost, final int cities) {
        return new PowerPlant(new PowerPlantNumber(number), accepted, cost, cities);
    }

    @Test
    public void testEmptyPlantsAndEmptyResourcesReturnsZeroCities() {
        Variant<RequestError, Integer> result = usePowerPlants.numberOfSuppliableTowns(List.of(), List.of());
        assertTrue(result.isB());
        assertEquals(Integer.valueOf(0), result.getB());
    }

    @Test
    public void testEmptyPlantsWithResourcesReturnsError() {
        Variant<RequestError, Integer> result = usePowerPlants.numberOfSuppliableTowns(List.of(Resource.COAL), List.of());
        assertTrue(result.isA());
        assertNotNull(result.getA());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullResourcesThrowsException() {
        usePowerPlants.numberOfSuppliableTowns(null, List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlantsThrowsException() {
        usePowerPlants.numberOfSuppliableTowns(List.of(), null);
    }

    @Test
    public void testEcologicalPlant() {
        PowerPlant ecoPlant = createPlant(13, List.of(), 0, 1);

        // Supplying 0 resources to eco plant powers 1 city
        Variant<RequestError, Integer> success = usePowerPlants.numberOfSuppliableTowns(List.of(), List.of(ecoPlant));
        assertTrue(success.isB());
        assertEquals(Integer.valueOf(1), success.getB());

        // Providing excess resources to eco plant fails
        Variant<RequestError, Integer> failure = usePowerPlants.numberOfSuppliableTowns(List.of(Resource.OIL), List.of(ecoPlant));
        assertTrue(failure.isA());
    }

    @Test
    public void testSingleCoalPlant() {
        // Needs 2 Coal, powers 2 cities
        PowerPlant coalPlant = createPlant(4, List.of(Resource.COAL), 2, 2);

        // Success: exactly 2 coal
        Variant<RequestError, Integer> success = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL),
                List.of(coalPlant)
        );
        assertTrue(success.isB());
        assertEquals(Integer.valueOf(2), success.getB());

        // Not enough resources (1 coal)
        Variant<RequestError, Integer> notEnough = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL),
                List.of(coalPlant)
        );
        assertTrue(notEnough.isA());

        // Too many resources (3 coal)
        Variant<RequestError, Integer> tooMany = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL),
                List.of(coalPlant)
        );
        assertTrue(tooMany.isA());

        // Wrong resource type (2 oil)
        Variant<RequestError, Integer> wrongType = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.OIL, Resource.OIL),
                List.of(coalPlant)
        );
        assertTrue(wrongType.isA());
    }

    @Test
    public void testHybridPlant() {
        // Hybrid plant: accepts COAL or OIL, needs 2, powers 3 cities
        PowerPlant hybridPlant = createPlant(5, List.of(Resource.COAL, Resource.OIL), 2, 3);

        // 2 Coal
        Variant<RequestError, Integer> res1 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL),
                List.of(hybridPlant)
        );
        assertTrue(res1.isB());
        assertEquals(Integer.valueOf(3), res1.getB());

        // 2 Oil
        Variant<RequestError, Integer> res2 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.OIL, Resource.OIL),
                List.of(hybridPlant)
        );
        assertTrue(res2.isB());
        assertEquals(Integer.valueOf(3), res2.getB());

        // 1 Coal + 1 Oil
        Variant<RequestError, Integer> res3 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.OIL),
                List.of(hybridPlant)
        );
        assertTrue(res3.isB());
        assertEquals(Integer.valueOf(3), res3.getB());

        // 1 Coal + 1 Garbage -> Fails
        Variant<RequestError, Integer> res4 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.GARBAGE),
                List.of(hybridPlant)
        );
        assertTrue(res4.isA());
    }

    @Test
    public void testMultiplePlants() {
        // Plant 1: Coal (needs 2 Coal, powers 2 cities)
        PowerPlant coalPlant = createPlant(4, List.of(Resource.COAL), 2, 2);
        // Plant 2: Hybrid (needs 1 Coal/Oil, powers 1 city)
        PowerPlant hybridPlant = createPlant(5, List.of(Resource.COAL, Resource.OIL), 1, 1);

        List<PowerPlant> plants = List.of(coalPlant, hybridPlant);

        // 2 Coal + 1 Oil -> Powers 2 + 1 = 3 cities
        Variant<RequestError, Integer> res1 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL, Resource.OIL),
                plants
        );
        assertTrue(res1.isB());
        assertEquals(Integer.valueOf(3), res1.getB());

        // 3 Coal + 0 Oil -> Powers 3 cities
        Variant<RequestError, Integer> res2 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL),
                plants
        );
        assertTrue(res2.isB());
        assertEquals(Integer.valueOf(3), res2.getB());

        // 1 Coal + 2 Oil -> Cannot power coal plant (needs 2 coal)
        Variant<RequestError, Integer> res3 = usePowerPlants.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.OIL, Resource.OIL),
                plants
        );
        assertTrue(res3.isA());
    }
}
