package sk.uniba.fmph.dcs.power_grid;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for MaximumNumberOfUsablePowerPlantsClass.
 */
public class MaximumNumberOfUsablePowerPlantsTest {

    private MaximumNumberOfUsablePowerPlants maxUsable;

    @Before
    public void setUp() {
        maxUsable = new MaximumNumberOfUsablePowerPlantsClass();
    }

    private PowerPlant createPlant(final int number, final List<Resource> accepted, final int cost, final int cities) {
        return new PowerPlant(new PowerPlantNumber(number), accepted, cost, cities);
    }

    @Test
    public void testEmptyPowerPlantsReturnsZero() {
        assertEquals(0, maxUsable.numberOfSuppliableTowns(List.of(Resource.COAL), List.of()));
        assertEquals(0, maxUsable.numberOfSuppliableTowns(List.of(), List.of()));
    }

    @Test
    public void testEmptyResourcesWithEcologicalPlant() {
        PowerPlant coalPlant = createPlant(4, List.of(Resource.COAL), 2, 2);
        PowerPlant ecoPlant1 = createPlant(13, List.of(), 0, 1);
        PowerPlant ecoPlant2 = createPlant(22, List.of(), 0, 2);

        // With 0 resources, only the ecological plants can run
        int result = maxUsable.numberOfSuppliableTowns(
                List.of(),
                List.of(coalPlant, ecoPlant1, ecoPlant2)
        );
        assertEquals(3, result); // 1 + 2 = 3
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullResourcesThrowsException() {
        maxUsable.numberOfSuppliableTowns(null, List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlantsThrowsException() {
        maxUsable.numberOfSuppliableTowns(List.of(), null);
    }

    @Test
    public void testOptimalSelectionBetweenCompetingPlants() {
        // Plant A: needs 2 Coal, powers 2 cities
        PowerPlant plantA = createPlant(4, List.of(Resource.COAL), 2, 2);
        // Plant B: needs 2 Coal, powers 3 cities
        PowerPlant plantB = createPlant(8, List.of(Resource.COAL), 2, 3);

        // We only have 2 Coal: we can only power one plant.
        // It must choose Plant B to maximize towns (3 cities instead of 2).
        int result = maxUsable.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL),
                List.of(plantA, plantB)
        );
        assertEquals(3, result);
    }

    @Test
    public void testAllPlantsCanBePowered() {
        PowerPlant coalPlant = createPlant(4, List.of(Resource.COAL), 2, 2);
        PowerPlant oilPlant = createPlant(7, List.of(Resource.OIL), 1, 2);

        int result = maxUsable.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL, Resource.OIL),
                List.of(coalPlant, oilPlant)
        );
        assertEquals(4, result);
    }

    @Test
    public void testHybridResourceOptimalAllocation() {
        // Plant 1: Pure Coal (needs 2 Coal, powers 3 cities)
        PowerPlant coalPlant = createPlant(10, List.of(Resource.COAL), 2, 3);
        // Plant 2: Hybrid (needs 2 Coal/Oil, powers 3 cities)
        PowerPlant hybridPlant = createPlant(15, List.of(Resource.COAL, Resource.OIL), 2, 3);
        // Plant 3: Pure Oil (needs 2 Oil, powers 2 cities)
        PowerPlant oilPlant = createPlant(16, List.of(Resource.OIL), 2, 2);

        // Available: 3 Coal, 1 Oil
        // Subsets analysis:
        // - {Plant 1, Plant 2}: needs 4 resources (Plant 1 needs 2 Coal; Hybrid takes remaining 1 Coal + 1 Oil).
        //   Both can be powered! Total towns = 3 + 3 = 6.
        int result = maxUsable.numberOfSuppliableTowns(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.OIL),
                List.of(coalPlant, hybridPlant, oilPlant)
        );
        assertEquals(6, result);
    }

    @Test
    public void testComplexFourPlantsScenario() {
        PowerPlant ecoPlant = createPlant(13, List.of(), 0, 1);
        PowerPlant garbagePlant = createPlant(14, List.of(Resource.GARBAGE), 1, 2);
        PowerPlant uraniumPlant = createPlant(17, List.of(Resource.URANIUM), 1, 3);
        PowerPlant coalPlant = createPlant(20, List.of(Resource.COAL), 2, 4);

        // Available: 1 Uranium, 1 Garbage (Coal plant cannot run because no coal)
        // Can power: ecoPlant (1) + garbagePlant (2) + uraniumPlant (3) = 6 cities
        int result = maxUsable.numberOfSuppliableTowns(
                List.of(Resource.URANIUM, Resource.GARBAGE),
                List.of(ecoPlant, garbagePlant, uraniumPlant, coalPlant)
        );
        assertEquals(6, result);
    }

    @Test
    public void testBothSpellingMethods() {
        PowerPlant ecoPlant = createPlant(13, List.of(), 0, 2);
        int r1 = maxUsable.numberOfSuppliebleTowns(List.of(), List.of(ecoPlant));
        int r2 = maxUsable.numberOfSuppliableTowns(List.of(), List.of(ecoPlant));
        assertEquals(2, r1);
        assertEquals(2, r2);
    }
}
