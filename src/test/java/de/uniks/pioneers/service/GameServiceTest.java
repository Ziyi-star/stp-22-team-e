package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateGameDto;
import de.uniks.pioneers.model.Game;
import de.uniks.pioneers.model.GameSettings;
import de.uniks.pioneers.rest.GameApiService;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Spy
    GameStorage gameStorage;

    @Spy
    ObjectMapper mapper;

    @Mock
    GameApiService gameApiService;

    @InjectMocks
    GameService gameService;

    @Test
    void createdLobby() {
        when(gameApiService.create(any())).thenReturn(Observable.just(new Game("1", "2", "3", "Lobby", "Owen", 0, false, new GameSettings(2, 10))));

        // create game
        final String result = gameService.createdLobby("Lobby", "myPassword", new GameSettings(2, 10)).blockingFirst();
        // create Lobby success test
        assertEquals(Constants.LOBBY_CREATION_SUCCESS, result);
        // check return value
        assertEquals("3", gameStorage.getGame()._id());
        verify(gameApiService).create(new CreateGameDto("Lobby", false, new GameSettings(2, 10), "myPassword"));

        // Error case
        // Create error message
        final String errMsg = "That's an error";

        // Response with an error that is not a http exception when used game api service
        // and try to create a game
        when(gameApiService.create(any())).thenReturn(Observable.error(new Throwable()));
        final String errorResult = gameService.createdLobby("Lobby", "12", new GameSettings(2, 10)).blockingFirst();

        // Check that you get an error message
        assertEquals(Constants.CUSTOM_ERROR, errorResult);

        // Create an error response body with no message
        final String errorResponseNoMsg = "{" + "\"statusCode\": 400," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoMsg = ResponseBody.create(errorResponseNoMsg, MediaType.parse("application/json"));

        // Response with an error that is a http exception and try to create a game
        when(gameApiService.create(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoMsg))));
        final String errorResultNoMsg = gameService.createdLobby("Lobby", "12", new GameSettings(2, 10)).blockingFirst();

        // Check that you get an error message
        assertEquals(Constants.CUSTOM_ERROR, errorResultNoMsg);

        // Create an error response with message with no array
        // and try to create a game
        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"That's an error\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));

        when(gameApiService.create(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoArray))));
        final String errorResultNoArray = gameService.createdLobby("Lobby", "12", new GameSettings(2, 10)).blockingFirst();

        // Check that you get the error message from response
        assertEquals(errMsg, errorResultNoArray);

        // Create an error response with message in array
        // and try to create a Lobby
        final String errorResponseArray = "{" + "\"statusCode\": 400," + "\"message\": [\"That's an error\"]," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyArray = ResponseBody.create(errorResponseArray, MediaType.parse("application/json"));

        when(gameApiService.create(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyArray))));
        final String errorResultArray = gameService.createdLobby("Lobby", "12", new GameSettings(2, 10)).blockingFirst();

        // Check that you get the error message from response
        assertEquals(errMsg, errorResultArray);
    }

    @Test
    void getGame() {
        Game game = new Game("123", "1234", "12345678", "string", "stringst", 2, false, null);
        when(gameApiService.getGame(any())).thenReturn(Observable.just(game));
        final Game result = gameService.getGame("12345678").blockingFirst();
        assertEquals(2, result.members());
        verify(gameApiService).getGame("12345678");

        // Error case
        // Create error message
        final String errMsg = "That's an error";

        // Response with an error that is not a http exception when used game api service
        // and try to getGame
        when(gameApiService.getGame(ArgumentMatchers.any())).thenReturn(Observable.error(new Throwable()));
        final Game errorResult = gameService.getGame("12345678").blockingFirst();

        // Check that you get an error message
        assertEquals(Constants.GET_GAME_ERROR, errorResult.name());

        // Create an error response body with no message
        final String errorResponseNoMsg = "{" + "\"statusCode\": 400," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoMsg = ResponseBody.create(errorResponseNoMsg, MediaType.parse("application/json"));

        // Response with an error that is a http exception and try to create a game
        when(gameApiService.getGame(ArgumentMatchers.any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoMsg))));
        final Game errorResultNoMsg = gameService.getGame("12345678").blockingFirst();

        // Check that you get an error message
        assertEquals(Constants.GET_GAME_ERROR, errorResultNoMsg.name());

        // Create an error response with message with no array
        // and try to getGame
        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"That's an error\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));

        when(gameApiService.getGame(ArgumentMatchers.any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoArray))));
        final Game errorResultNoArray = gameService.getGame("12345678").blockingFirst();

        // Check that you get the error message from response
        assertEquals(errMsg, errorResultNoArray.name());

        // Create an error response with message in array
        // and try to getGame
        final String errorResponseArray = "{" + "\"statusCode\": 400," + "\"message\": [\"That's an error\"]," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyArray = ResponseBody.create(errorResponseArray, MediaType.parse("application/json"));

        when(gameApiService.getGame(ArgumentMatchers.any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyArray))));
        final Game errorResultArray = gameService.getGame("12345678").blockingFirst();

        // Check that you get the error message from response
        assertEquals(errMsg, errorResultArray.name());
    }

}