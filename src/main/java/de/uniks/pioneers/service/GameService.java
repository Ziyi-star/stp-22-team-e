package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateGameDto;
import de.uniks.pioneers.dto.UpdateGameDto;
import de.uniks.pioneers.model.Game;
import de.uniks.pioneers.model.GameSettings;
import de.uniks.pioneers.rest.GameApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import java.util.List;

public class GameService {

    private final GameApiService gameApiService;
    private final GameStorage gameStorage;
    private final ObjectMapper mapper;

    @Inject
    public GameService(GameApiService gameApiService, GameStorage gameStorage, ObjectMapper mapper) {
        this.gameApiService = gameApiService;
        this.gameStorage = gameStorage;
        this.mapper = mapper;
    }

    public Observable<String> createdLobby(String name, String password, GameSettings gameSettings) {
        // Created Lobby
        return gameApiService.create(new CreateGameDto(name, false, gameSettings, password))
                .doOnNext(gameStorage::setGame)
                .map(result -> Constants.LOBBY_CREATION_SUCCESS)
                .onErrorReturn(error -> new ErrorHandling().handleError(error, mapper));
    }

    public Observable<List<Game>> findAllGames() {
        return this.gameApiService.findAllGames();
    }

    public Observable<Game> deleteGame(String game_id) {
        return gameApiService.delete(game_id).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                // some other error happened while exitLobby
                return new Game(Constants.LOBBY_DELETE_ERROR, Constants.LOBBY_DELETE_ERROR, Constants.LOBBY_DELETE_ERROR, Constants.LOBBY_DELETE_ERROR, Constants.LOBBY_DELETE_ERROR, 1, false, null);
            } else {
                // return failed exitLobby with error message
                return new Game(Constants.LOBBY_DELETE_ERROR, errorResult, errorResult, errorResult, errorResult, 1, false, null);
            }
        });
    }

    public Observable<String> updateGame(String gameName, String user_id, boolean started, GameSettings gameSettings, String gamePassword) {
        //Function call with all parameters
        return gameApiService.updateGame(gameStorage.getGame()._id(), new UpdateGameDto(gameName, user_id, started, gameSettings, gamePassword)).map(result -> Constants.UPDATE_GAME_SUCCESS)
                .onErrorReturn(error -> new ErrorHandling().handleError(error, mapper));
    }

    public Observable<String> updateGame(String gameName, String user_id, boolean started) {
        //Function call with password parameter missing for comfort
        return updateGame(gameName, user_id, started, null, null);
    }

    public Game getStoredGame() {
        return gameStorage.getGame();
    }

    public void setStoredGame(Game game) {
        gameStorage.setGame(game);
    }

    public Observable<Game> getGame(String gameId) {
        return gameApiService.getGame(gameId).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                // some other error happened while exitLobby
                return new Game(Constants.GET_GAME_ERROR, Constants.GET_GAME_ERROR, Constants.GET_GAME_ERROR, Constants.GET_GAME_ERROR, Constants.GET_GAME_ERROR, 0, false, null);
            } else {
                // return failed exitLobby with error message
                return new Game(Constants.GET_GAME_ERROR, errorResult, errorResult, errorResult, errorResult, 0, false, null);
            }
        });
    }
}

