package sk.uniba.fmph.dcs.power_grid;

public interface PowerPlantMarket {

    boolean isStep3Start();

    void startStep3();

    void prepareNextTurn();
}
