package sk.uniba.fmph.dcs.power_grid;

import java.util.Objects;

public final class CheckAndBumpStepClass implements CheckAndBumpStep {

    public static final int DEFAULT_STEP_2_THRESHOLD = 7;

    private int step;
    private final int step2Threshold;
    private final Towns towns;
    private final ResourceMarket resourceMarket;
    private final PowerPlantMarket powerPlantMarket;

    public CheckAndBumpStepClass(
            final Towns towns,
            final ResourceMarket resourceMarket,
            final PowerPlantMarket powerPlantMarket,
            final int step2Threshold) {
        this.towns = Objects.requireNonNull(towns, "Towns cannot be null");
        this.resourceMarket = Objects.requireNonNull(resourceMarket, "ResourceMarket cannot be null");
        this.powerPlantMarket = Objects.requireNonNull(powerPlantMarket, "PowerPlantMarket cannot be null");
        if (step2Threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }
        this.step2Threshold = step2Threshold;
        this.step = 1;
    }

    public CheckAndBumpStepClass(
            final Towns towns,
            final ResourceMarket resourceMarket,
            final PowerPlantMarket powerPlantMarket) {
        this(towns, resourceMarket, powerPlantMarket, DEFAULT_STEP_2_THRESHOLD);
    }

    @Override
    public int getStep() {
        return step;
    }

    private void startStep2() {
        if (step == 1) {
            step = 2;
            towns.startStep2();
            resourceMarket.startStep2();
        }
    }

    private void startStep3() {
        if (step == 1) {
            startStep2();
        }
        if (step < 3) {
            step = 3;
            towns.startStep3();
            resourceMarket.startStep3();
            powerPlantMarket.startStep3();
        }
    }

    @Override
    public void checkAndBumpToStep2() {
        if (step == 1 && towns.getMaxNumberOfHouses() >= step2Threshold) {
            startStep2();
        }
    }

    @Override
    public void checkAndBumpToStep3() {
        if (step < 3 && powerPlantMarket.isStep3Start()) {
            startStep3();
        }
    }
}
