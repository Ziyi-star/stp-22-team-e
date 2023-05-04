package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateMemberDto;
import de.uniks.pioneers.dto.UpdateMemberDto;
import de.uniks.pioneers.model.Member;
import de.uniks.pioneers.rest.GameMemberApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import java.util.List;

public class GameMemberService {

    private final GameMemberApiService gameMemberApiService;
    private final GameStorage gameStorage;
    private final LoginResultStorage loginResultStorage;
    private final ObjectMapper mapper;

    @Inject
    public GameMemberService(GameMemberApiService gameMemberApiService, GameStorage gameStorage, LoginResultStorage loginResultStorage, ObjectMapper mapper) {
        this.gameMemberApiService = gameMemberApiService;
        this.gameStorage = gameStorage;
        this.loginResultStorage = loginResultStorage;
        this.mapper = mapper;
    }

    public Observable<String> joinLobby(String gameId, String lobbyPassword) {
        boolean ready = false;
        boolean spectator = false;
        return gameMemberApiService.joinLobby(gameId, new CreateMemberDto(ready, spectator, lobbyPassword))
                .map(GameMemberStatus -> Constants.JOIN_LOBBY_SUCCESS)
                .onErrorReturn(error -> {
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                        // some other error happened while joinLobby
                        return Constants.JOIN_LOBBY_ERROR;
                    } else {
                        // return failed joinLobby with error message
                        return errorResult;
                    }
                });
    }

    public Observable<Member> exitLobby() {
        //no idea how to get user id use userIdStorage first
        String gameId = gameStorage.getGame()._id();
        String userId = loginResultStorage.getLoginResult()._id();
        return gameMemberApiService.deleteGameMember(gameId, userId)
                .onErrorReturn(error -> {
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                        // some other error happened while exitLobby
                        return new Member(Constants.LOBBY_EXIT_ERROR, Constants.LOBBY_EXIT_ERROR, Constants.LOBBY_EXIT_ERROR, Constants.LOBBY_EXIT_ERROR, true, "#000000", false);
                    } else {
                        // return failed exitLobby with error message
                        return new Member(Constants.LOBBY_EXIT_ERROR, errorResult, errorResult, errorResult, true, "#000000", false);
                    }
                });
    }

    public Observable<List<Member>> findAllGamesMembers() {
        return this.gameMemberApiService.getGameMembers(gameStorage.getGame()._id());
    }

    public Observable<Member> getGameMember() {
        return gameMemberApiService.getGameMember(gameStorage.getGame()._id(), loginResultStorage.getLoginResult()._id())
                .onErrorReturn(error -> {
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                        // some other error happened while exitLobby
                        return new Member(Constants.LOBBY_GET_MEMBER_INFO_ERROR, Constants.LOBBY_GET_MEMBER_INFO_ERROR, Constants.LOBBY_GET_MEMBER_INFO_ERROR, Constants.LOBBY_GET_MEMBER_INFO_ERROR, false, Constants.LOBBY_GET_MEMBER_INFO_ERROR, false);
                    } else {
                        // return failed exitLobby with error message
                        return new Member(errorResult, errorResult, errorResult, errorResult, false, "#000000", false);
                    }
                });
    }


    public Observable<String> updateMemberReady(boolean ready, String color) {
        return gameMemberApiService.updateMember(gameStorage.getGame()._id(), loginResultStorage.getLoginResult()._id(), new UpdateMemberDto(ready, color, false))
                .map(result -> Constants.CHANGE_MEMBER_SHIP_SUCCESS)
                .onErrorReturn(error -> {
                    // changeMemberShip was not successful
                    return new ErrorHandling().handleError(error, mapper);
                });
    }

    public Observable<String> updateMemberColor(String chosenColor) {
        return gameMemberApiService.updateMember(gameStorage.getGame()._id(), loginResultStorage.getLoginResult()._id(), new UpdateMemberDto(false, chosenColor, false))
                .map(result -> Constants.CHANGE_MEMBER_SHIP_SUCCESS)
                .onErrorReturn(error -> {
                    // changeMemberShip was not successful
                    return new ErrorHandling().handleError(error, mapper);
                });
    }

    public Observable<String> updateMemberSpectator(Boolean spectate) {
        return gameMemberApiService.updateMember(gameStorage.getGame()._id(), loginResultStorage.getLoginResult()._id(), new UpdateMemberDto(spectate, null, spectate))
                .map(result -> Constants.CHANGE_MEMBER_SHIP_SUCCESS)
                .onErrorReturn(error -> {
                    // changeMemberShip was not successful
                    return new ErrorHandling().handleError(error, mapper);
                });
    }
}