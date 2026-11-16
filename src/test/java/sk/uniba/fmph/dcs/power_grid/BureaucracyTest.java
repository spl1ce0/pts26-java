package sk.uniba.fmph.dcs.power_grid;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BureaucracyTest {

    private StubPlayerBoardFacade redPlayerBoard;
    private StubPlayerBoardFacade bluePlayerBoard;
    private StubPlayerBoardFacade greenPlayerBoard;
    private Map<PlayerColor, PlayerBoardFacade> playerBoardFacades;

    private StubCheckAndBumpStep checkAndBumpStep;
    private StubPoolForBureaucracy stubPool;
    private ResourceMarket resourceMarket;
    private StubPowerPlantMarketForBureaucracy powerPlantMarket;
    private StubTownsForBureaucracy towns;

    private BureaucracyClass bureaucracy;

    @Before
    public void setUp() {
        this.redPlayerBoard = new StubPlayerBoardFacade();
        this.bluePlayerBoard = new StubPlayerBoardFacade();
        this.greenPlayerBoard = new StubPlayerBoardFacade();

        this.playerBoardFacades = new HashMap<>();
        this.playerBoardFacades.put(PlayerColor.RED, this.redPlayerBoard);
        this.playerBoardFacades.put(PlayerColor.BLUE, this.bluePlayerBoard);
        this.playerBoardFacades.put(PlayerColor.GREEN, this.greenPlayerBoard);

        this.checkAndBumpStep = new StubCheckAndBumpStep();
        this.stubPool = new StubPoolForBureaucracy();
        Map<Integer, Map<Resource, Integer>> refillPattern = Map.of(
                1, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                2, Map.of(Resource.COAL, 2, Resource.OIL, 2, Resource.GARBAGE, 2, Resource.URANIUM, 2),
                3, Map.of(Resource.COAL, 3, Resource.OIL, 3, Resource.GARBAGE, 3, Resource.URANIUM, 3)
        );
        this.resourceMarket = new ResourceMarket(this.stubPool, List.of(), refillPattern);
        this.powerPlantMarket = new StubPowerPlantMarketForBureaucracy();
        this.towns = new StubTownsForBureaucracy();

        this.bureaucracy = new BureaucracyClass(
                this.playerBoardFacades,
                this.checkAndBumpStep,
                this.resourceMarket,
                this.powerPlantMarket,
                this.towns,
                17);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullPlayerBoards() {
        new BureaucracyClass(null, this.checkAndBumpStep, this.resourceMarket, this.powerPlantMarket, this.towns);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCheckAndBumpStep() {
        new BureaucracyClass(this.playerBoardFacades, null, this.resourceMarket, this.powerPlantMarket, this.towns);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullResourceMarket() {
        new BureaucracyClass(this.playerBoardFacades, this.checkAndBumpStep, null, this.powerPlantMarket, this.towns);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullPowerPlantMarket() {
        new BureaucracyClass(this.playerBoardFacades, this.checkAndBumpStep, this.resourceMarket, null, this.towns);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidThreshold() {
        new BureaucracyClass(
                this.playerBoardFacades,
                this.checkAndBumpStep,
                this.resourceMarket,
                this.powerPlantMarket,
                this.towns,
                0);
    }

    @Test(expected = NullPointerException.class)
    public void testStartPhaseNullOrder() {
        this.bureaucracy.startPhase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPhaseEmptyOrder() {
        this.bureaucracy.startPhase(Collections.emptyList());
    }

    @Test
    public void testInitialStateBeforePhaseStarts() {
        String stateString = this.bureaucracy.getState();
        JSONObject json = new JSONObject(stateString);
        assertEquals("Bureaucracy", json.getString("phase"));
        assertFalse(json.getBoolean("isPhaseActive"));
        assertTrue(json.isNull("currentPlayer"));
        assertEquals(0, json.getJSONArray("turnOrder").length());
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantsNullPlayer() {
        this.bureaucracy.startPhase(List.of(PlayerColor.RED));
        this.bureaucracy.usePowerPlants(null, Collections.emptyList(), Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantsNullPowerPlants() {
        this.bureaucracy.startPhase(List.of(PlayerColor.RED));
        this.bureaucracy.usePowerPlants(PlayerColor.RED, null, Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testUsePowerPlantsNullResources() {
        this.bureaucracy.startPhase(List.of(PlayerColor.RED));
        this.bureaucracy.usePowerPlants(PlayerColor.RED, Collections.emptyList(), null);
    }

    @Test
    public void testUsePowerPlantsWhenPhaseNotActive() {
        Variant<PhaseStatus, RequestError> result = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());

        assertNotNull(result);
        assertNotNull(result.getB());
        assertEquals("Bureaucracy phase is not active.", result.getB().getDescription());
    }

    @Test
    public void testUsePowerPlantsWrongTurnOrder() {
        List<PlayerColor> order = List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN);
        this.bureaucracy.startPhase(order);

        Variant<PhaseStatus, RequestError> result = this.bureaucracy.usePowerPlants(
                PlayerColor.BLUE,
                Collections.emptyList(),
                Collections.emptyList());

        assertNotNull(result);
        assertNotNull(result.getB());
        assertEquals("It is not player BLUE's turn.", result.getB().getDescription());
    }

    @Test
    public void testUsePowerPlantsMissingPlayerBoard() {
        List<PlayerColor> order = List.of(PlayerColor.YELLOW, PlayerColor.RED);
        this.bureaucracy.startPhase(order);

        Variant<PhaseStatus, RequestError> result = this.bureaucracy.usePowerPlants(
                PlayerColor.YELLOW,
                Collections.emptyList(),
                Collections.emptyList());

        assertNotNull(result);
        assertNotNull(result.getB());
        assertEquals("No player board found for player YELLOW.", result.getB().getDescription());
    }

    @Test
    public void testUsePowerPlantsPlayerBoardRejectionDoesNotAdvanceTurn() {
        List<PlayerColor> order = List.of(PlayerColor.RED, PlayerColor.BLUE);
        this.bureaucracy.startPhase(order);

        this.redPlayerBoard.setUsePowerPlantsError(new RequestError("Invalid resources supplied."));

        Variant<PhaseStatus, RequestError> failureResult = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                List.of(new PowerPlantNumber(3)),
                List.of(Resource.COAL));

        assertNotNull(failureResult);
        assertNotNull(failureResult.getB());
        assertEquals("Invalid resources supplied.", failureResult.getB().getDescription());

        JSONObject stateJson = new JSONObject(this.bureaucracy.getState());
        assertEquals(PlayerColor.RED.name(), stateJson.getString("currentPlayer"));

        this.redPlayerBoard.setUsePowerPlantsError(null);
        Variant<PhaseStatus, RequestError> successResult = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());

        assertNotNull(successResult);
        assertEquals(PhaseStatus.PHASE_CONTINUES, successResult.getA());

        JSONObject updatedState = new JSONObject(this.bureaucracy.getState());
        assertEquals(PlayerColor.BLUE.name(), updatedState.getString("currentPlayer"));
    }

    @Test
    public void testCompleteBureaucracyNormalRoundTriggersMaintenance() {
        List<PlayerColor> order = List.of(PlayerColor.RED, PlayerColor.BLUE);
        this.bureaucracy.startPhase(order);

        Variant<PhaseStatus, RequestError> firstTurn = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.PHASE_CONTINUES, firstTurn.getA());

        assertEquals(0, this.checkAndBumpStep.bumpToStep2Count);
        assertEquals(0, this.stubPool.takeResourcesCallCount);
        assertEquals(0, this.powerPlantMarket.prepareNextTurnCount);
        assertEquals(0, this.checkAndBumpStep.bumpToStep3Count);

        Variant<PhaseStatus, RequestError> secondTurn = this.bureaucracy.usePowerPlants(
                PlayerColor.BLUE,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.PHASE_ENDS, secondTurn.getA());

        assertEquals(1, this.checkAndBumpStep.bumpToStep2Count);
        // After maintenance, resourceMarket refill called pool.takeResources 4 times
        assertEquals(4, this.stubPool.takeResourcesCallCount);
        assertEquals(1, this.powerPlantMarket.prepareNextTurnCount);
        assertEquals(1, this.checkAndBumpStep.bumpToStep3Count);

        JSONObject finalState = new JSONObject(this.bureaucracy.getState());
        assertFalse(finalState.getBoolean("isPhaseActive"));
        assertTrue(finalState.isNull("currentPlayer"));
    }

    @Test
    public void testCompleteBureaucracyGameEndConditionTriggered() {
        this.towns.maxNumberOfHouses = 17;

        List<PlayerColor> order = List.of(PlayerColor.RED, PlayerColor.BLUE);
        this.bureaucracy.startPhase(order);

        Variant<PhaseStatus, RequestError> firstTurn = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.PHASE_CONTINUES, firstTurn.getA());

        Variant<PhaseStatus, RequestError> secondTurn = this.bureaucracy.usePowerPlants(
                PlayerColor.BLUE,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.GAME_ENDS, secondTurn.getA());

        // Maintenance should not run when game ends
        assertEquals(0, this.checkAndBumpStep.bumpToStep2Count);
        assertEquals(0, this.stubPool.takeResourcesCallCount);
        assertEquals(0, this.powerPlantMarket.prepareNextTurnCount);
        assertEquals(0, this.checkAndBumpStep.bumpToStep3Count);

        JSONObject finalState = new JSONObject(this.bureaucracy.getState());
        assertFalse(finalState.getBoolean("isPhaseActive"));
        assertTrue(finalState.isNull("currentPlayer"));
    }

    @Test
    public void testConstructorWithoutTownsAlwaysReturnsPhaseEnds() {
        BureaucracyClass bureaucracyWithoutTowns = new BureaucracyClass(
                this.playerBoardFacades,
                this.checkAndBumpStep,
                this.resourceMarket,
                this.powerPlantMarket);

        List<PlayerColor> order = List.of(PlayerColor.RED);
        bureaucracyWithoutTowns.startPhase(order);

        Variant<PhaseStatus, RequestError> turn = bureaucracyWithoutTowns.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());

        assertEquals(PhaseStatus.PHASE_ENDS, turn.getA());
        assertEquals(1, this.checkAndBumpStep.bumpToStep2Count);
        assertEquals(4, this.stubPool.takeResourcesCallCount);
        assertEquals(1, this.powerPlantMarket.prepareNextTurnCount);
        assertEquals(1, this.checkAndBumpStep.bumpToStep3Count);
    }

    @Test
    public void testUsePowerPlantsAfterPhaseEndsReturnsError() {
        this.bureaucracy.startPhase(List.of(PlayerColor.RED));
        Variant<PhaseStatus, RequestError> turn = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.PHASE_ENDS, turn.getA());

        // Calling again now that phase ended
        Variant<PhaseStatus, RequestError> extraTurn = this.bureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());
        assertNotNull(extraTurn);
        assertNotNull(extraTurn.getB());
        assertEquals("Bureaucracy phase is not active.", extraTurn.getB().getDescription());
    }

    @Test
    public void testCustomGameEndThreshold() {
        BureaucracyClass customBureaucracy = new BureaucracyClass(
                this.playerBoardFacades,
                this.checkAndBumpStep,
                this.resourceMarket,
                this.powerPlantMarket,
                this.towns,
                15);

        this.towns.maxNumberOfHouses = 14;
        customBureaucracy.startPhase(List.of(PlayerColor.RED));
        Variant<PhaseStatus, RequestError> notYetEnd = customBureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.PHASE_ENDS, notYetEnd.getA());

        this.towns.maxNumberOfHouses = 15;
        customBureaucracy.startPhase(List.of(PlayerColor.RED));
        Variant<PhaseStatus, RequestError> reachesEnd = customBureaucracy.usePowerPlants(
                PlayerColor.RED,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(PhaseStatus.GAME_ENDS, reachesEnd.getA());
    }
}

final class StubPlayerBoardFacade implements PlayerBoardFacade {
    private RequestError usePowerPlantsError = null;

    void setUsePowerPlantsError(final RequestError error) {
        this.usePowerPlantsError = error;
    }

    @Override
    public boolean hasEnoughMoney(final int amount) {
        return true;
    }

    @Override
    public void spendMoney(final int amount) {
    }

    @Override
    public Optional<RequestError> buyResources(final List<Resource> resourcesToBuy) {
        return Optional.empty();
    }

    @Override
    public Optional<RequestError> usePowerPlants(
            final PlayerColor playerColor,
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        if (usePowerPlantsError != null) {
            return Optional.of(usePowerPlantsError);
        }
        return Optional.empty();
    }

    @Override
    public int outstandingMoney() {
        return 50;
    }
}

final class StubCheckAndBumpStep implements CheckAndBumpStep {
    int bumpToStep2Count = 0;
    int bumpToStep3Count = 0;

    @Override
    public int getStep() {
        return 1;
    }

    @Override
    public void checkAndBumpToStep2() {
        bumpToStep2Count++;
    }

    @Override
    public void checkAndBumpToStep3() {
        bumpToStep3Count++;
    }
}

final class StubPoolForBureaucracy implements InterfaceResourcePoolResourceMarket {
    int takeResourcesCallCount = 0;

    @Override
    public boolean canTakeResourcesFromStorageSpace(final List<Resource> resources) {
        return false;
    }

    @Override
    public void getFromStorageSpace(final List<Resource> resources) {
    }

    @Override
    public List<Resource> takeResources(final List<Resource> resources) {
        takeResourcesCallCount++;
        return new ArrayList<>(resources);
    }

    @Override
    public String getStateString() {
        return "{}";
    }
}

final class StubPowerPlantMarketForBureaucracy implements PowerPlantMarket {
    int prepareNextTurnCount = 0;

    @Override
    public boolean isStep3Start() {
        return false;
    }

    @Override
    public void startStep3() {
    }

    @Override
    public void prepareNextTurn() {
        prepareNextTurnCount++;
    }
}

final class StubTownsForBureaucracy implements Towns {
    int maxNumberOfHouses = 0;

    @Override
    public int getMaxNumberOfHouses() {
        return maxNumberOfHouses;
    }

    @Override
    public void startStep2() {
    }

    @Override
    public void startStep3() {
    }
}
