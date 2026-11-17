package sk.uniba.fmph.dcs.power_grid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Central orchestrator controlling round progression, phase transitions,
 * turn order updates, and player action delegation across Power Grid phases.
 */
public final class GameController {

    private boolean isFirstTurn;
    private GamePhase gamePhase;
    private List<PlayerColor> playerOrder;

    private final InterfaceDeterminePlayerOrder determinePlayerOrder;
    private final InterfaceAuctionPowerPlants auctionPowerPlants;
    private final InterfaceBuyingResources buyingResources;
    private final InterfaceBuildingHouses buildingHouses;
    private final InterfaceBureaucracy bureaucracy;
    private final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades;

    /**
     * Constructs a GameController, initializing round 1 with player order
     * and starting the AuctionPowerPlants phase.
     *
     * @param determinePlayerOrder player order calculator
     * @param auctionPowerPlants   AuctionPowerPlants phase coordinator
     * @param buyingResources     BuyingResources phase coordinator
     * @param buildingHouses      BuildingHouses phase coordinator
     * @param bureaucracy         Bureaucracy phase coordinator
     * @param playerBoardFacades  map of player boards
     */
    public GameController(
            final InterfaceDeterminePlayerOrder determinePlayerOrder,
            final InterfaceAuctionPowerPlants auctionPowerPlants,
            final InterfaceBuyingResources buyingResources,
            final InterfaceBuildingHouses buildingHouses,
            final InterfaceBureaucracy bureaucracy,
            final Map<PlayerColor, PlayerBoardFacade> playerBoardFacades) {
        this.determinePlayerOrder = Objects.requireNonNull(determinePlayerOrder, "DeterminePlayerOrder cannot be null");
        this.auctionPowerPlants = Objects.requireNonNull(auctionPowerPlants, "AuctionPowerPlants cannot be null");
        this.buyingResources = Objects.requireNonNull(buyingResources, "BuyingResources cannot be null");
        this.buildingHouses = Objects.requireNonNull(buildingHouses, "BuildingHouses cannot be null");
        this.bureaucracy = Objects.requireNonNull(bureaucracy, "Bureaucracy cannot be null");
        this.playerBoardFacades = Map.copyOf(Objects.requireNonNull(playerBoardFacades, "PlayerBoards cannot be null"));

        this.isFirstTurn = true;
        this.playerOrder = this.determinePlayerOrder.reorder();
        this.gamePhase = GamePhase.AUCTION_POWER_PLANTS;
        this.auctionPowerPlants.startPhase(this.playerOrder);
    }

    public boolean isFirstTurn() {
        return isFirstTurn;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public List<PlayerColor> getPlayerOrder() {
        return Collections.unmodifiableList(playerOrder);
    }

    /**
     * Phase 2: Selects a power plant to start an auction.
     *
     * @param player   the player opening the bid
     * @param ppn      the chosen power plant number
     * @param firstBid the opening bid amount
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> selectPowerPlantToBid(
            final PlayerColor player,
            final PowerPlantNumber ppn,
            final int firstBid) {
        if (gamePhase != GamePhase.AUCTION_POWER_PLANTS) {
            return Optional.of(new RequestError("Cannot select power plant to bid outside AUCTION_POWER_PLANTS phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = auctionPowerPlants.selectPowerPlantToBid(player, ppn, firstBid);
        return handlePhaseResult(result);
    }

    /**
     * Phase 2: Player chooses to pass offering a power plant for auction.
     *
     * @param player the player opting out
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> doNotWantPowerPlant(final PlayerColor player) {
        if (gamePhase != GamePhase.AUCTION_POWER_PLANTS) {
            return Optional.of(new RequestError("Cannot pass auction outside AUCTION_POWER_PLANTS phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = auctionPowerPlants.doNotWantPowerPlant(player);
        return handlePhaseResult(result);
    }

    /**
     * Phase 2: Places a higher bid on the currently active power plant.
     *
     * @param player the bidding player
     * @param bid    the bid amount
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> bidForCurrentPowerPlant(final PlayerColor player, final int bid) {
        if (gamePhase != GamePhase.AUCTION_POWER_PLANTS) {
            return Optional.of(new RequestError("Cannot bid outside AUCTION_POWER_PLANTS phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = auctionPowerPlants.bidForCurrentPowerPlant(player, bid);
        return handlePhaseResult(result);
    }

    /**
     * Phase 2: Drops out of the current power plant bidding.
     *
     * @param player the player dropping out of bidding
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> doNotBidForCurrentPowerPlant(final PlayerColor player) {
        if (gamePhase != GamePhase.AUCTION_POWER_PLANTS) {
            return Optional.of(new RequestError("Cannot pass bidding outside AUCTION_POWER_PLANTS phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = auctionPowerPlants.doNotBidForCurrentPowerPlant(player);
        return handlePhaseResult(result);
    }

    /**
     * Scraps an owned power plant when a 4th plant is acquired, transferring resources.
     *
     * @param player    the player removing the power plant
     * @param ppn       the power plant number to discard
     * @param resources resources from the scrapped power plant
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> removePowerPlant(
            final PlayerColor player,
            final PowerPlantNumber ppn,
            final List<Resource> resources) {
        if (gamePhase == GamePhase.GAME_END) {
            return Optional.of(new RequestError("Game has ended."));
        }
        PlayerBoardFacade board = playerBoardFacades.get(player);
        if (board == null) {
            return Optional.of(new RequestError("No player board found for player " + player));
        }
        return board.removePowerPlant(ppn, resources);
    }

    /**
     * Phase 3: Buys resources from the resource market.
     *
     * @param player           the buying player
     * @param removedResources the list of resources to purchase
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> buyResources(
            final PlayerColor player,
            final List<Resource> removedResources) {
        if (gamePhase != GamePhase.BUY_RESOURCES) {
            return Optional.of(new RequestError("Cannot buy resources outside BUY_RESOURCES phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = buyingResources.buyResources(player, removedResources);
        return handlePhaseResult(result);
    }

    /**
     * Phase 4: Connects houses in cities across the map.
     *
     * @param player    the building player
     * @param townNames the list of town names to build in
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> buyHouses(
            final PlayerColor player,
            final List<String> townNames) {
        if (gamePhase != GamePhase.BUY_HOUSES) {
            return Optional.of(new RequestError("Cannot buy houses outside BUY_HOUSES phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = buildingHouses.buyHouses(player, townNames);
        return handlePhaseResult(result);
    }

    /**
     * Phase 5: Uses power plants and burns resources to power cities during Bureaucracy.
     *
     * @param player          the powering player
     * @param ppns            the power plant numbers to use
     * @param resourcesToBurn the resources to burn
     * @return Optional.empty() on success, or RequestError if invalid
     */
    public Optional<RequestError> usePowerPlants(
            final PlayerColor player,
            final List<PowerPlantNumber> ppns,
            final List<Resource> resourcesToBurn) {
        if (gamePhase != GamePhase.BUREAUCRACY) {
            return Optional.of(new RequestError("Cannot use power plants outside BUREAUCRACY phase. Current phase: " + gamePhase));
        }
        Variant<PhaseStatus, RequestError> result = bureaucracy.usePowerPlants(player, ppns, resourcesToBurn);
        return handlePhaseResult(result);
    }

    private Optional<RequestError> handlePhaseResult(final Variant<PhaseStatus, RequestError> result) {
        if (result.isB()) {
            return Optional.of(result.getB());
        }

        PhaseStatus status = result.getA();
        if (status == PhaseStatus.GAME_ENDS) {
            this.gamePhase = GamePhase.GAME_END;
            return Optional.empty();
        }

        if (status == PhaseStatus.PHASE_ENDS) {
            advanceToNextPhase();
        }

        return Optional.empty();
    }

    private void advanceToNextPhase() {
        switch (gamePhase) {
            case AUCTION_POWER_PLANTS:
                if (isFirstTurn) {
                    this.playerOrder = determinePlayerOrder.reorder();
                }
                this.gamePhase = GamePhase.BUY_RESOURCES;
                this.buyingResources.startPhase(this.playerOrder);
                break;

            case BUY_RESOURCES:
                this.gamePhase = GamePhase.BUY_HOUSES;
                this.buildingHouses.startPhase(this.playerOrder);
                break;

            case BUY_HOUSES:
                this.gamePhase = GamePhase.BUREAUCRACY;
                this.bureaucracy.startPhase(this.playerOrder);
                break;

            case BUREAUCRACY:
                this.isFirstTurn = false;
                this.playerOrder = determinePlayerOrder.reorder();
                this.gamePhase = GamePhase.AUCTION_POWER_PLANTS;
                this.auctionPowerPlants.startPhase(this.playerOrder);
                break;

            default:
                break;
        }
    }

    /**
     * Returns a serialized JSON string representing the complete game state.
     *
     * @return serialized game state string
     */
    public String getState() {
        return String.format(
                "{\"phase\":\"%s\",\"isFirstTurn\":%b,\"playerOrder\":%s}",
                gamePhase.name(),
                isFirstTurn,
                playerOrder != null ? playerOrder.toString() : "[]"
        );
    }
}
