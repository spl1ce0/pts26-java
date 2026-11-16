package sk.uniba.fmph.dcs.power_grid;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BureaucracyClass implements Bureaucracy {

    public static final int DEFAULT_GAME_END_HOUSE_THRESHOLD = 17;

    private final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades;
    private final CheckAndBumpStep checkAndBumpStep;
    private final ResourceMarket resourceMarket;
    private final PowerPlantMarket powerPlantMarket;
    private final Towns towns;
    private final int gameEndHouseThreshold;

    private List<PlayerColor> playerOrder = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean isPhaseActive = false;

    public BureaucracyClass(
            final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades,
            final CheckAndBumpStep checkAndBumpStep,
            final ResourceMarket resourceMarket,
            final PowerPlantMarket powerPlantMarket,
            final Towns towns,
            final int gameEndHouseThreshold) {
        this.playerBoardFacades = Objects.requireNonNull(playerBoardFacades, "PlayerBoardFacades cannot be null");
        this.checkAndBumpStep = Objects.requireNonNull(checkAndBumpStep, "CheckAndBumpStep cannot be null");
        this.resourceMarket = Objects.requireNonNull(resourceMarket, "ResourceMarket cannot be null");
        this.powerPlantMarket = Objects.requireNonNull(powerPlantMarket, "PowerPlantMarket cannot be null");
        this.towns = towns;
        if (gameEndHouseThreshold <= 0) {
            throw new IllegalArgumentException("Game end house threshold must be positive");
        }
        this.gameEndHouseThreshold = gameEndHouseThreshold;
    }

    public BureaucracyClass(
            final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades,
            final CheckAndBumpStep checkAndBumpStep,
            final ResourceMarket resourceMarket,
            final PowerPlantMarket powerPlantMarket,
            final Towns towns) {
        this(playerBoardFacades, checkAndBumpStep, resourceMarket, powerPlantMarket, towns, DEFAULT_GAME_END_HOUSE_THRESHOLD);
    }

    public BureaucracyClass(
            final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades,
            final CheckAndBumpStep checkAndBumpStep,
            final ResourceMarket resourceMarket,
            final PowerPlantMarket powerPlantMarket) {
        this(playerBoardFacades, checkAndBumpStep, resourceMarket, powerPlantMarket, null, DEFAULT_GAME_END_HOUSE_THRESHOLD);
    }

    @Override
    public void startPhase(final List<PlayerColor> playerOrder) {
        Objects.requireNonNull(playerOrder, "Player order cannot be null");
        if (playerOrder.isEmpty()) {
            throw new IllegalArgumentException("Player order cannot be empty");
        }

        this.playerOrder = Collections.unmodifiableList(new ArrayList<>(playerOrder));
        this.currentPlayerIndex = 0;
        this.isPhaseActive = true;
    }

    @Override
    public Variant<PhaseStatus, RequestError> usePowerPlants(
            final PlayerColor playerColor,
            final List<PowerPlantNumber> powerPlantNumbers,
            final List<Resource> resourcesToBurn) {
        Objects.requireNonNull(playerColor, "PlayerColor cannot be null");
        Objects.requireNonNull(powerPlantNumbers, "Power plant numbers cannot be null");
        Objects.requireNonNull(resourcesToBurn, "Resources to burn cannot be null");

        if (!this.isPhaseActive) {
            return Variant.ofB(new RequestError("Bureaucracy phase is not active."));
        }

        PlayerColor expectedPlayer = this.playerOrder.get(this.currentPlayerIndex);
        if (!expectedPlayer.equals(playerColor)) {
            return Variant.ofB(new RequestError("It is not player " + playerColor + "'s turn."));
        }

        PlayerBoardFacade playerBoardFacade = this.playerBoardFacades.get(playerColor);
        if (playerBoardFacade == null) {
            return Variant.ofB(new RequestError("No player board found for player " + playerColor + "."));
        }

        Optional<RequestError> usageError = playerBoardFacade.usePowerPlants(
                playerColor,
                powerPlantNumbers,
                resourcesToBurn);
        if (usageError.isPresent()) {
            return Variant.ofB(usageError.get());
        }

        this.currentPlayerIndex++;
        if (this.currentPlayerIndex < this.playerOrder.size()) {
            return Variant.ofA(PhaseStatus.PHASE_CONTINUES);
        }

        this.isPhaseActive = false;

        if (this.towns != null && this.towns.getMaxNumberOfHouses() >= this.gameEndHouseThreshold) {
            return Variant.ofA(PhaseStatus.GAME_ENDS);
        }

        this.checkAndBumpStep.checkAndBumpToStep2();
        this.resourceMarket.refill();
        this.powerPlantMarket.prepareNextTurn();
        this.checkAndBumpStep.checkAndBumpToStep3();

        return Variant.ofA(PhaseStatus.PHASE_ENDS);
    }

    @Override
    public String getState() {
        JSONObject stateJson = new JSONObject();
        stateJson.put("phase", "Bureaucracy");
        stateJson.put("isPhaseActive", this.isPhaseActive);

        JSONArray orderArray = new JSONArray();
        for (PlayerColor playerColor : this.playerOrder) {
            orderArray.put(playerColor.name());
        }
        stateJson.put("turnOrder", orderArray);

        if (this.isPhaseActive && this.currentPlayerIndex < this.playerOrder.size()) {
            stateJson.put("currentPlayer", this.playerOrder.get(this.currentPlayerIndex).name());
        } else {
            stateJson.put("currentPlayer", JSONObject.NULL);
        }

        return stateJson.toString();
    }
}
