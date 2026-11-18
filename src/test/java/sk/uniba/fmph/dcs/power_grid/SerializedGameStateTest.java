package sk.uniba.fmph.dcs.power_grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class SerializedGameStateTest {

    @Test
    public void testStateAndToString() {
        SerializedGameState sgs = new SerializedGameState("{\"phase\":\"AUCTION\"}");
        assertEquals("{\"phase\":\"AUCTION\"}", sgs.getState());
        assertEquals("{\"phase\":\"AUCTION\"}", sgs.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        SerializedGameState s1 = new SerializedGameState("STATE_A");
        SerializedGameState s2 = new SerializedGameState("STATE_A");
        SerializedGameState s3 = new SerializedGameState("STATE_B");

        assertEquals(s1, s1);
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertNotEquals(s1, null);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testNullState() {
        new SerializedGameState(null);
    }
}
