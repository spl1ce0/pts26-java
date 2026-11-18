package sk.uniba.fmph.dcs.power_grid;

/**
 * Implementation of PlayerMoney managing a player's funds.
 */
public final class PlayerMoneyClass implements PlayerMoney {

    private static final int START_MONEY = 50;

    private static final int[] PAYMENT_TABLE = {
            10, 22, 33, 44, 54, 64, 73, 82, 90, 98,
            105, 112, 118, 124, 129, 134, 138, 142, 145, 148, 150
    };

    private final PlayerColor color;
    private int currentMoney;

    public PlayerMoneyClass(final PlayerColor color) {
        this(color, START_MONEY);
    }

    public PlayerMoneyClass(final PlayerColor color, final int initialMoney) {
        if (color == null) {
            throw new IllegalArgumentException("Player color cannot be null.");
        }
        if (initialMoney < 0) {
            throw new IllegalArgumentException("Initial money cannot be negative.");
        }
        this.color = color;
        this.currentMoney = initialMoney;
    }

    @Override
    public boolean hasEnoughMoney(final int amount) {
        return currentMoney >= amount && amount >= 0;
    }

    @Override
    public void receiveMoney(final int numberOfTowns) {
        if (numberOfTowns < 0) {
            throw new IllegalArgumentException("Number of towns cannot be negative: " + numberOfTowns);
        }
        int index = Math.min(numberOfTowns, PAYMENT_TABLE.length - 1);
        currentMoney += PAYMENT_TABLE[index];
    }

    @Override
    public void spendMoney(final int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to spend must be positive: " + amount);
        }
        if (amount > currentMoney) {
            throw new IllegalStateException("Insufficient funds: current " + currentMoney + ", attempted " + amount);
        }
        currentMoney -= amount;
    }

    @Override
    public int currentMoney() {
        return currentMoney;
    }

    @Override
    public PlayerColor color() {
        return color;
    }
}
