package sk.uniba.fmph.dcs.power_grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class PowerGridGameAdapterTest {

    private GameController controller;
    private PowerGridGameAdapter adapter;
    private FakeAuction auction;
    private FakeBuyingResources buyingResources;
    private FakeBuildingHouses buildingHouses;
    private FakeBureaucracy bureaucracy;

    private static final class FakePlayerOrder implements InterfaceDeterminePlayerOrder {
        @Override
        public List<PlayerColor> reorder() {
            return List.of(PlayerColor.RED, PlayerColor.BLUE);
        }

        @Override
        public List<PlayerColor> gameResult() {
            return List.of(PlayerColor.RED, PlayerColor.BLUE);
        }
    }

    private static final class FakeAuction implements InterfaceAuctionPowerPlants {
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) { }

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
            return "{\"auction\":\"active\"}";
        }
    }

    private static final class FakeBuyingResources implements InterfaceBuyingResources {
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) { }

        @Override
        public Variant<PhaseStatus, RequestError> buyResources(
                final PlayerColor player,
                final List<Resource> resources) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"buyingResources\":\"active\"}";
        }
    }

    private static final class FakeBuildingHouses implements InterfaceBuildingHouses {
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) { }

        @Override
        public Variant<PhaseStatus, RequestError> buyHouses(
                final PlayerColor player,
                final List<String> townNames) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"buildingHouses\":\"active\"}";
        }
    }

    private static final class FakeBureaucracy implements InterfaceBureaucracy {
        private Variant<PhaseStatus, RequestError> nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);

        @Override
        public void startPhase(final List<PlayerColor> playerOrder) { }

        @Override
        public Variant<PhaseStatus, RequestError> usePowerPlants(
                final PlayerColor player,
                final List<PowerPlantNumber> ppns,
                final List<Resource> resourcesToBurn) {
            return nextResult;
        }

        @Override
        public String getState() {
            return "{\"bureaucracy\":\"active\"}";
        }
    }

    private static final class FakePlayerMoney implements InterfacePlayerMoney {
        @Override
        public boolean hasEnoughMoney(final int amount) {
            return true;
        }

        @Override
        public void spendMoney(final int amount) { }

        @Override
        public void receiveMoney(final int numberOfTowns) { }

        @Override
        public int currentMoney() {
            return 50;
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

    @Before
    public void setUp() {
        auction = new FakeAuction();
        buyingResources = new FakeBuyingResources();
        buildingHouses = new FakeBuildingHouses();
        bureaucracy = new FakeBureaucracy();

        Map<PlayerColor, PlayerBoardFacade> boards = new HashMap<>();
        boards.put(PlayerColor.RED, new PlayerBoardFacade(
                PlayerColor.RED,
                new FakePlayerMoney(),
                new FakePlayerPowerPlants(),
                new FakePlayerResources()
        ));
        boards.put(PlayerColor.BLUE, new PlayerBoardFacade(
                PlayerColor.BLUE,
                new FakePlayerMoney(),
                new FakePlayerPowerPlants(),
                new FakePlayerResources()
        ));

        controller = new GameController(
                new FakePlayerOrder(),
                auction,
                buyingResources,
                buildingHouses,
                bureaucracy,
                boards
        );

        // Map: 0 -> RED, 1 -> BLUE
        adapter = new PowerGridGameAdapter(controller, List.of(PlayerColor.RED, PlayerColor.BLUE));
    }

    @Test
    public void testPlayerIdValidation() {
        // Unknown player ID returns RequestError
        Variant<SerializedGameState, RequestError> result = adapter.selectPowerPlantToBid(99, 10, 10);
        assertTrue(result.isB());
        assertTrue(result.getB().getDescription().contains("Unknown player ID"));
    }

    @Test
    public void testSelectPowerPlantToBidSuccessAndError() {
        // Success (Phase continues)
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_CONTINUES);
        Variant<SerializedGameState, RequestError> result = adapter.selectPowerPlantToBid(0, 10, 10);
        assertTrue(result.isA());
        assertTrue(result.getA().getState().contains("AUCTION_POWER_PLANTS"));

        // Error from controller
        auction.nextResult = Variant.ofB(new RequestError("Bid too low"));
        Variant<SerializedGameState, RequestError> errResult = adapter.selectPowerPlantToBid(0, 10, 5);
        assertTrue(errResult.isB());
        assertEquals("Bid too low", errResult.getB().getDescription());
    }

    @Test
    public void testDoNotWantPowerPlant() {
        Variant<SerializedGameState, RequestError> result = adapter.doNotWantPowerPlant(0);
        assertTrue(result.isA());
    }

    @Test
    public void testBidForCurrentPowerPlantAndDoNotBid() {
        Variant<SerializedGameState, RequestError> bidResult = adapter.bidForCurrentPowerPlant(0, 15);
        assertTrue(bidResult.isA());

        Variant<SerializedGameState, RequestError> passResult = adapter.doNotBidForCurrentPowerPlant(0);
        assertTrue(passResult.isA());
    }

    @Test
    public void testRemovePowerPlant() {
        // Valid resources string: "C O"
        Variant<SerializedGameState, RequestError> result = adapter.removePowerPlant(0, 10, "C O");
        assertTrue(result.isA());

        // Invalid resource character
        Variant<SerializedGameState, RequestError> invalidResult = adapter.removePowerPlant(0, 10, "XYZ");
        assertTrue(invalidResult.isB());
        assertTrue(invalidResult.getB().getDescription().contains("Invalid resource character"));

        // Null resources string
        Variant<SerializedGameState, RequestError> nullResult = adapter.removePowerPlant(0, 10, null);
        assertTrue(nullResult.isB());
        assertTrue(nullResult.getB().getDescription().contains("cannot be null"));
    }

    @Test
    public void testBuyResourcesResourceParsing() {
        // Transition to BUY_RESOURCES phase first
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        adapter.selectPowerPlantToBid(0, 10, 10);

        // Buy resources with "COGU"
        Variant<SerializedGameState, RequestError> result = adapter.buyResources(0, "COGU");
        assertTrue(result.isA());
        assertTrue(result.getA().getState().contains("BUY_RESOURCES"));

        // Buy resources with empty string
        Variant<SerializedGameState, RequestError> emptyResult = adapter.buyResources(0, "");
        assertTrue(emptyResult.isA());

        // Invalid character
        Variant<SerializedGameState, RequestError> errResult = adapter.buyResources(0, "C K");
        assertTrue(errResult.isB());
        assertTrue(errResult.getB().getDescription().contains("Invalid resource character: 'K'"));
    }

    @Test
    public void testBuyHouses() {
        // Transition to BUY_HOUSES phase
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        adapter.selectPowerPlantToBid(0, 10, 10);
        buyingResources.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        adapter.buyResources(0, "C");

        Variant<SerializedGameState, RequestError> result = adapter.buyHouses(0, List.of("Berlin", "Hamburg"));
        assertTrue(result.isA());
        assertTrue(result.getA().getState().contains("BUY_HOUSES"));

        // Null town list handled as empty list
        Variant<SerializedGameState, RequestError> nullResult = adapter.buyHouses(0, null);
        assertTrue(nullResult.isA());
    }

    @Test
    public void testUsePowerPlants() {
        // Transition to BUREAUCRACY phase
        auction.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        adapter.selectPowerPlantToBid(0, 10, 10);
        buyingResources.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        adapter.buyResources(0, "C");
        buildingHouses.nextResult = Variant.ofA(PhaseStatus.PHASE_ENDS);
        adapter.buyHouses(0, List.of("Berlin"));

        // Valid usePowerPlants
        Variant<SerializedGameState, RequestError> result = adapter.usePowerPlants(
                0,
                List.of(10, 15),
                "CC"
        );
        assertTrue(result.isA());
        assertTrue(result.getA().getState().contains("BUREAUCRACY"));

        // Null plant numbers
        Variant<SerializedGameState, RequestError> nullPlants = adapter.usePowerPlants(0, null, "C");
        assertTrue(nullPlants.isB());

        // Plant list with null element
        Variant<SerializedGameState, RequestError> nullElement = adapter.usePowerPlants(0, Collections.singletonList(null), "C");
        assertTrue(nullElement.isB());
    }

    @Test
    public void testGettersAndState() {
        assertEquals(controller, adapter.getGameController());
        assertTrue(adapter.getState().contains("phase"));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullController() {
        new PowerGridGameAdapter(null, List.of(PlayerColor.RED));
    }
}
