package sk.uniba.fmph.dcs.power_grid;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

final class FakeTowns implements Towns {
    int maximumNumberOfHouses = 0;
    int step2NotificationCount = 0;
    int step3NotificationCount = 0;

    @Override
    public int getMaxNumberOfHouses() {
        return maximumNumberOfHouses;
    }

    @Override
    public void startStep2() {
        step2NotificationCount++;
    }

    @Override
    public void startStep3() {
        step3NotificationCount++;
    }
}

final class FakePowerPlantMarket implements PowerPlantMarket {
    boolean step3CardDrawn = false;
    int step3NotificationCount = 0;

    @Override
    public boolean isStep3Start() {
        return step3CardDrawn;
    }

    @Override
    public void startStep3() {
        step3NotificationCount++;
    }

    @Override
    public void prepareNextTurn() {
    }
}

final class StubResourcePoolForMarket implements InterfaceResourcePoolResourceMarket {
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

    private FakeTowns fakeTowns;
    private ResourceMarket resourceMarket;
    private FakePowerPlantMarket fakePowerPlantMarket;
    private CheckAndBumpStepClass stepChecker;

    private static ResourceMarket createResourceMarket() {
        Map<Integer, Map<Resource, Integer>> refillPattern = Map.of(
                1, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                2, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1),
                3, Map.of(Resource.COAL, 1, Resource.OIL, 1, Resource.GARBAGE, 1, Resource.URANIUM, 1)
        );
        return new ResourceMarket(new StubResourcePoolForMarket(), List.of(), refillPattern);
    }

    @Before
    public void setUp() {
        fakeTowns = new FakeTowns();
        resourceMarket = createResourceMarket();
        fakePowerPlantMarket = new FakePowerPlantMarket();
        stepChecker = new CheckAndBumpStepClass(fakeTowns, resourceMarket, fakePowerPlantMarket);
    }

    @Test
    public void testInitialStepIsOne() {
        assertEquals(1, stepChecker.getStep());
        assertEquals(0, fakeTowns.step2NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep2BelowThresholdDoesNotTrigger() {
        fakeTowns.maximumNumberOfHouses = 6;
        stepChecker.checkAndBumpToStep2();

        assertEquals(1, stepChecker.getStep());
        assertEquals(0, fakeTowns.step2NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep2AtThresholdTriggers() {
        fakeTowns.maximumNumberOfHouses = 7;
        stepChecker.checkAndBumpToStep2();

        assertEquals(2, stepChecker.getStep());
        assertEquals(1, fakeTowns.step2NotificationCount);
        assertEquals(0, fakeTowns.step3NotificationCount);
        assertEquals(0, fakePowerPlantMarket.step3NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep2AboveThresholdTriggers() {
        fakeTowns.maximumNumberOfHouses = 10;
        stepChecker.checkAndBumpToStep2();

        assertEquals(2, stepChecker.getStep());
        assertEquals(1, fakeTowns.step2NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep2WhenAlreadyAtStep2DoesNotTriggerAgain() {
        fakeTowns.maximumNumberOfHouses = 7;
        stepChecker.checkAndBumpToStep2();
        assertEquals(2, stepChecker.getStep());

        fakeTowns.maximumNumberOfHouses = 8;
        stepChecker.checkAndBumpToStep2();

        assertEquals(2, stepChecker.getStep());
        assertEquals(1, fakeTowns.step2NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep3WhenCardNotDrawnDoesNotTrigger() {
        fakePowerPlantMarket.step3CardDrawn = false;
        stepChecker.checkAndBumpToStep3();

        assertEquals(1, stepChecker.getStep());
        assertEquals(0, fakePowerPlantMarket.step3NotificationCount);
        assertEquals(0, fakeTowns.step3NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep3FromStep2Triggers() {
        fakeTowns.maximumNumberOfHouses = 7;
        stepChecker.checkAndBumpToStep2();
        assertEquals(2, stepChecker.getStep());

        fakePowerPlantMarket.step3CardDrawn = true;
        stepChecker.checkAndBumpToStep3();

        assertEquals(3, stepChecker.getStep());
        assertEquals(1, fakeTowns.step2NotificationCount);
        assertEquals(1, fakeTowns.step3NotificationCount);
        assertEquals(1, fakePowerPlantMarket.step3NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep3DirectlyFromStep1ExecutesStep2ThenStep3() {
        fakePowerPlantMarket.step3CardDrawn = true;
        stepChecker.checkAndBumpToStep3();

        assertEquals(3, stepChecker.getStep());
        assertEquals(1, fakeTowns.step2NotificationCount);
        assertEquals(1, fakeTowns.step3NotificationCount);
        assertEquals(1, fakePowerPlantMarket.step3NotificationCount);
    }

    @Test
    public void testCheckAndBumpToStep3WhenAlreadyAtStep3DoesNotTriggerAgain() {
        fakePowerPlantMarket.step3CardDrawn = true;
        stepChecker.checkAndBumpToStep3();
        assertEquals(3, stepChecker.getStep());

        stepChecker.checkAndBumpToStep3();
        assertEquals(3, stepChecker.getStep());
        assertEquals(1, fakePowerPlantMarket.step3NotificationCount);
        assertEquals(1, fakeTowns.step3NotificationCount);
    }

    @Test
    public void testCustomThresholdForSixPlayers() {
        CheckAndBumpStepClass sixPlayerStepChecker = new CheckAndBumpStepClass(
                fakeTowns, createResourceMarket(), fakePowerPlantMarket, 6);
        fakeTowns.maximumNumberOfHouses = 5;
        sixPlayerStepChecker.checkAndBumpToStep2();
        assertEquals(1, sixPlayerStepChecker.getStep());

        fakeTowns.maximumNumberOfHouses = 6;
        sixPlayerStepChecker.checkAndBumpToStep2();
        assertEquals(2, sixPlayerStepChecker.getStep());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidThresholdThrows() {
        new CheckAndBumpStepClass(fakeTowns, resourceMarket, fakePowerPlantMarket, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTownsThrows() {
        new CheckAndBumpStepClass(null, resourceMarket, fakePowerPlantMarket);
    }

    @Test(expected = NullPointerException.class)
    public void testNullResourceMarketThrows() {
        new CheckAndBumpStepClass(fakeTowns, null, fakePowerPlantMarket);
    }

    @Test(expected = NullPointerException.class)
    public void testNullPowerPlantMarketThrows() {
        new CheckAndBumpStepClass(fakeTowns, resourceMarket, null);
    }
}
