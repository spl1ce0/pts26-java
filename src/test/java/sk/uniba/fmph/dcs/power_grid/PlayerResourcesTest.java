package sk.uniba.fmph.dcs.power_grid;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

final class FakePlayerPowerPlants implements PlayerPowerPlants {
    boolean canStore = true;
    int connectableCities = 0;
    Variant<Integer, RequestError> usePowerPlantResult = Variant.ofA(2);
    Optional<RequestError> removePowerPlantResult = Optional.empty();

    List<Resource> lastCheckedStorageResources = null;
    List<PowerPlantNumber> lastUsedPowerPlantNumbers = null;
    List<Resource> lastBurnedResources = null;
    PowerPlantNumber lastRemovedPowerPlantNumber = null;
    List<Resource> lastRemainingResources = null;

    @Override
    public Optional<RequestError> removePowerPlant(
            final PowerPlantNumber powerPlantNumber,
            final List<Resource> remainingResources) {
        this.lastRemovedPowerPlantNumber = powerPlantNumber;
        this.lastRemainingResources = new ArrayList<>(remainingResources);
        return removePowerPlantResult;
    }

    @Override
    public int numberOfConnectableCities(final List<Resource> resources) {
        return connectableCities;
    }

    @Override
    public boolean canStoreResources(final List<Resource> resources) {
        this.lastCheckedStorageResources = new ArrayList<>(resources);
        return canStore;
    }

    @Override
    public Variant<Integer, RequestError> usePowerPlant(
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        this.lastUsedPowerPlantNumbers = new ArrayList<>(powerPlantNumbers);
        this.lastBurnedResources = new ArrayList<>(resourcesToBurn);
        return usePowerPlantResult;
    }
}

final class FakeResourcePool implements ResourcePool {
    List<Resource> depositedResources = new ArrayList<>();

    @Override
    public void putResources(final List<Resource> resources) {
        depositedResources.addAll(resources);
    }
}

final class StubPoolForPlayerResourcesMarket implements InterfaceResourcePoolResourceMarket {
    @Override
    public boolean canTakeResourcesFromStorageSpace(final List<Resource> resources) {
        return false;
    }

    @Override
    public void getFromStorageSpace(final List<Resource> resources) {
    }

    @Override
    public List<Resource> takeResources(final List<Resource> resources) {
        return new ArrayList<>();
    }

    @Override
    public String getStateString() {
        return "\"\"";
    }
}

public class PlayerResourcesTest {

    private FakePlayerPowerPlants fakePlayerPowerPlants;
    private ResourceMarket resourceMarket;
    private FakeResourcePool fakeResourcePool;
    private PlayerResourcesClass playerResources;

    private static ResourceMarket createResourceMarket() {
        Map<Integer, Map<Resource, Integer>> refillPattern = Map.of(
                1, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                2, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                3, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1)
        );
        List<Resource> startingMarketResources = List.of(
                Resource.COAL, Resource.COAL, Resource.COAL,
                Resource.COAL, Resource.COAL,
                Resource.OIL, Resource.OIL, Resource.OIL,
                Resource.GARBAGE, Resource.GARBAGE,
                Resource.URANIUM, Resource.URANIUM
        );
        return new ResourceMarket(new StubPoolForPlayerResourcesMarket(), startingMarketResources, refillPattern);
    }

    @Before
    public void setUp() {
        fakePlayerPowerPlants = new FakePlayerPowerPlants();
        resourceMarket = createResourceMarket();
        fakeResourcePool = new FakeResourcePool();
        playerResources = new PlayerResourcesClass(fakePlayerPowerPlants, resourceMarket, fakeResourcePool);
    }

    @Test
    public void testInitialResourcesAreEmpty() {
        assertTrue(playerResources.getResources().isEmpty());
    }

    @Test
    public void testBuyResourcesSuccessfully() {
        List<Resource> resourcesToBuy = List.of(Resource.COAL, Resource.COAL);
        Optional<RequestError> error = playerResources.buyResources(resourcesToBuy);

        assertTrue(error.isEmpty());
        assertEquals(2, playerResources.getResources().size());
        assertEquals(List.of(Resource.COAL, Resource.COAL), playerResources.getResources());
        assertEquals(resourcesToBuy, fakePlayerPowerPlants.lastCheckedStorageResources);
    }

    @Test
    public void testBuyResourcesWhenStorageCapacityIsExceeded() {
        fakePlayerPowerPlants.canStore = false;
        List<Resource> resourcesToBuy = List.of(Resource.URANIUM);
        Optional<RequestError> error = playerResources.buyResources(resourcesToBuy);

        assertTrue(error.isPresent());
        assertEquals("Player power plants cannot store the requested resources.", error.get().getDescription());
        assertTrue(playerResources.getResources().isEmpty());
    }

    @Test
    public void testBuyEmptyResourceListDoesNothing() {
        Optional<RequestError> error = playerResources.buyResources(List.of());

        assertTrue(error.isEmpty());
        assertTrue(playerResources.getResources().isEmpty());
    }

    @Test
    public void testUsePowerPlantSuccessfully() {
        playerResources.buyResources(List.of(Resource.COAL, Resource.COAL, Resource.OIL));

        fakePlayerPowerPlants.usePowerPlantResult = Variant.ofA(3);
        List<PowerPlantNumber> powerPlantsToActivate = List.of(new PowerPlantNumber(5));
        List<Resource> resourcesToBurn = List.of(Resource.COAL, Resource.COAL);

        Variant<Integer, RequestError> result =
                playerResources.usePowerPlant(powerPlantsToActivate, resourcesToBurn);

        assertTrue(result.isA());
        assertEquals(Integer.valueOf(3), result.getA());
        assertEquals(List.of(Resource.OIL), playerResources.getResources());
        assertEquals(resourcesToBurn, fakeResourcePool.depositedResources);
        assertEquals(powerPlantsToActivate, fakePlayerPowerPlants.lastUsedPowerPlantNumbers);
        assertEquals(resourcesToBurn, fakePlayerPowerPlants.lastBurnedResources);
    }

    @Test
    public void testUsePowerPlantWhenPlayerLacksRequiredResources() {
        playerResources.buyResources(List.of(Resource.COAL));

        Variant<Integer, RequestError> result = playerResources.usePowerPlant(
                List.of(new PowerPlantNumber(5)),
                List.of(Resource.COAL, Resource.COAL));

        assertTrue(result.isB());
        assertEquals("Player does not possess the resources required to burn.", result.getB().getDescription());
        assertEquals(List.of(Resource.COAL), playerResources.getResources());
        assertTrue(fakeResourcePool.depositedResources.isEmpty());
    }

    @Test
    public void testUsePowerPlantWhenPowerPlantsRejectFuel() {
        playerResources.buyResources(List.of(Resource.COAL, Resource.COAL));

        fakePlayerPowerPlants.usePowerPlantResult = Variant.ofB(new RequestError("Plant does not use Coal."));

        Variant<Integer, RequestError> result = playerResources.usePowerPlant(
                List.of(new PowerPlantNumber(13)),
                List.of(Resource.COAL));

        assertTrue(result.isB());
        assertEquals("Plant does not use Coal.", result.getB().getDescription());
        assertEquals(List.of(Resource.COAL, Resource.COAL), playerResources.getResources());
        assertTrue(fakeResourcePool.depositedResources.isEmpty());
    }

    @Test
    public void testUseEcologicalPowerPlantRequiringZeroResources() {
        fakePlayerPowerPlants.usePowerPlantResult = Variant.ofA(1);

        Variant<Integer, RequestError> result = playerResources.usePowerPlant(
                List.of(new PowerPlantNumber(13)),
                List.of());

        assertTrue(result.isA());
        assertEquals(Integer.valueOf(1), result.getA());
        assertTrue(fakeResourcePool.depositedResources.isEmpty());
    }

    @Test
    public void testRemovePowerPlantSuccessfully() {
        playerResources.buyResources(List.of(Resource.COAL, Resource.COAL, Resource.OIL));

        PowerPlantNumber powerPlantToScrap = new PowerPlantNumber(3);
        List<Resource> resourcesToRemove = List.of(Resource.COAL);

        Optional<RequestError> error =
                playerResources.removePowerPlant(powerPlantToScrap, resourcesToRemove);

        assertTrue(error.isEmpty());
        assertEquals(List.of(Resource.COAL, Resource.OIL), playerResources.getResources());
        assertEquals(resourcesToRemove, fakeResourcePool.depositedResources);
        assertEquals(powerPlantToScrap, fakePlayerPowerPlants.lastRemovedPowerPlantNumber);
        assertEquals(List.of(Resource.COAL, Resource.OIL), fakePlayerPowerPlants.lastRemainingResources);
    }

    @Test
    public void testRemovePowerPlantWhenPlayerDoesNotPossessRemovalResources() {
        playerResources.buyResources(List.of(Resource.COAL));

        Optional<RequestError> error = playerResources.removePowerPlant(
                new PowerPlantNumber(3),
                List.of(Resource.URANIUM));

        assertTrue(error.isPresent());
        assertEquals("Player does not possess the resources specified for removal.", error.get().getDescription());
        assertEquals(List.of(Resource.COAL), playerResources.getResources());
        assertTrue(fakeResourcePool.depositedResources.isEmpty());
    }

    @Test
    public void testRemovePowerPlantWhenRemainingPlantsCannotStoreRemainingResources() {
        playerResources.buyResources(List.of(Resource.COAL, Resource.OIL));

        fakePlayerPowerPlants.removePowerPlantResult =
                Optional.of(new RequestError("Cannot store remaining oil."));

        Optional<RequestError> error = playerResources.removePowerPlant(
                new PowerPlantNumber(3),
                List.of(Resource.COAL));

        assertTrue(error.isPresent());
        assertEquals("Cannot store remaining oil.", error.get().getDescription());
        assertEquals(List.of(Resource.COAL, Resource.OIL), playerResources.getResources());
        assertTrue(fakeResourcePool.depositedResources.isEmpty());
    }

    @Test
    public void testNumberOfConnectableCitiesDelegatesProperly() {
        fakePlayerPowerPlants.connectableCities = 4;
        assertEquals(4, playerResources.numberOfConnectableCities());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullPowerPlantsThrows() {
        new PlayerResourcesClass(null, resourceMarket, fakeResourcePool);
    }

    @Test(expected = NullPointerException.class)
    public void testBuyResourcesWithNullThrows() {
        playerResources.buyResources(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantWithNullPowerPlantNumbersThrows() {
        playerResources.usePowerPlant(null, List.of());
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantWithNullResourcesThrows() {
        playerResources.usePowerPlant(List.of(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemovePowerPlantWithNullPowerPlantNumberThrows() {
        playerResources.removePowerPlant(null, List.of());
    }

    @Test(expected = NullPointerException.class)
    public void testRemovePowerPlantWithNullResourcesThrows() {
        playerResources.removePowerPlant(new PowerPlantNumber(1), null);
    }
}
