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

final class PlayerPowerPlantsFake implements PlayerPowerPlants {
    boolean canStore = true;
    int connectableCities = 0;
    Variant<Integer, RequestError> useResult = Variant.ofA(2);
    Optional<RequestError> removeResult = Optional.empty();

    List<Resource> lastStoreCheckResources = null;
    List<PowerPlantNumber> lastPpns = null;
    List<Resource> lastBurnResources = null;
    PowerPlantNumber lastRemovePpn = null;
    List<Resource> lastRemainingResources = null;

    @Override
    public PowerPlantNumber bestPowerPlant() {
        return new PowerPlantNumber(10);
    }

    @Override
    public Optional<RequestError> removePowerPlant(final PowerPlantNumber ppn, final List<Resource> newResources) {
        this.lastRemovePpn = ppn;
        this.lastRemainingResources = new ArrayList<>(newResources);
        return removeResult;
    }

    @Override
    public int numberOfConnectableCities(final List<Resource> resources) {
        return connectableCities;
    }

    @Override
    public boolean canStoreResources(final List<Resource> resources) {
        this.lastStoreCheckResources = new ArrayList<>(resources);
        return canStore;
    }

    @Override
    public Variant<Integer, RequestError> usePowerPlant(
            final List<PowerPlantNumber> ppns,
            final List<Resource> resourcesToBurn) {
        this.lastPpns = new ArrayList<>(ppns);
        this.lastBurnResources = new ArrayList<>(resourcesToBurn);
        return useResult;
    }
}

final class ResourcePoolFakeForPlayer implements ResourcePool {
    List<Resource> putList = new ArrayList<>();

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
    public void putResources(final List<Resource> resources) {
        putList.addAll(resources);
    }
}

public class PlayerResourcesTest {

    private PlayerPowerPlantsFake powerPlants;
    private ResourceMarket resourceMarket;
    private ResourcePoolFakeForPlayer resourcePool;
    private PlayerResourcesClass playerResources;

    private static ResourceMarket createMarket() {
        Map<Integer, Map<Resource, Integer>> pattern = Map.of(
                1, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                2, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                3, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1)
        );
        List<Resource> startResources = List.of(
                Resource.COAL, Resource.COAL, Resource.COAL,
                Resource.OIL, Resource.OIL,
                Resource.GARBAGE,
                Resource.URANIUM
        );
        return new ResourceMarket(new ResourcePoolStub(), startResources, pattern);
    }

    @Before
    public void setUp() {
        powerPlants = new PlayerPowerPlantsFake();
        resourceMarket = createMarket();
        resourcePool = new ResourcePoolFakeForPlayer();
        playerResources = new PlayerResourcesClass(powerPlants, resourceMarket, resourcePool);
    }

    @Test
    public void testInitialResourcesEmpty() {
        assertTrue(playerResources.getResources().isEmpty());
    }

    @Test
    public void testBuyResourcesSuccess() {
        List<Resource> toBuy = List.of(Resource.COAL, Resource.COAL);
        Optional<RequestError> err = playerResources.buyResources(toBuy);

        assertTrue(err.isEmpty());
        assertEquals(2, playerResources.getResources().size());
        assertEquals(List.of(Resource.COAL, Resource.COAL), playerResources.getResources());
        assertEquals(toBuy, powerPlants.lastStoreCheckResources);
    }

    @Test
    public void testBuyResourcesCannotStore() {
        powerPlants.canStore = false;
        List<Resource> toBuy = List.of(Resource.URANIUM);
        Optional<RequestError> err = playerResources.buyResources(toBuy);

        assertTrue(err.isPresent());
        assertEquals("Player power plants cannot store the requested resources.", err.get().getDescription());
        assertTrue(playerResources.getResources().isEmpty());
    }

    @Test
    public void testBuyResourcesEmptyListDoesNothing() {
        Optional<RequestError> err = playerResources.buyResources(List.of());
        assertTrue(err.isEmpty());
        assertTrue(playerResources.getResources().isEmpty());
    }

    @Test
    public void testUsePowerPlantSuccess() {
        playerResources = new PlayerResourcesClass(
                powerPlants, resourceMarket, resourcePool,
                List.of(Resource.COAL, Resource.COAL, Resource.OIL));

        powerPlants.useResult = Variant.ofA(3);
        List<PowerPlantNumber> ppns = List.of(new PowerPlantNumber(5));
        List<Resource> burn = List.of(Resource.COAL, Resource.COAL);

        Variant<Integer, RequestError> result = playerResources.usePowerPlant(ppns, burn);

        assertTrue(result.isA());
        assertEquals(Integer.valueOf(3), result.getA());
        assertEquals(List.of(Resource.OIL), playerResources.getResources());
        assertEquals(burn, resourcePool.putList);
        assertEquals(ppns, powerPlants.lastPpns);
        assertEquals(burn, powerPlants.lastBurnResources);
    }

    @Test
    public void testUsePowerPlantNotEnoughResources() {
        playerResources = new PlayerResourcesClass(
                powerPlants, resourceMarket, resourcePool,
                List.of(Resource.COAL));

        Variant<Integer, RequestError> result = playerResources.usePowerPlant(
                List.of(new PowerPlantNumber(5)),
                List.of(Resource.COAL, Resource.COAL));

        assertTrue(result.isB());
        assertEquals("Player does not possess the resources required to burn.", result.getB().getDescription());
        assertEquals(1, playerResources.getResources().size());
        assertTrue(resourcePool.putList.isEmpty());
    }

    @Test
    public void testUsePowerPlantPlantsReject() {
        playerResources = new PlayerResourcesClass(
                powerPlants, resourceMarket, resourcePool,
                List.of(Resource.COAL, Resource.COAL));

        powerPlants.useResult = Variant.ofB(new RequestError("Plant does not use Coal."));

        Variant<Integer, RequestError> result = playerResources.usePowerPlant(
                List.of(new PowerPlantNumber(13)),
                List.of(Resource.COAL));

        assertTrue(result.isB());
        assertEquals("Plant does not use Coal.", result.getB().getDescription());
        assertEquals(2, playerResources.getResources().size());
        assertTrue(resourcePool.putList.isEmpty());
    }

    @Test
    public void testUsePowerPlantEcologicalZeroResources() {
        powerPlants.useResult = Variant.ofA(1);
        Variant<Integer, RequestError> result = playerResources.usePowerPlant(
                List.of(new PowerPlantNumber(13)),
                List.of());

        assertTrue(result.isA());
        assertEquals(Integer.valueOf(1), result.getA());
        assertTrue(resourcePool.putList.isEmpty());
    }

    @Test
    public void testRemovePowerPlantSuccess() {
        playerResources = new PlayerResourcesClass(
                powerPlants, resourceMarket, resourcePool,
                List.of(Resource.COAL, Resource.COAL, Resource.OIL));

        PowerPlantNumber scrap = new PowerPlantNumber(3);
        List<Resource> discard = List.of(Resource.COAL);

        Optional<RequestError> err = playerResources.removePowerPlant(scrap, discard);

        assertTrue(err.isEmpty());
        assertEquals(List.of(Resource.COAL, Resource.OIL), playerResources.getResources());
        assertEquals(discard, resourcePool.putList);
        assertEquals(scrap, powerPlants.lastRemovePpn);
        assertEquals(List.of(Resource.COAL, Resource.OIL), powerPlants.lastRemainingResources);
    }

    @Test
    public void testRemovePowerPlantNotPossessingResources() {
        playerResources = new PlayerResourcesClass(
                powerPlants, resourceMarket, resourcePool,
                List.of(Resource.COAL));

        Optional<RequestError> err = playerResources.removePowerPlant(
                new PowerPlantNumber(3),
                List.of(Resource.URANIUM));

        assertTrue(err.isPresent());
        assertEquals("Player does not possess the resources specified for removal.", err.get().getDescription());
        assertEquals(1, playerResources.getResources().size());
        assertTrue(resourcePool.putList.isEmpty());
    }

    @Test
    public void testRemovePowerPlantPlantsReject() {
        playerResources = new PlayerResourcesClass(
                powerPlants, resourceMarket, resourcePool,
                List.of(Resource.COAL, Resource.OIL));

        powerPlants.removeResult = Optional.of(new RequestError("Cannot store remaining oil."));

        Optional<RequestError> err = playerResources.removePowerPlant(
                new PowerPlantNumber(3),
                List.of(Resource.COAL));

        assertTrue(err.isPresent());
        assertEquals("Cannot store remaining oil.", err.get().getDescription());
        assertEquals(2, playerResources.getResources().size());
        assertTrue(resourcePool.putList.isEmpty());
    }

    @Test
    public void testNumberOfConnectableCitiesDelegates() {
        powerPlants.connectableCities = 4;
        assertEquals(4, playerResources.numberOfConnectableCities());
    }

    @Test(expected = NullPointerException.class)
    public void testNullPowerPlantsThrows() {
        new PlayerResourcesClass(null, resourceMarket, resourcePool);
    }

    @Test(expected = NullPointerException.class)
    public void testBuyResourcesNullThrows() {
        playerResources.buyResources(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantNullPpnsThrows() {
        playerResources.usePowerPlant(null, List.of());
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantNullResourcesThrows() {
        playerResources.usePowerPlant(List.of(), null);
    }
}
