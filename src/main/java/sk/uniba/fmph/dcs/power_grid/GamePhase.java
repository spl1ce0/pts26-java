package sk.uniba.fmph.dcs.power_grid;

/**
 * Enumeration of the different phases of a Power Grid round,
 * plus the terminal game end state.
 */
public enum GamePhase {
    AUCTION_POWER_PLANTS,
    BUY_RESOURCES,
    BUY_HOUSES,
    BUREAUCRACY,
    GAME_END
}
