package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateVoteDto;
import de.uniks.pioneers.model.Vote;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface MapVotesApiService {

    @POST("maps/{mapId}/votes")
    Observable<Vote> vote(@Path("mapId") String mapId, @Body CreateVoteDto createVoteDto);

    @GET("maps/{mapId}/votes")
    Observable<List<Vote>> getVotes(@Path("mapId") String mapId);

    @GET("users/{userId}/votes")
    Observable<List<Vote>> getVotesByUser(@Path("userId") String userId);

    @GET("maps/{mapId}/votes/{userId}")
    Observable<Vote> getVote(@Path("mapId") String mapId, @Path("userId") String userId);

    @PATCH("maps/{mapId}/votes/{userId}")
    Observable<Vote> updateVote(@Path("mapId") String mapId, @Path("userId") String userId, @Body CreateVoteDto createVoteDto);

    @DELETE("maps/{mapId}/votes/{userId}")
    Observable<Vote> deleteVote(@Path("mapId") String mapId, @Path("userId") String userId);
}
