package sk.uniba.fmph.dcs.power_grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class GameControllerTest {

    private FakePlayerOrder determinePlayerOrder;
    private FakeAuction auction;
    private FakeBuyingResources buyingResources;
    private FakeBuildingHouses buildingHouses;
    private FakeBureaucracy bureaucracy;
    private Map<PlayerColor, PlayerBoardFacade> boards;
    private GameController controller;

    private static final class FakePlayerOrder implements InterfaceDeterminePlayerOrder {
        private final List<PlayerColor> order;
        private int reorderCalls = 0;

        FakePlayerOrder(final List<PlayerColor> initial) {
            this.order = new ArrayList<>(initial);
        }

        @Override
        public List<PlayerColor> reorder() {
            reorderCalls++;
            return new ArrayList<>(order);
        }

        @Override
        public List<PlayerColor> gameResult() {
            return new ArrayList<>(order);
        }
    }

    private static final class FakeAuction implements InterfaceAuctionPowerPlants {
        private List<PlayerColor> startedWith;
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) {
            this.startedWith = new ArrayList<>(playerOrder);
        }

        @Override
        public Variant<PhaseStatus, RequestError> selectPowerPlantToBid(
                final PlayerColor player,
                final PowerPlantNumber ppn,
                final int firstBid) {
            return nextResult;
        }

        @Override
        public Variant<PhaseStatus, RequestError> doNotWantPowerPlant(final PlayerColor player) {
            return nextResult;
        }

        @Override
        public Variant<PhaseStatus, RequestError> bidForCurrentPowerPlant(final PlayerColor player, final int bid) {
            return nextResult;
        }

        @Override
        public Variant<PhaseStatus, RequestError> doNotBidForCurrentPowerPlant(final PlayerColor player) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"auctionState\":\"active\"}";
        }
    }

    private static final class FakeBuyingResources implements InterfaceBuyingResources {
        private List<PlayerColor> startedWith;
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) {
            this.startedWith = new ArrayList<>(playerOrder);
        }

        @Override
        public Variant<PhaseStatus, RequestError> buyResources(
                final PlayerColor player,
                final List<Resource> resources) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"buyingResourcesState\":\"active\"}";
        }
    }

    private static final class FakeBuildingHouses implements InterfaceBuildingHouses {
        private List<PlayerColor> startedWith;
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) {
            this.startedWith = new ArrayList<>(playerOrder);
        }

        @Override
        public Variant<PhaseStatus, RequestError> buyHouses(
                final PlayerColor player,
                final List<String> townNames) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"buildingHousesState\":\"active\"}";
        }
    }

    private static final class FakeBureaucracy implements InterfaceBureaucracy {
        private List<PlayerColor> startedWith;
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) {
            this.startedWith = new ArrayList<>(playerOrder);
        }

        @Override
        public Variant<PhaseStatus, RequestError> usePowerPlants(
                final PlayerColor player,
                final List<PowerPlantNumber> ppns,
                final List<Resource> resourcesToBurn) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"bureaucracyState\":\"active\"}";
        }
    }

    @Before
    public void setUp() {
        determinePlayerOrder = new FakePlayerOrder(List.of(PlayerColor.RED, PlayerColor.BLUE));
        auction = new FakeAuction();
        buyingResources = new FakeBuyingResources();
        buildingHouses = new FakeBuildingHouses();
        bureaucracy = new FakeBureaucracy();

        boards = new HashMap<>();
        PlayerBoardFacade redBoard = new PlayerBoardFacade(
                PlayerColor.RED,
                new FakePlayerMoney(50),
                new FakePlayerPowerPlants(),
                new FakePlayerResources()
        );
        boards.put(PlayerColor.RED, redBoard);

        controller = new GameController(
                determinePlayerOrder,
                auction,
                buyingResources,
                buildingHouses,
                bureaucracy,
                boards
        );
    }

    private static final class FakePlayerMoney implements InterfacePlayerMoney {
        private int cash;

        FakePlayerMoney(final int cash) {
            this.cash = cash;
        }

        @Override
        public boolean hasEnoughMoney(final int amount) {
            return cash >= amount;
        }

        @Override
        public void spendMoney(final int amount) {
            cash -= amount;
        }

        @Override
        public void receiveMoney(final int numberOfTowns) {
            cash += numberOfTowns * 10;
        }

        @Override
        public int currentMoney() {
            return cash;
        }
    }

    private static final class FakePlayerPowerPlants implements InterfacePlayerPowerPlants {
        @Override
        public PowerPlantNumber bestPowerPlant() {
            return new PowerPlantNumber(10);
        }

        @Override
        public boolean receivePowerPlant(final PowerPlant powerPlant) {
            return true;
        }

        @Override
        public boolean receivePowerPlant(final PowerPlantNumber powerPlantNumber) {
            return true;
        }

        @Override
        public Optional<RequestError> removePowerPlant(
                final PowerPlantNumber powerPlantNumber,
                final List<Resource> remainingResources) {
            if (powerPlantNumber.getValue() == 99) {
                return Optional.of(new RequestError("Plant 99 not found"));
            }
            return Optional.empty();
        }

        @Override
        public int numberOfConnectableCities(final List<Resource> resources) {
            return 2;
        }

        @Override
        public boolean canStoreResources(final List<Resource> resources) {
            return true;
        }

        @Override
        public List<PowerPlant> getPowerPlants() {
            return Collections.emptyList();
        }
    }

    private static final class FakePlayerResources implements InterfacePlayerResources {
        @Override
        public Optional<RequestError> buyResources(final List<Resource> resources) {
            return Optional.empty();
        }

        @Override
        public Optional<RequestError> usePowerPlants(
                final PlayerColor playerColor,
                final List<PowerPlantNumber> powerPlantNumbers,
                final List<Resource> resourcesToBurn) {
            return Optional.empty();
        }

        @Override
        public int numberOfConnectableCities() {
            return 2;
        }

        @Override
        public Optional<RequestError> removePowerPlant(
                final PowerPlantNumber powerPlantNumber,
                final List<Resource> remainingResources) {
            return Optional.empty();
        }

        @Override
        public List<Resource> getResources() {
            return Collections.emptyList();
        }
    }

    @Test
    public void testInitialization() {
        assertTrue(controller.isFirstTurn());
        assertEquals(GamePhase.AUCTION_POWER_PLANTS, controller.getGamePhase());
        assertEquals(List.of(PlayerColor.RED, PlayerColor.BLUE), controller.getPlayerOrder());
        assertEquals(List.of(PlayerColor.RED, PlayerColor.BLUE), auction.startedWith);
    }

    @Test
    public void testActionsInWrongPhaseReturnError() {
        // Currently in AUCTION_POWER_PLANTS phase
        Optional<RequestError> buyResError = controller.buyResources(PlayerColor.RED, List.of(Resource.COAL));
        assertTrue(buyResError.isPresent());
        assertTrue(buyResError.get().getDescription().contains("BUY_RESOURCES"));

        Optional<RequestError> buyHousesError = controller.buyHouses(PlayerColor.RED, List.of("Berlin"));
        assertTrue(buyHousesError.isPresent());
        assertTrue(buyHousesError.get().getDescription().contains("BUY_HOUSES"));

        Optional<RequestError> usePlantsError = controller.usePowerPlants(
                PlayerColor.RED,
                List.of(new PowerPlantNumber(10)),
                List.of(Resource.COAL));
        assertTrue(usePlantsError.isPresent());
        assertTrue(usePlantsError.get().getDescription().contains("BUREAUCRACY"));
    }

    @Test
    public void testAuctionBiddingAndPhaseTransitions() {
        // Auction actions during auction phase
        auction.nextResult = Variant.ofB(new RequestError("Minimum bid is 10"));
        Optional<RequestError> err = controller.selectPowerPlantToBid(PlayerColor.RED, new PowerPlantNumber(10), 5);
        assertTrue(err.isPresent());
        assertEquals("Minimum bid is 10", err.get().getDescription());

        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);
        Optional<RequestError> passErr = controller.doNotWantPowerPlant(PlayerColor.RED);
        assertTrue(passErr.isEmpty());

        Optional<RequestError> bidErr = controller.bidForCurrentPowerPlant(PlayerColor.RED, 15);
        assertTrue(bidErr.isEmpty());

        Optional<RequestError> noBidErr = controller.doNotBidForCurrentPowerPlant(PlayerColor.RED);
        assertTrue(noBidErr.isEmpty());

        // Now end auction phase -> In first round, re-evaluates order and transitions to BUY_RESOURCES
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        Optional<RequestError> endAuction = controller.selectPowerPlantToBid(PlayerColor.RED, new PowerPlantNumber(10), 10);
        assertTrue(endAuction.isEmpty());
        assertEquals(GamePhase.BUY_RESOURCES, controller.getGamePhase());
        assertEquals(List.of(PlayerColor.RED, PlayerColor.BLUE), buyingResources.startedWith);
    }

    @Test
    public void testFullRoundProgression() {
        // 1. Auction ends -> BuyResources
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.selectPowerPlantToBid(PlayerColor.RED, new PowerPlantNumber(10), 10);
        assertEquals(GamePhase.BUY_RESOURCES, controller.getGamePhase());

        // 2. BuyResources ends -> BuyHouses
        buyingResources.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.buyResources(PlayerColor.RED, List.of(Resource.COAL));
        assertEquals(GamePhase.BUY_HOUSES, controller.getGamePhase());
        assertEquals(List.of(PlayerColor.RED, PlayerColor.BLUE), buildingHouses.startedWith);

        // 3. BuyHouses ends -> Bureaucracy
        buildingHouses.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.buyHouses(PlayerColor.RED, List.of("Hamburg"));
        assertEquals(GamePhase.BUREAUCRACY, controller.getGamePhase());
        assertEquals(List.of(PlayerColor.RED, PlayerColor.BLUE), bureaucracy.startedWith);

        // 4. Bureaucracy ends -> Starts Round 2 in AUCTION_POWER_PLANTS with isFirstTurn = false
        bureaucracy.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.usePowerPlants(PlayerColor.RED, List.of(new PowerPlantNumber(10)), List.of(Resource.COAL));
        assertEquals(GamePhase.AUCTION_POWER_PLANTS, controller.getGamePhase());
        assertFalse(controller.isFirstTurn());
    }

    @Test
    public void testGameEndTransition() {
        // When bureaucracy signals game end
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.selectPowerPlantToBid(PlayerColor.RED, new PowerPlantNumber(10), 10);
        buyingResources.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.buyResources(PlayerColor.RED, List.of(Resource.COAL));
        buildingHouses.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        controller.buyHouses(PlayerColor.RED, List.of("Hamburg"));

        bureaucracy.nextResult = Variant.ofA(PhaseStatus.GAME_ENDS);
        controller.usePowerPlants(PlayerColor.RED, List.of(new PowerPlantNumber(10)), List.of(Resource.COAL));

        assertEquals(GamePhase.GAME_END, controller.getGamePhase());

        // Any action in GAME_END returns error
        Optional<RequestError> err = controller.selectPowerPlantToBid(PlayerColor.RED, new PowerPlantNumber(10), 10);
        assertTrue(err.isPresent());

        Optional<RequestError> scrapErr = controller.removePowerPlant(PlayerColor.RED, new PowerPlantNumber(10), Collections.emptyList());
        assertTrue(scrapErr.isPresent());
        assertTrue(scrapErr.get().getDescription().contains("Game has ended"));
    }

    @Test
    public void testRemovePowerPlantDelegation() {
        // Valid player board
        Optional<RequestError> success = controller.removePowerPlant(PlayerColor.RED, new PowerPlantNumber(10), List.of(Resource.COAL));
        assertTrue(success.isEmpty());

        // Board returns error
        Optional<RequestError> error = controller.removePowerPlant(PlayerColor.RED, new PowerPlantNumber(99), Collections.emptyList());
        assertTrue(error.isPresent());

        // Unknown player board
        Optional<RequestError> unknown = controller.removePowerPlant(PlayerColor.GREEN, new PowerPlantNumber(10), Collections.emptyList());
        assertTrue(unknown.isPresent());
        assertTrue(unknown.get().getDescription().contains("No player board found"));
    }

    @Test
    public void testGetState() {
        String state = controller.getState();
        assertTrue(state.contains("\"phase\":\"AUCTION_POWER_PLANTS\""));
        assertTrue(state.contains("\"isFirstTurn\":true"));
        assertTrue(state.contains("RED"));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOrder() {
        new GameController(null, auction, buyingResources, buildingHouses, bureaucracy, boards);
    }
}
