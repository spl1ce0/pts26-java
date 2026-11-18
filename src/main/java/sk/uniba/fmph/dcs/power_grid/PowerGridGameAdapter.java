package sk.uniba.fmph.dcs.power_grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapter converting external primitive and string types to internal domain types
 * for the GameController according to the PowerGridRulesGameInterface contract.
 */
public final class PowerGridGameAdapter implements PowerGridRulesGameInterface {

    private final GameController gameController;
    private final Map<Integer, PlayerColor> playerMapping;

    /**
     * Constructs a PowerGridGameAdapter with an explicit player ID to PlayerColor mapping.
     *
     * @param gameController the underlying GameController engine
     * @param playerMapping  mapping from integer player ID to PlayerColor
     */
    public PowerGridGameAdapter(
            final GameController gameController,
            final Map<Integer, PlayerColor> playerMapping) {
        this.gameController = Objects.requireNonNull(gameController, "GameController cannot be null");
        Objects.requireNonNull(playerMapping, "PlayerMapping cannot be null");
        this.playerMapping = Map.copyOf(playerMapping);
    }

    /**
     * Constructs a PowerGridGameAdapter from a list of players, supporting both
     * 0-based and 1-based indexing for player IDs.
     *
     * @param gameController the underlying GameController engine
     * @param players        list of player colors
     */
    public PowerGridGameAdapter(
            final GameController gameController,
            final List<PlayerColor> players) {
        this.gameController = Objects.requireNonNull(gameController, "GameController cannot be null");
        Objects.requireNonNull(players, "Players cannot be null");

        Map<Integer, PlayerColor> map = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            map.put(i, players.get(i));
            map.put(i + 1, players.get(i));
        }
        this.playerMapping = Collections.unmodifiableMap(map);
    }

    public GameController getGameController() {
        return gameController;
    }

    public String getState() {
        return gameController.getState();
    }

    @Override
    public Variant<SerializedGameState, RequestError> selectPowerPlantToBid(
            final int playerId,
            final int powerPlantNumber,
            final int firstBid) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        Optional<RequestError> error = gameController.selectPowerPlantToBid(
                playerResult.getA(),
                new PowerPlantNumber(powerPlantNumber),
                firstBid
        );
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> doNotWantPowerPlant(final int playerId) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        Optional<RequestError> error = gameController.doNotWantPowerPlant(playerResult.getA());
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> bidForCurrentPowerPlant(
            final int playerId,
            final int bid) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        Optional<RequestError> error = gameController.bidForCurrentPowerPlant(playerResult.getA(), bid);
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> doNotBidForCurrentPowerPlant(final int playerId) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        Optional<RequestError> error = gameController.doNotBidForCurrentPowerPlant(playerResult.getA());
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> removePowerPlant(
            final int playerId,
            final int powerPlantNumber,
            final String removedResources) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        Variant<List<Resource>, RequestError> resourcesResult = parseResources(removedResources);
        if (resourcesResult.isB()) {
            return Variant.ofB(resourcesResult.getB());
        }

        Optional<RequestError> error = gameController.removePowerPlant(
                playerResult.getA(),
                new PowerPlantNumber(powerPlantNumber),
                resourcesResult.getA()
        );
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> buyResources(
            final int playerId,
            final String resources) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        Variant<List<Resource>, RequestError> resourcesResult = parseResources(resources);
        if (resourcesResult.isB()) {
            return Variant.ofB(resourcesResult.getB());
        }

        Optional<RequestError> error = gameController.buyResources(
                playerResult.getA(),
                resourcesResult.getA()
        );
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> buyHouses(
            final int playerId,
            final List<String> townNames) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        List<String> towns = townNames != null ? townNames : Collections.emptyList();
        Optional<RequestError> error = gameController.buyHouses(playerResult.getA(), towns);
        return wrapResult(error);
    }

    @Override
    public Variant<SerializedGameState, RequestError> usePowerPlants(
            final int playerId,
            final List<Integer> powerPlantNumbers,
            final String resourcesToBurn) {
        Variant<PlayerColor, RequestError> playerResult = getPlayer(playerId);
        if (playerResult.isB()) {
            return Variant.ofB(playerResult.getB());
        }

        if (powerPlantNumbers == null) {
            return Variant.ofB(new RequestError("Power plant numbers list cannot be null"));
        }

        Variant<List<Resource>, RequestError> resourcesResult = parseResources(resourcesToBurn);
        if (resourcesResult.isB()) {
            return Variant.ofB(resourcesResult.getB());
        }

        List<PowerPlantNumber> ppns = new ArrayList<>();
        for (Integer num : powerPlantNumbers) {
            if (num == null) {
                return Variant.ofB(new RequestError("Power plant number cannot be null"));
            }
            ppns.add(new PowerPlantNumber(num));
        }

        Optional<RequestError> error = gameController.usePowerPlants(
                playerResult.getA(),
                ppns,
                resourcesResult.getA()
        );
        return wrapResult(error);
    }

    private Variant<PlayerColor, RequestError> getPlayer(final int playerId) {
        PlayerColor color = playerMapping.get(playerId);
        if (color == null) {
            return Variant.ofB(new RequestError("Unknown player ID: " + playerId));
        }
        return Variant.ofA(color);
    }

    private Variant<List<Resource>, RequestError> parseResources(final String resourcesStr) {
        if (resourcesStr == null) {
            return Variant.ofB(new RequestError("Resources string cannot be null"));
        }

        List<Resource> result = new ArrayList<>();
        for (int i = 0; i < resourcesStr.length(); i++) {
            char ch = resourcesStr.charAt(i);
            if (Character.isWhitespace(ch)) {
                continue;
            }

            char upper = Character.toUpperCase(ch);
            switch (upper) {
                case 'C':
                    result.add(Resource.COAL);
                    break;
                case 'O':
                    result.add(Resource.OIL);
                    break;
                case 'G':
                    result.add(Resource.GARBAGE);
                    break;
                case 'U':
                    result.add(Resource.URANIUM);
                    break;
                default:
                    return Variant.ofB(new RequestError(
                            "Invalid resource character: '" + ch + "' in \"" + resourcesStr + "\""));
            }
        }
        return Variant.ofA(result);
    }

    private Variant<SerializedGameState, RequestError> wrapResult(final Optional<RequestError> error) {
        if (error.isPresent()) {
            return Variant.ofB(error.get());
        }
        return Variant.ofA(new SerializedGameState(gameController.getState()));
    }
}
