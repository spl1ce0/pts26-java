package sk.uniba.fmph.dcs.power_grid;

public class PlayerMoneyClass implements PlayerMoney {

    private PlayerColor color;
    private int currentMoney;
    private final int START_MONEY = 50;
    private static final int[] PAYMENT_TABLE = {
            10, 22, 33, 44, 54, 64, 73, 82, 90, 98,
            105, 112, 118, 124, 129, 134, 138, 142, 145, 148, 150
    };

    // inicialize the player with the start money
    public PlayerMoneyClass(PlayerColor color) {
        this.color = color;
        this.currentMoney = START_MONEY;
    }

    // allows setting an arbitrary initial value (for testing purposes)
    public PlayerMoneyClass(PlayerColor color, int initialMoney) {
        this.color = color;
        this.currentMoney = initialMoney;
    }

    @Override
    public boolean hasEnoughMoney(int amount) {
        return currentMoney >= amount && amount >= 0;
    }

    @Override
    public void receiveMoney(int numberOfTowns) {
        if (numberOfTowns < 0) {
            throw new IllegalArgumentException();
        }
        int index = Math.min(numberOfTowns, PAYMENT_TABLE.length - 1);
        currentMoney += PAYMENT_TABLE[index];
    }

    @Override
    public void spendMoney(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException();
        }
        if (amount > currentMoney) {
            throw new IllegalStateException();
        }
        currentMoney -= amount;
    }
}
