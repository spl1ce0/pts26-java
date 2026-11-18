package sk.uniba.fmph.dcs.power_grid;

/**
 * Interface representing player's money operations.
 */
public interface PlayerMoney {

    boolean hasEnoughMoney(int amount);

    void receiveMoney(int numberOfTowns);

    void spendMoney(int amount);

    int currentMoney();

    PlayerColor color();

    default int getMoney() {
        return currentMoney();
    }

    default PlayerColor getColor() {
        return color();
    }
}
