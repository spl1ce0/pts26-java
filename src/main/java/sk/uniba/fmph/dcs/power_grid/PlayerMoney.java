package sk.uniba.fmph.dcs.power_grid;

public interface PlayerMoney {
    boolean hasEnoughMoney(int amount);
    void receiveMoney(int numberOfTowns);
    void spendMoney(int amount);
}
