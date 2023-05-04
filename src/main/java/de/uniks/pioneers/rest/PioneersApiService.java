package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateMoveDto;
import de.uniks.pioneers.dto.UpdatePlayerDto;
import de.uniks.pioneers.model.*;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface PioneersApiService {

    @GET("games/{gameId}/map")
    Observable<Map> getMap(@Path("gameId") String gameId);

    @GET("games/{gameId}/players")
    Observable<List<Player>> getPlayers(@Path("gameId") String gameId);

    @GET("games/{gameId}/players/{userId}")
    Observable<Player> getUser(@Path("gameId") String gameId, @Path("userId") String userId);

    @PATCH("games/{gameId}/players/{userId}")
    Observable<Player> setUserActive(@Path("gameId") String gameId, @Path("userId") String userId, @Body UpdatePlayerDto dto);

    @GET("games/{gameId}/state")
    Observable<State> getState(@Path("gameId") String gameId);

    @GET("games/{gameId}/buildings")
    Observable<List<Building>> getBuildings(@Path("gameId") String gameId);

    @GET("games/{gameId}/buildings/{buildingId}")
    Observable<Building> getBuilding(@Path("gameId") String gameId, @Path("buildingId") String buildingId);

    @POST("games/{gameId}/moves")
    Observable<Move> createMove(@Path("gameId") String gameId, @Body CreateMoveDto dto);

    @GET("games/{gameId}/moves")
    Observable<Move> getMoveThroughUserId(@Path("gameId") String gameId, @Query("userId") String userId);

    @GET("games/{gameId}/moves/{moveId}")
    Observable<Move> getMoveThroughMoveId(@Path("gameId") String gameId, @Query("moveId") String moveId);

}
