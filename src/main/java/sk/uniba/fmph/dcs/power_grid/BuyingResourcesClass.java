package sk.uniba.fmph.dcs.power_grid;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BuyingResourcesClass implements BuyingResources {

    private final ResourceMarket resourceMarket;
    private final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades;

    private List<PlayerColor> reversePlayerOrder = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private boolean isPhaseActive = false;

    public BuyingResourcesClass(
            final ResourceMarket resourceMarket,
            final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades) {
        this.resourceMarket = Objects.requireNonNull(resourceMarket, "ResourceMarket cannot be null");
        this.playerBoardFacades = Objects.requireNonNull(playerBoardFacades, "PlayerBoardFacades cannot be null");
    }

    @Override
    public void startPhase(final List<PlayerColor> playerOrder) {
        Objects.requireNonNull(playerOrder, "Player order cannot be null");
        if (playerOrder.isEmpty()) {
            throw new IllegalArgumentException("Player order cannot be empty");
        }

        this.reversePlayerOrder = new ArrayList<>(playerOrder);
        Collections.reverse(this.reversePlayerOrder);
        this.currentPlayerIndex = 0;
        this.isPhaseActive = true;
    }

    @Override
    public Variant<PhaseStatus, RequestError> buyResources(
            final PlayerColor playerColor,
            final List<Resource> resourcesToBuy) {
        Objects.requireNonNull(playerColor, "PlayerColor cannot be null");
        Objects.requireNonNull(resourcesToBuy, "Resources to buy cannot be null");

        if (!isPhaseActive) {
            return Variant.ofB(new RequestError("Buy resources phase is not active."));
        }

        PlayerColor expectedPlayer = this.reversePlayerOrder.get(this.currentPlayerIndex);
        if (!expectedPlayer.equals(playerColor)) {
            return Variant.ofB(new RequestError("It is not player " + playerColor + "'s turn to buy resources."));
        }

        PlayerBoardFacade playerBoard = this.playerBoardFacades.get(playerColor);
        if (playerBoard == null) {
            return Variant.ofB(new RequestError("No player board found for player " + playerColor + "."));
        }

        if (!resourcesToBuy.isEmpty()) {
            Integer totalCost = this.resourceMarket.getPrice(resourcesToBuy);
            if (totalCost == null) {
                return Variant.ofB(new RequestError("The requested resources are not available in the market."));
            }

            if (!playerBoard.hasEnoughMoney(totalCost)) {
                return Variant.ofB(new RequestError("Player " + playerColor + " does not have enough money."));
            }

            Optional<RequestError> storageError = playerBoard.buyResources(resourcesToBuy);
            if (storageError.isPresent()) {
                return Variant.ofB(storageError.get());
            }

            playerBoard.spendMoney(totalCost);
        }

        this.currentPlayerIndex++;
        if (this.currentPlayerIndex >= this.reversePlayerOrder.size()) {
            this.isPhaseActive = false;
            return Variant.ofA(PhaseStatus.PHASE_ENDS);
        }

        return Variant.ofA(PhaseStatus.PHASE_CONTINUES);
    }

    @Override
    public String getState() {
        JSONObject stateJson = new JSONObject();
        stateJson.put("phase", "BuyResources");
        stateJson.put("isPhaseActive", this.isPhaseActive);

        JSONArray orderArray = new JSONArray();
        for (PlayerColor playerColor : this.reversePlayerOrder) {
            orderArray.put(playerColor.name());
        }
        stateJson.put("turnOrder", orderArray);

        if (this.isPhaseActive && this.currentPlayerIndex < this.reversePlayerOrder.size()) {
            stateJson.put("currentPlayer", this.reversePlayerOrder.get(this.currentPlayerIndex).name());
        } else {
            stateJson.put("currentPlayer", JSONObject.NULL);
        }

        return stateJson.toString();
    }
}
