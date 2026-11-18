package sk.uniba.fmph.dcs.power_grid;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for PlayerMoneyClass.
 */
public class PlayerMoneyTest {

    @Test
    public void testDefaultConstructor() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.RED);
        assertEquals(PlayerColor.RED, playerMoney.color());
        assertEquals(PlayerColor.RED, playerMoney.getColor());
        assertEquals(50, playerMoney.currentMoney());
        assertEquals(50, playerMoney.getMoney());
    }

    @Test
    public void testCustomConstructor() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.BLUE, 100);
        assertEquals(PlayerColor.BLUE, playerMoney.color());
        assertEquals(100, playerMoney.currentMoney());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullColorThrowsException() {
        new PlayerMoneyClass(null, 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeInitialMoneyThrowsException() {
        new PlayerMoneyClass(PlayerColor.GREEN, -10);
    }

    @Test
    public void testHasEnoughMoney() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.YELLOW, 50);

        assertTrue(playerMoney.hasEnoughMoney(0));
        assertTrue(playerMoney.hasEnoughMoney(30));
        assertTrue(playerMoney.hasEnoughMoney(50));
        assertFalse(playerMoney.hasEnoughMoney(51));
        assertFalse(playerMoney.hasEnoughMoney(100));
        assertFalse(playerMoney.hasEnoughMoney(-5));
    }

    @Test
    public void testSpendMoney() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.PURPLE, 50);

        playerMoney.spendMoney(20);
        assertEquals(30, playerMoney.currentMoney());

        playerMoney.spendMoney(30);
        assertEquals(0, playerMoney.currentMoney());
    }

    @Test(expected = IllegalStateException.class)
    public void testSpendMoreThanAvailableThrowsException() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.BLACK, 20);
        playerMoney.spendMoney(25);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpendZeroThrowsException() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.RED, 50);
        playerMoney.spendMoney(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpendNegativeAmountThrowsException() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.RED, 50);
        playerMoney.spendMoney(-10);
    }

    @Test
    public void testReceiveMoneyIncomeTable() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.RED, 0);

        // 0 towns -> +10
        playerMoney.receiveMoney(0);
        assertEquals(10, playerMoney.currentMoney());

        // 1 town -> +22 (10 + 22 = 32)
        playerMoney.receiveMoney(1);
        assertEquals(32, playerMoney.currentMoney());

        // 2 towns -> +33 (32 + 33 = 65)
        playerMoney.receiveMoney(2);
        assertEquals(65, playerMoney.currentMoney());
    }

    @Test
    public void testReceiveMoneyForHighTownCounts() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.BLUE, 0);

        // 10 towns -> +105
        playerMoney.receiveMoney(10);
        assertEquals(105, playerMoney.currentMoney());

        // 20 towns -> +150
        PlayerMoney playerMoney20 = new PlayerMoneyClass(PlayerColor.GREEN, 0);
        playerMoney20.receiveMoney(20);
        assertEquals(150, playerMoney20.currentMoney());

        // Towns beyond 20 cap at 150
        PlayerMoney playerMoneyBeyond = new PlayerMoneyClass(PlayerColor.YELLOW, 0);
        playerMoneyBeyond.receiveMoney(25);
        assertEquals(150, playerMoneyBeyond.currentMoney());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReceiveMoneyNegativeTownsThrowsException() {
        PlayerMoney playerMoney = new PlayerMoneyClass(PlayerColor.RED, 50);
        playerMoney.receiveMoney(-1);
    }
}
