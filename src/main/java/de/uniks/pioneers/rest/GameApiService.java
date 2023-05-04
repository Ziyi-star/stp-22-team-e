package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateGameDto;
import de.uniks.pioneers.dto.UpdateGameDto;
import de.uniks.pioneers.model.Game;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface GameApiService {
    @POST("games")
    Observable<Game> create(@Body CreateGameDto dto);

    @GET("games")
    Observable<List<Game>> findAllGames();

    @GET("games/{id}")
    Observable<Game> getGame(@Path("id") String id);

    @PATCH("games/{id}")
    Observable<Game> updateGame(@Path("id") String _id, @Body UpdateGameDto dto);

    @DELETE("games/{id}")
    Observable<Game> delete(@Path("id") String id);


}

