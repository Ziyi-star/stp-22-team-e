package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateMemberDto;
import de.uniks.pioneers.model.Game;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.Member;
import de.uniks.pioneers.rest.GameMemberApiService;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameMemberServiceTest {
    @Spy
    GameStorage gameStorage;

    @Spy
    LoginResultStorage loginResultStorage;

    @Spy
    ObjectMapper mapper;

    @Mock
    GameMemberApiService gameMemberApiService;

    @InjectMocks
    GameMemberService gameMemberService;

    @Test
    void joinLobby() {

        when(gameMemberApiService.joinLobby(any(), any())).thenReturn((Observable.just(new Member("1000", "2000", "12345", "5678", true, "#000000", false))));
        final String result = gameMemberService.joinLobby("12345", "12345678").blockingFirst();
        // join Lobby success test
        assertEquals(Constants.JOIN_LOBBY_SUCCESS, result);

        verify(gameMemberApiService).joinLobby("12345", new CreateMemberDto(false, false, "12345678"));

        // Error case
        // 1. Create error message
        final String errMsg = "That's an error";
        // Response with an error that is not a http exception when used user api service
        // and try to join a lobby
        when(gameMemberApiService.joinLobby(any(), any())).thenReturn(Observable.error(new Throwable()));
        final String errorResult = gameMemberService.joinLobby("12345", "12345678").blockingFirst();
        // Check that you get a registration error message
        assertEquals(Constants.JOIN_LOBBY_ERROR, errorResult);

        // 2. Create an error response body with no message
        final String errorResponseNoMsg = "{" + "\"statusCode\": 400," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoMsg = ResponseBody.create(errorResponseNoMsg, MediaType.parse("application/json"));
        // Response with an error that is a http exception and try to and try to join a lobby
        when(gameMemberApiService.joinLobby(any(), any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoMsg))));
        final String errorResultNoMsg = gameMemberService.joinLobby("12345", "12345678").blockingFirst();
        // Check that you get a registration error message
        assertEquals(Constants.JOIN_LOBBY_ERROR, errorResultNoMsg);

        // 3.Create an error response with message with no array
        // and try to join a lobby
        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"That's an error\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));
        when(gameMemberApiService.joinLobby(any(), any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoArray))));
        final String errorResultNoArray = gameMemberService.joinLobby("12345", "12345678").blockingFirst();
        // Check that you get the error message from response
        assertEquals(errMsg, errorResultNoArray);

        // 4. Create an error response with message in array
        // and try to join a lobby
        final String errorResponseArray = "{" + "\"statusCode\": 400," + "\"message\": [\"That's an error\"]," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyArray = ResponseBody.create(errorResponseArray, MediaType.parse("application/json"));
        when(gameMemberApiService.joinLobby(any(), any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyArray))));
        final String errorResultArray = gameMemberService.joinLobby("12345", "12345678").blockingFirst();
        // Check that you get the error message from response
        assertEquals(errMsg, errorResultArray);
    }

    @Test
    void getGameMember() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, false, null);
        gameStorage.setGame(expectedGame);

        LoginResult loginResult = new LoginResult("1", "1", "5678", "testus", "online", "http://test", null, "12345678", "987654321");
        loginResultStorage.setLoginResult(loginResult);

        //sucess case
        when(gameMemberApiService.getGameMember(any(), any())).thenReturn(Observable.just(new Member("1", "1", "3", "5678", true, "#000000", false)));
        final Member result = gameMemberService.getGameMember().blockingFirst();
        assertEquals(result.color(), "#000000");

        //error case
        when(gameMemberApiService.getGameMember(any(), any())).thenReturn(Observable.error(new Throwable()));
        final Member errorResult = gameMemberService.getGameMember().blockingFirst();
        assertEquals(errorResult.color(), Constants.LOBBY_GET_MEMBER_INFO_ERROR);

    }

    @Test
    void updateMemberReady() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, false, null);
        gameStorage.setGame(expectedGame);

        LoginResult loginResult = new LoginResult("1", "1", "5678", "testus", "online", "http://test", null, "12345678", "987654321");
        loginResultStorage.setLoginResult(loginResult);

        //sucess case
        when(gameMemberApiService.updateMember(any(), anyString(), any())).thenReturn((Observable.just(new Member("1", "1", "3", "5678", true, "#000000", false))));
        final String result = gameMemberService.updateMemberReady(true, "#000000").blockingFirst();
        assertEquals(result, Constants.CHANGE_MEMBER_SHIP_SUCCESS);
        //error case
        when(gameMemberApiService.updateMember(any(), anyString(), any())).thenReturn(Observable.error(new Throwable()));
        final String errorResult = gameMemberService.updateMemberReady(false, "#000000").blockingFirst();
        assertEquals(errorResult, Constants.CUSTOM_ERROR);

    }

    @Test
    void updateMemberColor() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, false, null);
        gameStorage.setGame(expectedGame);

        LoginResult loginResult = new LoginResult("1", "1", "5678", "testus", "online", "http://test", null, "12345678", "987654321");
        loginResultStorage.setLoginResult(loginResult);
        //errorcase
        when(gameMemberApiService.updateMember(any(), anyString(), any())).thenReturn(Observable.error(new Throwable()));
        final String errorResult = gameMemberService.updateMemberColor("#000000").blockingFirst();
        assertEquals(errorResult, Constants.CUSTOM_ERROR);

    }

    @Test
    void updateMemberSpectator() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, false, null);
        gameStorage.setGame(expectedGame);
        LoginResult loginResult = new LoginResult("1", "1", "5678", "testus", "online", "http://test", null, "12345678", "987654321");
        loginResultStorage.setLoginResult(loginResult);
        //success case
        when(gameMemberApiService.updateMember(any(), anyString(), any())).thenReturn((Observable.just(new Member("1", "1", "3", "5678", true, "#000000", true))));
        final String result = gameMemberService.updateMemberSpectator(true).blockingFirst();
        assertEquals(result, Constants.CHANGE_MEMBER_SHIP_SUCCESS);
        //error case
        when(gameMemberApiService.updateMember(any(), anyString(), any())).thenReturn(Observable.error(new Throwable()));
        final String errorResult = gameMemberService.updateMemberSpectator(false).blockingFirst();
        assertEquals(errorResult, Constants.CUSTOM_ERROR);
    }
}