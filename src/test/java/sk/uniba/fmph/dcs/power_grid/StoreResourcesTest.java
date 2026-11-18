package sk.uniba.fmph.dcs.power_grid;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for StoreResourcesClass.
 */
public class StoreResourcesTest {

    private StoreResources storeResources;

    @Before
    public void setUp() {
        storeResources = new StoreResourcesClass();
    }

    private PowerPlant createPlant(final int number, final List<Resource> accepted, final int cost, final int cities) {
        return new PowerPlant(new PowerPlantNumber(number), accepted, cost, cities);
    }

    @Test
    public void testEmptyResourcesCanAlwaysBeStored() {
        PowerPlant coalPlant = createPlant(3, List.of(Resource.COAL), 2, 1);
        assertTrue(storeResources.canStoreResources(List.of(), List.of(coalPlant)));
        assertTrue(storeResources.canStoreResources(List.of(), List.of()));
    }

    @Test
    public void testNonEmptyResourcesWithNoPlantsReturnsFalse() {
        assertFalse(storeResources.canStoreResources(List.of(Resource.COAL), List.of()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullResourcesThrowsException() {
        storeResources.canStoreResources(null, List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlantsThrowsException() {
        storeResources.canStoreResources(List.of(), null);
    }

    @Test
    public void testSingleCoalPlantStorage() {
        // Needs 2 Coal -> Storage capacity = 4 Coal
        PowerPlant coalPlant = createPlant(4, List.of(Resource.COAL), 2, 1);

        assertTrue(storeResources.canStoreResources(List.of(Resource.COAL), List.of(coalPlant)));
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.COAL),
                List.of(coalPlant)
        ));

        // 5 Coal exceeds capacity of 4
        assertFalse(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.COAL, Resource.COAL),
                List.of(coalPlant)
        ));

        // Wrong resource type
        assertFalse(storeResources.canStoreResources(List.of(Resource.OIL), List.of(coalPlant)));
    }

    @Test
    public void testEcologicalPlantStorage() {
        // Ecological plant: 0 resources needed -> 0 storage capacity
        PowerPlant ecoPlant = createPlant(13, List.of(), 0, 1);

        assertTrue(storeResources.canStoreResources(List.of(), List.of(ecoPlant)));
        assertFalse(storeResources.canStoreResources(List.of(Resource.URANIUM), List.of(ecoPlant)));
    }

    @Test
    public void testHybridPlantStorage() {
        // Hybrid plant: accepts COAL and OIL, needs 2 -> Storage capacity = 4
        PowerPlant hybridPlant = createPlant(5, List.of(Resource.COAL, Resource.OIL), 2, 1);

        // 4 Coal
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.COAL),
                List.of(hybridPlant)
        ));

        // 4 Oil
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.OIL, Resource.OIL, Resource.OIL, Resource.OIL),
                List.of(hybridPlant)
        ));

        // 2 Coal + 2 Oil = 4
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.OIL, Resource.OIL),
                List.of(hybridPlant)
        ));

        // 3 Coal + 1 Oil = 4
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.OIL),
                List.of(hybridPlant)
        ));

        // 3 Coal + 2 Oil = 5 -> Exceeds capacity
        assertFalse(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.OIL, Resource.OIL),
                List.of(hybridPlant)
        ));

        // Non-accepted resource
        assertFalse(storeResources.canStoreResources(
                List.of(Resource.GARBAGE),
                List.of(hybridPlant)
        ));
    }

    @Test
    public void testMultiplePlantsWithHybridBalancing() {
        // Plant 1: Pure Coal (needs 1 -> storage = 2)
        PowerPlant coalPlant = createPlant(3, List.of(Resource.COAL), 1, 1);
        // Plant 2: Hybrid (needs 1 -> storage = 2)
        PowerPlant hybridPlant = createPlant(5, List.of(Resource.COAL, Resource.OIL), 1, 1);

        List<PowerPlant> plants = List.of(coalPlant, hybridPlant);

        // 3 Coal and 1 Oil:
        // Pure coal holds 2 Coal, Hybrid holds 1 Coal + 1 Oil -> Fits!
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.OIL),
                plants
        ));

        // 4 Coal and 0 Oil: Fits (2 in coal plant, 2 in hybrid)
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.COAL),
                plants
        ));

        // 3 Coal and 2 Oil = 5 resources -> Capacity is 2+2=4 -> Exceeds
        assertFalse(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.COAL, Resource.COAL, Resource.OIL, Resource.OIL),
                plants
        ));

        // 1 Coal and 3 Oil: Pure coal cannot hold Oil, hybrid holds at most 2 Oil -> Exceeds
        assertFalse(storeResources.canStoreResources(
                List.of(Resource.COAL, Resource.OIL, Resource.OIL, Resource.OIL),
                plants
        ));
    }

    @Test
    public void testGarbageAndUraniumStorage() {
        PowerPlant garbagePlant = createPlant(14, List.of(Resource.GARBAGE), 2, 2);
        PowerPlant uraniumPlant = createPlant(17, List.of(Resource.URANIUM), 1, 2);

        List<PowerPlant> plants = List.of(garbagePlant, uraniumPlant);

        // Garbage plant capacity = 4, Uranium plant capacity = 2
        assertTrue(storeResources.canStoreResources(
                List.of(Resource.GARBAGE, Resource.GARBAGE, Resource.URANIUM, Resource.URANIUM),
                plants
        ));

        // 3 Uranium exceeds capacity of 2
        assertFalse(storeResources.canStoreResources(
                List.of(Resource.URANIUM, Resource.URANIUM, Resource.URANIUM),
                plants
        ));
    }
}
