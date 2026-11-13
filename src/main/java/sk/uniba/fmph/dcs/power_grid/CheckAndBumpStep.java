package sk.uniba.fmph.dcs.power_grid;

public interface CheckAndBumpStep {
    int getStep();
    void checkAndBumpToStep2();
    void checkAndBumpToStep3();
}
