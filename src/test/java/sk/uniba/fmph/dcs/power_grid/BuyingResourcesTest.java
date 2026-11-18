package sk.uniba.fmph.dcs.power_grid;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

final class FakePlayerBoardFacade implements InterfacePlayerBoardFacade {
    int money = 50;
    int spentMoneyTotal = 0;
    boolean canStoreResources = true;
    List<Resource> boughtResourcesList = new ArrayList<>();
    Optional<RequestError> usePowerPlantsResult = Optional.empty();

    @Override
    public boolean hasEnoughMoney(final int amount) {
        return money >= amount;
    }

    @Override
    public void spendMoney(final int amount) {
        money -= amount;
        spentMoneyTotal += amount;
    }

    @Override
    public Optional<RequestError> buyResources(final List<Resource> resourcesToBuy) {
        if (!canStoreResources) {
            return Optional.of(new RequestError("Cannot store resources."));
        }
        boughtResourcesList.addAll(resourcesToBuy);
        return Optional.empty();
    }

    @Override
    public Optional<RequestError> usePowerPlants(
            final PlayerColor playerColor,
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        return usePowerPlantsResult;
    }

    @Override
    public int outstandingMoney() {
        return money;
    }
}

public class BuyingResourcesTest {

    private ResourceMarket resourceMarket;
    private Map<PlayerColor, InterfacePlayerBoardFacade> playerBoardFacades;
    private FakePlayerBoardFacade redBoard;
    private FakePlayerBoardFacade blueBoard;
    private FakePlayerBoardFacade yellowBoard;
    private BuyingResourcesClass buyingResources;

    private static ResourceMarket createResourceMarket() {
        Map<Integer, Map<Resource, Integer>> refillPattern = Map.of(
                1, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                2, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                3, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1)
        );
        List<Resource> startingMarketResources = List.of(
                Resource.COAL, Resource.COAL, Resource.COAL,
                Resource.OIL, Resource.OIL
        );
        return new ResourceMarket(new StubPoolForPlayerResourcesMarket(), startingMarketResources, refillPattern);
    }

    @Before
    public void setUp() {
        resourceMarket = createResourceMarket();
        playerBoardFacades = new EnumMap<>(PlayerColor.class);

        redBoard = new FakePlayerBoardFacade();
        blueBoard = new FakePlayerBoardFacade();
        yellowBoard = new FakePlayerBoardFacade();

        playerBoardFacades.put(PlayerColor.RED, redBoard);
        playerBoardFacades.put(PlayerColor.BLUE, blueBoard);
        playerBoardFacades.put(PlayerColor.YELLOW, yellowBoard);

        buyingResources = new BuyingResourcesClass(resourceMarket, playerBoardFacades);
    }

    @Test
    public void testReversePlayerOrderExecution() {
        // Normal order: RED (1st), BLUE (2nd), YELLOW (3rd)
        // Reverse order: YELLOW buys 1st, BLUE 2nd, RED 3rd
        buyingResources.startPhase(List.of(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.YELLOW));

        // Attempting to buy with RED should fail because it's YELLOW's turn
        Variant<PhaseStatus, RequestError> redAttempt =
                buyingResources.buyResources(PlayerColor.RED, List.of(Resource.COAL));
        assertTrue(redAttempt.isB());
        assertEquals("It is not player RED's turn to buy resources.", redAttempt.getB().getDescription());

        // YELLOW passes (empty list)
        Variant<PhaseStatus, RequestError> yellowPass =
                buyingResources.buyResources(PlayerColor.YELLOW, List.of());
        assertTrue(yellowPass.isA());
        assertEquals(PhaseStatus.PHASE_CONTINUES, yellowPass.getA());

        // Now it's BLUE's turn; BLUE buys 1 Coal
        Variant<PhaseStatus, RequestError> blueBuy =
                buyingResources.buyResources(PlayerColor.BLUE, List.of(Resource.COAL));
        assertTrue(blueBuy.isA());
        assertEquals(PhaseStatus.PHASE_CONTINUES, blueBuy.getA());
        assertEquals(1, blueBoard.boughtResourcesList.size());
        assertTrue(blueBoard.spentMoneyTotal > 0);

        // Finally RED's turn; RED passes -> phase should end!
        Variant<PhaseStatus, RequestError> redPass =
                buyingResources.buyResources(PlayerColor.RED, List.of());
        assertTrue(redPass.isA());
        assertEquals(PhaseStatus.PHASE_ENDS, redPass.getA());
    }

    @Test
    public void testSuccessfulPurchaseDeductsMoneyAndStoresResources() {
        buyingResources.startPhase(List.of(PlayerColor.RED, PlayerColor.YELLOW));

        // YELLOW is 1st in reverse order
        int initialMoney = yellowBoard.money;
        Variant<PhaseStatus, RequestError> result =
                buyingResources.buyResources(PlayerColor.YELLOW, List.of(Resource.COAL, Resource.COAL));

        assertTrue(result.isA());
        assertEquals(PhaseStatus.PHASE_CONTINUES, result.getA());
        assertEquals(2, yellowBoard.boughtResourcesList.size());
        assertTrue(yellowBoard.money < initialMoney);
        assertEquals(yellowBoard.spentMoneyTotal, initialMoney - yellowBoard.money);
    }

    @Test
    public void testPurchaseFailsWhenResourcesUnavailableInMarket() {
        buyingResources.startPhase(List.of(PlayerColor.RED, PlayerColor.YELLOW));

        // YELLOW tries to buy Uranium, which is not in the market
        Variant<PhaseStatus, RequestError> result =
                buyingResources.buyResources(PlayerColor.YELLOW, List.of(Resource.URANIUM));

        assertTrue(result.isB());
        assertEquals("The requested resources are not available in the market.", result.getB().getDescription());
        assertEquals(0, yellowBoard.spentMoneyTotal);
        assertTrue(yellowBoard.boughtResourcesList.isEmpty());
    }

    @Test
    public void testPurchaseFailsWhenPlayerHasInsufficientMoney() {
        buyingResources.startPhase(List.of(PlayerColor.RED, PlayerColor.YELLOW));

        yellowBoard.money = 0; // Broke player
        Variant<PhaseStatus, RequestError> result =
                buyingResources.buyResources(PlayerColor.YELLOW, List.of(Resource.COAL));

        assertTrue(result.isB());
        assertEquals("Player YELLOW does not have enough money.", result.getB().getDescription());
        assertEquals(0, yellowBoard.spentMoneyTotal);
        assertTrue(yellowBoard.boughtResourcesList.isEmpty());
    }

    @Test
    public void testPurchaseFailsWhenPlayerCannotStoreResources() {
        buyingResources.startPhase(List.of(PlayerColor.RED, PlayerColor.YELLOW));

        yellowBoard.canStoreResources = false; // Plants are full
        Variant<PhaseStatus, RequestError> result =
                buyingResources.buyResources(PlayerColor.YELLOW, List.of(Resource.COAL));

        assertTrue(result.isB());
        assertEquals("Cannot store resources.", result.getB().getDescription());
        assertEquals(0, yellowBoard.spentMoneyTotal);
        assertTrue(yellowBoard.boughtResourcesList.isEmpty());
    }

    @Test
    public void testActionFailsWhenPhaseNotActive() {
        Variant<PhaseStatus, RequestError> result =
                buyingResources.buyResources(PlayerColor.YELLOW, List.of());

        assertTrue(result.isB());
        assertEquals("Buy resources phase is not active.", result.getB().getDescription());
    }

    @Test
    public void testJsonStateOutput() {
        buyingResources.startPhase(List.of(PlayerColor.RED, PlayerColor.YELLOW));

        JSONObject state = new JSONObject(buyingResources.getState());
        assertEquals("BuyResources", state.getString("phase"));
        assertTrue(state.getBoolean("isPhaseActive"));
        assertEquals("YELLOW", state.getString("currentPlayer"));
        assertEquals("YELLOW", state.getJSONArray("turnOrder").getString(0));
        assertEquals("RED", state.getJSONArray("turnOrder").getString(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPhaseWithEmptyOrderThrows() {
        buyingResources.startPhase(List.of());
    }

    @Test(expected = NullPointerException.class)
    public void testNullResourceMarketThrows() {
        new BuyingResourcesClass(null, playerBoardFacades);
    }

    @Test(expected = NullPointerException.class)
    public void testNullPlayerBoardFacadesThrows() {
        new BuyingResourcesClass(resourceMarket, null);
    }

    @Test
    public void testConstructorWithoutResourceMarketDelegatesDirectlyToPlayerBoard() {
        BuyingResources directBuying = new BuyingResourcesClass(playerBoardFacades);
        directBuying.startPhase(List.of(PlayerColor.RED, PlayerColor.YELLOW));

        Variant<PhaseStatus, RequestError> result =
                directBuying.buyResources(PlayerColor.YELLOW, List.of(Resource.COAL));

        assertTrue(result.isA());
        assertEquals(PhaseStatus.PHASE_CONTINUES, result.getA());
        assertEquals(List.of(Resource.COAL), yellowBoard.boughtResourcesList);
    }
}
