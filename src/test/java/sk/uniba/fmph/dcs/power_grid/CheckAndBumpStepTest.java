package sk.uniba.fmph.dcs.power_grid;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

final class TownsFake implements Towns {
    int maxHouses = 0;
    int step2Calls = 0;
    int step3Calls = 0;

    @Override
    public Variant<Integer, RequestError> buyHouses(final PlayerColor player, final List<String> townNames, final int maxMoney) {
        return Variant.ofA(0);
    }

    @Override
    public int getNumberOfHouses(final PlayerColor player) {
        return 0;
    }

    @Override
    public int getMaxNumberOfHouses() {
        return maxHouses;
    }

    @Override
    public void startStep2() {
        step2Calls++;
    }

    @Override
    public void startStep3() {
        step3Calls++;
    }
}

final class PowerPlantMarketFake implements PowerPlantMarket {
    boolean step3Start = false;
    int step3Calls = 0;
    int prepareNextTurnCalls = 0;

    @Override
    public boolean isStep3Start() {
        return step3Start;
    }

    @Override
    public void startStep3() {
        step3Calls++;
    }

    @Override
    public void prepareNextTurn() {
        prepareNextTurnCalls++;
    }
}

final class ResourcePoolStub implements InterfaceResourcePoolResourceMarket {
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

public class CheckAndBumpStepTest {

    private TownsFake towns;
    private ResourceMarket resourceMarket;
    private PowerPlantMarketFake powerPlantMarket;
    private CheckAndBumpStepClass checker;

    private static ResourceMarket createResourceMarket() {
        Map<Integer, Map<Resource, Integer>> pattern = Map.of(
                1, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                2, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                3, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1)
        );
        return new ResourceMarket(new ResourcePoolStub(), List.of(), pattern);
    }

    @Before
    public void setUp() {
        towns = new TownsFake();
        resourceMarket = createResourceMarket();
        powerPlantMarket = new PowerPlantMarketFake();
        checker = new CheckAndBumpStepClass(towns, resourceMarket, powerPlantMarket);
    }

    @Test
    public void testInitialStepIsOne() {
        assertEquals(1, checker.getStep());
        assertEquals(0, towns.step2Calls);
    }

    @Test
    public void testCheckAndBumpToStep2BelowThresholdDoesNotTrigger() {
        towns.maxHouses = 6;
        checker.checkAndBumpToStep2();

        assertEquals(1, checker.getStep());
        assertEquals(0, towns.step2Calls);
    }

    @Test
    public void testCheckAndBumpToStep2AtThresholdTriggers() {
        towns.maxHouses = 7;
        checker.checkAndBumpToStep2();

        assertEquals(2, checker.getStep());
        assertEquals(1, towns.step2Calls);
        assertEquals(0, towns.step3Calls);
        assertEquals(0, powerPlantMarket.step3Calls);
    }

    @Test
    public void testCheckAndBumpToStep2AboveThresholdTriggers() {
        towns.maxHouses = 10;
        checker.checkAndBumpToStep2();

        assertEquals(2, checker.getStep());
        assertEquals(1, towns.step2Calls);
    }

    @Test
    public void testCheckAndBumpToStep2WhenAlreadyAtStep2DoesNotTriggerAgain() {
        towns.maxHouses = 7;
        checker.checkAndBumpToStep2();
        assertEquals(2, checker.getStep());

        towns.maxHouses = 8;
        checker.checkAndBumpToStep2();

        assertEquals(2, checker.getStep());
        assertEquals(1, towns.step2Calls);
    }

    @Test
    public void testCheckAndBumpToStep3WhenNotDrawnDoesNotTrigger() {
        powerPlantMarket.step3Start = false;
        checker.checkAndBumpToStep3();

        assertEquals(1, checker.getStep());
        assertEquals(0, powerPlantMarket.step3Calls);
        assertEquals(0, towns.step3Calls);
    }

    @Test
    public void testCheckAndBumpToStep3FromStep2Triggers() {
        towns.maxHouses = 7;
        checker.checkAndBumpToStep2();
        assertEquals(2, checker.getStep());

        powerPlantMarket.step3Start = true;
        checker.checkAndBumpToStep3();

        assertEquals(3, checker.getStep());
        assertEquals(1, towns.step2Calls);
        assertEquals(1, towns.step3Calls);
        assertEquals(1, powerPlantMarket.step3Calls);
    }

    @Test
    public void testCheckAndBumpToStep3DirectlyFromStep1ExecutesStep2ThenStep3() {
        // According to official rules: if Step 3 starts before Step 2, first apply Step 2 changes, then Step 3.
        powerPlantMarket.step3Start = true;
        checker.checkAndBumpToStep3();

        assertEquals(3, checker.getStep());
        assertEquals(1, towns.step2Calls);
        assertEquals(1, towns.step3Calls);
        assertEquals(1, powerPlantMarket.step3Calls);
    }

    @Test
    public void testCheckAndBumpToStep3WhenAlreadyAtStep3DoesNotTriggerAgain() {
        powerPlantMarket.step3Start = true;
        checker.checkAndBumpToStep3();
        assertEquals(3, checker.getStep());

        checker.checkAndBumpToStep3();
        assertEquals(3, checker.getStep());
        assertEquals(1, powerPlantMarket.step3Calls);
        assertEquals(1, towns.step3Calls);
    }

    @Test
    public void testCustomThresholdForSixPlayers() {
        CheckAndBumpStepClass sixPlayerChecker = new CheckAndBumpStepClass(
                towns, createResourceMarket(), powerPlantMarket, 6);
        towns.maxHouses = 5;
        sixPlayerChecker.checkAndBumpToStep2();
        assertEquals(1, sixPlayerChecker.getStep());

        towns.maxHouses = 6;
        sixPlayerChecker.checkAndBumpToStep2();
        assertEquals(2, sixPlayerChecker.getStep());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidThresholdThrows() {
        new CheckAndBumpStepClass(towns, resourceMarket, powerPlantMarket, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTownsThrows() {
        new CheckAndBumpStepClass(null, resourceMarket, powerPlantMarket);
    }
}
