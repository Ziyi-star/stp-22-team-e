package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.*;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.rest.PioneersApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PioneersService {
    private final PioneersApiService pioneersApiService;
    private final ObjectMapper mapper;
    private final GameStorage gameStorage;
    private final LoginResultStorage loginResultStorage;
    private final HashMap<String, Integer> armies = new HashMap<>();
    private final VictoryStorage victoryStorage;
    private final GameService gameService;


    @Inject
    public PioneersService(PioneersApiService pioneersApiService, ObjectMapper mapper, GameStorage gameStorage,
                           LoginResultStorage loginResultStorage, GameService gameService, VictoryStorage victoryStorage) {

        this.pioneersApiService = pioneersApiService;
        this.mapper = mapper;
        this.gameStorage = gameStorage;
        this.loginResultStorage = loginResultStorage;
        this.gameService = gameService;
        this.victoryStorage = victoryStorage;
    }

    public Game getStoredGame() {

        return gameStorage.getGame();
    }

    public LoginResult getStoredData() {
        return loginResultStorage.getLoginResult();
    }

    public boolean checkRoadResources(Player thisPlayer) {
        Resources ownResources = thisPlayer.resources();
        boolean enoughLumber = ownResources.lumber() >= 1;
        boolean enoughBrick = ownResources.brick() >= 1;
        boolean enoughRoads = thisPlayer.remainingBuildings().road() >= 1;
        return enoughBrick && enoughLumber && enoughRoads;
    }

    public boolean checkSettlementResources(Player thisPlayer) {
        Resources ownResources = thisPlayer.resources();
        boolean enoughLumber = ownResources.lumber() >= 1;
        boolean enoughBrick = ownResources.brick() >= 1;
        boolean enoughWool = ownResources.wool() >= 1;
        boolean enoughGrain = ownResources.grain() >= 1;
        boolean enoughSettlements = thisPlayer.remainingBuildings().settlement() >= 1;
        return enoughLumber && enoughBrick && enoughWool && enoughGrain && enoughSettlements;
    }

    public boolean checkCityResources(Player thisPlayer) {
        Resources ownResources = thisPlayer.resources();
        boolean enoughGrain = ownResources.grain() >= 2;
        boolean enoughOre = ownResources.ore() >= 3;
        boolean enoughCities = thisPlayer.remainingBuildings().city() >= 1;
        return enoughGrain && enoughOre && enoughCities;
    }

    public boolean checkDevelopmentResources(Player thisPlayer) {
        Resources ownResources = thisPlayer.resources();
        boolean enoughWool = ownResources.wool() >= 1;
        boolean enoughOre = ownResources.ore() >= 1;
        boolean enoughGrain = ownResources.grain() >= 1;
        return enoughWool && enoughOre && enoughGrain;
    }

    public boolean checkNoDevelopmentCardsAvailable(java.util.Map<String, Player> userIdToPlayer) {
        // Calculate max development card and count all development cards there are in game
        // If they have the same sum return true because there are no cards left in the game
        // else return false
        int developmentCardsSum;
        int multiplier = 0;
        int developmentCardsInGame = 0;

        // Check if there are more than 4 players and set the multiplier
        if (userIdToPlayer.size() > 4) {
            multiplier = (int) Math.ceil((double) (userIdToPlayer.size() - 4) / 2);
        }
        // Knights, victory points, road building, monopoly, year of plenty
        developmentCardsSum = (14 + 6 * multiplier) + (5) + (2 + multiplier) + (2 + multiplier) + (2 + multiplier);
        for (Player player : userIdToPlayer.values()) {
            developmentCardsInGame += player.developmentCards().size();
        }
        return developmentCardsSum == developmentCardsInGame;
    }

    public Observable<Map> getMap(String gameId) {
        //Gets SERVER map information
        return pioneersApiService.getMap(gameId).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Map(errorResult, null, null);
        });
    }

    public Observable<Move> makeMoveBuilding(String action, Number x, Number y, Number z, Number side, String type) {
        return makeMove(action, null, null, null, null, new CreateBuildingDto(x, y, z, side, type));
    }

    public Observable<Move> makeMoveRob(String action, Number x, Number y, Number z, String robTarget) {
        return makeMove(action, new RobDto(x, y, z, robTarget), null, null, null, null);
    }

    public Observable<Move> makeMoveAction(String action) {
        return makeMove(action, null, null, null, null, null);
    }

    public Observable<Move> makeMoveTrade(String action, ResourcesDto resourcesDto, String partnerId) {
        return makeMove(action, null, resourcesDto, partnerId, null, null);
    }

    public Observable<Move> makeMoveResources(String action, ResourcesDto resourcesDto) {
        return makeMove(action, null, resourcesDto, null, null, null);
    }

    public Observable<Move> makeMoveDevelopmentCard(String action, String developmentCard) {
        CreateMoveDto payload = new CreateMoveDto(action, null, null, null, developmentCard, null);
        return pioneersApiService.createMove(getStoredGame()._id(), payload).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Move(errorResult, "", "", "", "", 0, "", null, null, "");
        });
    }

    public Observable<Move> makeMove(String action, RobDto robDto, ResourcesDto resourcesDto, String partner, String developmentCard, CreateBuildingDto createBuildingDto) {
        //GENERIC MAKE MOVE - called by all upper-level functions such as makeMoveBuilding
        CreateMoveDto payload = new CreateMoveDto(action, robDto, resourcesDto, partner, developmentCard, createBuildingDto);
        return pioneersApiService.createMove(getStoredGame()._id(), payload).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Move(errorResult, "", "", "", "", 0, "", null, null, "");
        });
    }

    public Observable<State> getGameState() {
        return this.pioneersApiService.getState(getStoredGame()._id()).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new State("", errorResult, new ArrayList<>(), null);
        });
    }

    public boolean moveWasDone(State previousState, State currentState, Constants.ACTION action) {
        //This method checks if a move was performed by comparing the previous and current state passed.
        //An action is detected as performed if it was in previous State, but is not in currentState.
        return expectedMovesContainsAction(previousState.expectedMoves(), action) && !expectedMovesContainsAction(currentState.expectedMoves(), action);
    }

    public boolean expectedMovesContainsAction(List<ExpectedMove> expectedMoves, Constants.ACTION action) {
        //Returns TRUE if the passed expectedMoves list contains the specified Action, FALSE otherwise
        for (ExpectedMove expectedMove : expectedMoves) {
            if (expectedMove.action().equals(action.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean expectedMovesContainsPlayer(List<ExpectedMove> expectedMoves, Player player) {
        //Returns TRUE if the passed expectedMoves list contains the specified Player, FALSE otherwise
        for (ExpectedMove expectedMove : expectedMoves) {
            if (expectedMove.players().contains(player.userId())) {
                return true;
            }
        }
        return false;
    }

    public boolean expectedMovesContainsExpectedMove(List<ExpectedMove> expectedMoves, Player player, Constants.ACTION action) {
        //Returns TRUE if expectedMoves contains a specific Expected Move consisting of the player and the action in particular
        for (ExpectedMove expectedMove : expectedMoves) {
            if (expectedMove.players().contains(player.userId()) && expectedMove.action().contains(action.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean canAffordCounterOffer(ResourcesDto resourcesDto, Resources resources) {
        boolean grain = Math.max(resourcesDto.grain(), 0) <= resources.grain();
        boolean lumber = Math.max(resourcesDto.lumber(), 0) <= resources.lumber();
        boolean wool = Math.max(resourcesDto.wool(), 0) <= resources.wool();
        boolean ore = Math.max(resourcesDto.ore(), 0) <= resources.ore();
        boolean brick = Math.max(resourcesDto.brick(), 0) <= resources.brick();
        return grain && lumber && wool && ore && brick;
    }

    public Observable<List<Player>> getGamePlayers() {
        return this.pioneersApiService.getPlayers(getStoredGame()._id()).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            ArrayList<Player> errorReturn = new ArrayList<>();
            errorReturn.add(new Player(errorResult, "", "", true, 0, null, null, null, null, null));
            return errorReturn;
        });
    }

    public Observable<List<Building>> getBuildings() {
        return this.pioneersApiService.getBuildings(getStoredGame()._id());
    }

    public Observable<Player> setPlayerInactive(boolean active) {
        return this.pioneersApiService.setUserActive(getStoredGame()._id(), getStoredData()._id(), new UpdatePlayerDto(active)).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Player(errorResult, Constants.CUSTOM_ERROR, "", true, 0, null, null, null, null, null);
        });
    }

    public Player determineLongestRoad(java.util.Map<String, Player> userIdToPlayer) {

        Player candidate = null;

        // determine layer with the longest road
        for (var entry : userIdToPlayer.entrySet()) {
            if (entry.getValue().hasLongestRoad()) {
                candidate = entry.getValue();
            }
        }

        return candidate;
    }

    public Player determineKnights(java.util.Map<String, Player> userIdToPlayer, String id, boolean rejoin) {

        Player candidate = null;

        // determine army sizes
        if (!rejoin) {

            if (armies.containsKey(id)) {
                armies.put(id, armies.get(id) + 1);
            } else {
                armies.put(id, 1);
            }


        } else {
            for (var entry : userIdToPlayer.entrySet()) {
                int number = 0;
                for (var card : entry.getValue().developmentCards()) {
                    if (card.type().equals("knight") && card.revealed()) {
                        number++;
                    }
                }
                if (armies.containsKey(entry.getValue().userId())) {
                    armies.put(entry.getValue().userId(), number);
                } else {
                    armies.put(entry.getValue().userId(), number);
                }
            }
        }

        victoryStorage.setKnights(armies);

        // determine player with the largest army
        for (var entry : userIdToPlayer.entrySet()) {
            if (entry.getValue().hasLargestArmy()) {
                candidate = entry.getValue();
            }
        }

        return candidate;
    }

    public HashMap<String, Integer> getArmies() {
        return armies;
    }

    public Observable<Game> deleteGame() {
        return gameService.deleteGame(gameStorage.getGame()._id());
    }
}
